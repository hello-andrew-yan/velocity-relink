package io.github.helloandrewyan.relink.data.local;

import com.moandjiezana.toml.Toml;
import io.github.helloandrewyan.relink.Relink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class LocalDataManager {
    private final Path directory;
    private final Toml dataFile;
    private static final String DATA_FILE = "data.toml";

    public LocalDataManager(Path directory) {
        this.directory = directory;
        this.dataFile = getData();
    }
    // TODO - Remove duplicated code instance in Relink and LocalDataManager.
    //        Pull method into separate file managing class.
    private Toml getData() {
        File configFile = new File(directory.toFile(), DATA_FILE);
        try (InputStream resource = getClass().getResourceAsStream("/" + DATA_FILE)) {
            if (resource == null) {
                Relink.getLogger().warn("Resource {} not found. Contact developer.", DATA_FILE);
                return null;
            }
            Files.createDirectories(configFile.getParentFile().toPath());
            if (!configFile.exists()) {
                Files.copy(resource, configFile.toPath());
                Relink.getLogger().info("Copying new data file from resources.");
            }
            return new Toml().read(configFile);
        } catch (IOException exception) {
            Relink.getLogger().warn("Failed to read data file: {}", exception.getMessage());
            return null;
        }
    }

    private boolean validateData() {
        Toml table = dataFile.getTable("users");
        if (table == null) return false;
        try {
            table.toMap();
            return true;
        // TODO - Check which exceptions are caught.
        } catch (Exception exception) {
            Relink.getLogger().warn("Failed to validate users: {}", exception.getMessage());
            return false;
        }
    }

    public void insertUserConnection(UUID uuid, String lastServer) {

    }

}
