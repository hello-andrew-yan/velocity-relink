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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Relink is a utility plugin for <a href="https://velocitypowered.com/">Velocity</a>
 * that automatically will relink players to their previously connected server within the network.
 */
@Plugin(
        id = "relink",
        name = "Relink",
        version = "1.0-SNAPSHOT",
        url = "https://github.com/hello-andrew-yan/relink",
        description = "Utility plugin that will relink you to your previously connected server in a Velocity network.",
        authors = {"hello-andrew-yan"}
)
public class Relink {
    private final ProxyServer server;
    private final Logger logger;
    private final Path directory;
    private static Connection connection;
    private final Map<String, RegisteredServer> proxy = new HashMap<>();
    private final List<String> linked = new ArrayList<>();
    private static final String CONFIG_FILE = "config.toml";
    private static final String CONFIG_PROXY_TABLE = "proxy";
    private static final String CONFIG_LINKED = "linked";
    private static final String CONFIG_SQL_TABLE = "sql";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Set<String> SUPPORTED_PROTOCOLS = new HashSet<>(Arrays.asList("mysql", "postgresql", "oracle"));

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
        if (config == null || config.isEmpty() || !validateProxy(config) || !loadDatabase(config)) {
            logger.warn("Plugin could not be properly loaded.");
            return;
        }

        // TODO - TEMPORARY SAFE CLOSING HERE
        try {
            connection.close();
        } catch (SQLException exception) {
            logger.warn("Connection failed to close: " + exception.getMessage());
        }
    }
    /**
     * Loads all registered servers in the velocity network into the proxy map.
     */
    private void loadProxy() {
        server.getAllServers().forEach(server -> proxy.put(server.getServerInfo().getName(), server));
    }

    /**
     * Reads and retrieves the configuration from the config file.
     *
     * @return the {@link Toml} configuration object
     */
    private Toml getConfig() {
        File configFile = new File(directory.toFile(), CONFIG_FILE);
        try (InputStream resource = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            // Defensive check in case resources is not properly established in pom.xml
            if (resource == null) {
                logger.warn("Resource " + CONFIG_FILE + " not found. Contact developer.");
                return null;
            }
            // Creates the plugin directory if it doesn't exist.
            Files.createDirectories(configFile.getParentFile().toPath());

            if (!configFile.exists()) {
                // Copies the resource config file to new file.
                Files.copy(resource, configFile.toPath());
                logger.info("Copying new configuration file from resources.");
            }
            return new Toml().read(configFile);
        } catch (IOException exception) {
            logger.warn("Failed to read configuration file: {}", exception.getMessage());
            return null;
        }
    }

    /**
     * Validates the proxy configuration from the TOML file.
     *
     * @param config the {@link Toml} configuration object
     * @return       {@code true} if the proxy configuration is valid, {@code false} otherwise
     */
    private boolean validateProxy(Toml config) {
        logger.info("Validating proxy configuration...");
        Toml proxyTable = config.getTable(CONFIG_PROXY_TABLE);
        if (proxyTable == null) {
            logger.warn("Missing TOML table [" + CONFIG_PROXY_TABLE + "]");
            return false;
        }
        List<String> temp = proxyTable.getList(CONFIG_LINKED);
        if (temp == null) {
            logger.warn("Missing TOML list \"" + CONFIG_LINKED + "\"");
            return false;
        }

        temp.stream()
                .filter(server -> !proxy.containsKey(server))
                .forEach(server -> logger.warn("Could not find server \"{}\". Ignoring.", server));
        linked.addAll(temp);

        if (linked.isEmpty()) {
            logger.warn("No servers have been linked to Relink configuration.");
            return false;
        }

        return true;
    }

    /**
     * Loads the database configuration from the TOML file and establishes a connection to the database.
     *
     * @param config the {@link Toml} configuration object
     * @return       {@code true} if the database connection is successfully established, {@code false} otherwise
     */
    private boolean loadDatabase(Toml config) {
        logger.info("Validating SQL configuration...");
        Toml table = config.getTable(CONFIG_SQL_TABLE);
        if (table == null) {
            logger.warn("Missing TOML table [" + CONFIG_SQL_TABLE + "]");
            return false;
        }

        String url = table.getString("url");
        String username = table.getString("username");
        String password = table.getString("password");

        if (url == null || username == null || password == null) {
            logger.warn("SQL details were not properly set");
            return false;
        }

        try {
            URL databaseURL = new URL(url);
            if (databaseURL.getProtocol().startsWith("jdbc:")) {
                throw new MalformedURLException("Database URL is invalid.");
            }
            String protocol = databaseURL.getProtocol().substring(5);
            if (!SUPPORTED_PROTOCOLS.contains(protocol.toLowerCase())) {
                throw new MalformedURLException("Unsupported database protocol.");
            }
        } catch (MalformedURLException exception) {
            logger.info("Failed to validate database URL: " + exception.getMessage());
        }

        try {
            Class.forName(DRIVER);
        } catch (Exception exception) {
            logger.warn("JDBC Driver not properly established: {}", exception.getMessage());
            return false;
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            logger.info("Database connection established.");
            return true;
        } catch (SQLException exception) {
            logger.warn("Database connection could not be established: {}", exception.getMessage());
            return false;
        }
    }

    public static Connection getConnection() {
        return connection;
    }
    public List<String> getLinked() {
        return linked;
    }
    public RegisteredServer getServer(String server) {
        return proxy.get(server);
    }
}
