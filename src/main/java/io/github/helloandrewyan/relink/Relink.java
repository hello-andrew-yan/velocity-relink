package io.github.helloandrewyan.relink;

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
    private Toml config;
    private final Map<String, RegisteredServer> proxy = new HashMap<>();
    private static final String CONFIG_FILE = "config.toml";
    private static final String CONFIG_PROXY_TABLE = "proxy";
    private static final String CONFIG_SQL_TABLE = "sql";
    private static final String CONFIG_IGNORED = "ignored";
    private static final String CONFIG_LINKED = "linked";
    @Inject
    public Relink(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;
        this.directory = directory;
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        getProxy();
        this.config = loadConfig();
    }
    private void getProxy() {
        server.getAllServers().forEach(server -> {
            logger.info("Detected \"" + server.getServerInfo().getName() + "\" in proxy.");
            proxy.put(server.getServerInfo().getName(), server);
        });
    }
    private Toml loadConfig() {
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

    // TODO - Complete this defensive validation method.
    private boolean isValidConfig(Toml config) {
        if (config.isEmpty()) {
            logger.warn("Configuration file is empty.");
            return false;
        }
        if (!config.containsTable(CONFIG_PROXY_TABLE)) {
            logger.warn("Proxy table is missing.");
            return false;
        }

        List<String> ignored = config.getTable(CONFIG_PROXY_TABLE).getList(CONFIG_IGNORED);
        List<String> linked = config.getTable(CONFIG_PROXY_TABLE).getList(CONFIG_LINKED);
        if (ignored == null) {
            logger.warn("Proxy table is missing.");
            return false;
        }
        for (String server : ignored) {
            if (!proxy.containsKey(server)) {
                logger.warn("\"" + server + "\", does not exist in proxy. Ignoring.");
                return false;
            }
        }

        return ignored.stream().allMatch(proxy::containsKey) && linked.stream().allMatch(proxy::containsKey);
    }
}
