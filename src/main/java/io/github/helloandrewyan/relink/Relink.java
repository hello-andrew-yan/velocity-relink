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
    private final List<String> ignored = new ArrayList<>();
    private final List<String> linked = new ArrayList<>();
    private static final String CONFIG_FILE = "config.toml";
    private static final String CONFIG_PROXY_TABLE = "proxy";
    private static final String CONFIG_SQL_TABLE = "sql";
    private static final String CONFIG_IGNORED = "ignored";
    private static final String CONFIG_LINKED = "linked";
    private Database database;
    @Inject
    public Relink(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;
        this.directory = directory;
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        getProxy();
        Toml config = getConfig();
        if (config == null || !validateProxy(config) || !validateSQL(config)) {
            logger.warn("Plugin could not be properly loaded.");
            return;
        }

    }
    private void getProxy() {
        server.getAllServers().forEach(server -> proxy.put(server.getServerInfo().getName(), server));
    }
    private Toml getConfig() {
        File configFile = new File(directory.toFile(), CONFIG_FILE);
        try (InputStream resource = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            if (resource == null) {
                logger.warn("Resource " + CONFIG_FILE + " not found. Contact developer.");
                return null;
            }
            // Create the directory if it doesn't exist.
            Files.createDirectories(configFile.getParentFile().toPath());

            if (!configFile.exists()) {
                // Copies the resource config file to new file.
                Files.copy(resource, configFile.toPath());
                logger.info("Copying new configuration file from resources.");
            }
            return new Toml().read(configFile);
        } catch (IOException exception) {
            logger.warn("Failed to read configuration file: " + exception.getMessage());
            return null;
        }
    }

    // TODO - DRAFT IMPLEMENTATION
    private boolean validateProxy(Toml config) {
        if (config.isEmpty()) {
            logger.warn("Configuration file is empty.");
            return false;
        }
        if (config.getTable(CONFIG_PROXY_TABLE) == null) {
           logger.warn("Missing TOML table [" + CONFIG_PROXY_TABLE + "]");
           return false;
        }

        List<String> temp = config.getTable(CONFIG_PROXY_TABLE).getList(CONFIG_IGNORED);
        if (temp == null) {
            logger.warn("Missing TOML list \"" + CONFIG_IGNORED + "\"");
            return false;
        }

        for (String server : temp) {
            if (!proxy.containsKey(server)) {
                logger.warn("Could not find server \"" + server + "\". Ignoring.");
                continue;
            }
            ignored.add(server);
        }

        temp = config.getTable(CONFIG_PROXY_TABLE).getList(CONFIG_LINKED);
        if (temp == null) {
            logger.warn("Missing TOML list \"" + CONFIG_LINKED + "\"");
            return false;
        }
        for (String server : temp) {
            if (!proxy.containsKey(server)) {
                logger.warn("Could not find server \"" + server + "\". Ignoring.");
                continue;
            }
            linked.add(server);
        }

        if (config.getTable(CONFIG_SQL_TABLE) == null) {
            logger.warn("Missing TOML table [" + CONFIG_SQL_TABLE + "]");
            return false;
        }
        return true;
    }

    // TODO - DRAFT IMPLEMENTATION
    private boolean validateSQL(Toml config) {
        String host = config.getTable(CONFIG_SQL_TABLE).getString("host");
        Long port = config.getTable(CONFIG_SQL_TABLE).getLong("port");
        String username = config.getTable(CONFIG_SQL_TABLE).getString("username");
        String password = config.getTable(CONFIG_SQL_TABLE).getString("password");
        String databaseName = config.getTable(CONFIG_SQL_TABLE).getString("database");

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
            database = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
            DB.setGlobalDatabase(database);

            logger.info("Connection to Database was successfully made.");
            return true;
        } catch (Exception exception) {
            logger.info("Connection to Database was not successful.");
            return false;
        }
    }
}
