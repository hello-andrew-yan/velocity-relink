package io.github.helloandrewyan.relink;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Relink is a utility plugin for <a href="https://velocitypowered.com/">Velocity</a>
 * that will automatically will relink players to their previously connected server within the network.
 */
@Plugin(
        id = Relink.ID,
        name = Relink.NAME,
        version = Relink.VERSION,
        url = Relink.URL,
        description = Relink.DESCRIPTION,
        authors = {Relink.AUTHOR}
)
public class Relink {
    public static final String ID = "relink";
    public static final String NAME = "Relink";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String URL = "https://github.com/hello-andrew-yan/relink";
    public static final String DESCRIPTION =
            "Utility plugin that will relink you to your previously connected server in a Velocity network.";
    public static final String AUTHOR = "hello-andrew-yan";
    private static final String CONFIG_FILE = "config.toml";
    private static final String CONFIG_PROXY_TABLE = "proxy";
    private static final String CONFIG_LINKED = "linked";
    private static final String CONFIG_SQL_TABLE = "sql";
    private static final String JDBC_SCHEME = "jdbc";
    private static final Set<String> SUPPORTED_PROTOCOLS =
            new HashSet<>(Arrays.asList("mysql", "postgresql", "oracle"));
    private final ProxyServer server;
    private final Logger logger;
    private final Path directory;
    private Connection connection;
    private final Map<String, RegisteredServer> proxy = new HashMap<>();
    private final List<String> linked = new ArrayList<>();

    @Inject
    public Relink(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;
        this.directory = directory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadProxy();
        Toml config = getConfig();
        if (config == null || config.isEmpty() || !validateProxy(config) || !validateSQL(config)) {
            logger.warn("Plugin could not be properly loaded.");
            return;
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            logger.warn("Connection failed to close: {}", exception.getMessage());
        }
    }

    private void loadProxy() {
        server.getAllServers().forEach(server -> proxy.put(server.getServerInfo().getName(), server));
    }

    private Toml getConfig() {
        File configFile = new File(directory.toFile(), CONFIG_FILE);
        try (InputStream resource = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            if (resource == null) {
                logger.warn("Resource {} not found. Contact developer.", CONFIG_FILE);
                return null;
            }
            Files.createDirectories(configFile.getParentFile().toPath());
            if (!configFile.exists()) {
                Files.copy(resource, configFile.toPath());
                logger.info("Copying new configuration file from resources.");
            }
            return new Toml().read(configFile);
        } catch (IOException exception) {
            logger.warn("Failed to read configuration file: {}", exception.getMessage());
            return null;
        }
    }

    private boolean validateProxy(Toml config) {
        logger.info("Validating proxy configuration...");
        Toml proxyTable = config.getTable(CONFIG_PROXY_TABLE);
        if (proxyTable == null) {
            logger.warn("Missing TOML table [{}].", CONFIG_PROXY_TABLE);
            return false;
        }
        List<String> temp = proxyTable.getList(CONFIG_LINKED);
        if (temp == null) {
            logger.warn("Missing TOML list \"{}\".", CONFIG_LINKED);
            return false;
        }
        temp.stream()
                .filter(server -> !proxy.containsKey(server))
                .forEach(server -> logger.warn("Could not find server \"{}\". Skipping.", server));
        linked.addAll(temp);
        if (linked.isEmpty()) {
            logger.warn("No servers have been linked to Relink configuration.");
            return false;
        }
        return true;
    }

    private boolean validateSQL(Toml config) {
        logger.info("Validating SQL configuration...");
        Toml table = config.getTable(CONFIG_SQL_TABLE);
        if (table == null) {
            logger.warn("Missing TOML table [{}]", CONFIG_SQL_TABLE);
            return false;
        }
        String url = table.getString("url");
        String username = table.getString("username");
        String password = table.getString("password");

        if (url == null || username == null || password == null) {
            logger.warn("SQL details were not properly set");
            return false;
        }
        if (!validateDatabaseURL(url)) return false;
        return loadDatabase(url, username, password);
    }

    private boolean validateDatabaseURL(String url) {
        try {
            URI databaseURI = new URI(url);
            if (!JDBC_SCHEME.equals(databaseURI.getScheme())) {
                throw new MalformedURLException("Database URL is invalid.");
            }
            String[] parts = databaseURI.getSchemeSpecificPart().split(":");
            if (parts.length < 2) {
                throw new MalformedURLException("Database URL is missing protocol details.");
            }
            if (!SUPPORTED_PROTOCOLS.contains(parts[0].toLowerCase())) {
                throw new MalformedURLException("Unsupported database protocol.");
            }
        } catch (URISyntaxException | MalformedURLException exception) {
            logger.info("Failed to validate database URL: {}", exception.getMessage());
            return false;
        }
        return true;
    }

    private boolean loadDatabase(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(url, username, password);
            logger.info("Database connection established.");
            return true;
        } catch (SQLException exception) {
            logger.warn("Database connection could not be established: {}", exception.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public List<String> getLinked() {
        return linked;
    }

    public RegisteredServer getServer(String server) {
        return proxy.get(server);
    }
}
