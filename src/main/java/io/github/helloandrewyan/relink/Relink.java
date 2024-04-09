package io.github.helloandrewyan.relink;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<String, RegisteredServer> proxy = new HashMap<>();
    private final List<String> linked = new ArrayList<>();
    private static final String CONFIG_FILE = "config.toml";
    private static final String CONFIG_PROXY_TABLE = "proxy";
    private static final String CONFIG_SQL_TABLE = "sql";
    private static final String CONFIG_LINKED = "linked";
    @Inject
    public Relink(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;
        this.directory = directory;
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Loading plugin.");
        loadProxy();
        if (getConfig() == null || !validateProxy(getConfig()) || !loadDatabase(getConfig())) {
            logger.warn("Plugin could not be properly loaded.");
            return;
        }
    }
    private void loadProxy() {
        server.getAllServers().forEach(server -> proxy.put(server.getServerInfo().getName(), server));
    }
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
            Toml output = new Toml().read(configFile);
            if (output.isEmpty()) {
                logger.warn("Configuration file is empty.");
                return null;
            }
            return output;
        } catch (IOException exception) {
            logger.warn("Failed to read configuration file: " + exception.getMessage());
            return null;
        }
    }
    private boolean validateProxy(Toml config) {
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
                .forEach(server -> logger.warn("Could not find server \"" + server + "\". Ignoring."));
        linked.addAll(temp);
        return true;
    }
    private boolean loadDatabase(Toml config) {
        Toml table = config.getTable(CONFIG_SQL_TABLE);
        if (table == null) {
            logger.warn("Missing TOML table [" + CONFIG_SQL_TABLE + "]");
            return false;
        }

        String host = table.getString("host");
        Long port = table.getLong("port");
        String username = table.getString("username");
        String password = table.getString("password");
        String databaseName = table.getString("database");

        if (host == null || port == null || username == null || password == null || databaseName == null) {
            logger.warn("SQL details were not properly set");
            return false;
        }

        try {
            DatabaseOptions options = DatabaseOptions.builder().mysql(
                    username,
                    password,
                    databaseName,
                    host + ":" + port
            ).build();
            // Singleton Pattern. Can be referenced using .getGlobalDatabase()
            DB.setGlobalDatabase(
                    PooledDatabaseOptions.builder().options(options).createHikariDatabase()
            );
            logger.info("Connection to Database was successfully made.");
            return true;
        } catch (Exception exception) {
            logger.warn("Connection to Database was not successful: " + exception);
            return false;
        }
    }
}
