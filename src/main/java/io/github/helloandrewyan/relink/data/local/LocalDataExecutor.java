package io.github.helloandrewyan.relink.data.local;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.helloandrewyan.relink.Relink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class LocalDataExecutor {
    private final Path directory;
    private Toml dataFile;
    private static final String DATA_FILE = "data.toml";

    public LocalDataExecutor(Path directory) {
        this.directory = directory;
        this.dataFile = getData();
    }

    private Toml getData() {
        try {
            File configFile = new File(directory.toFile(), DATA_FILE);
            Files.createDirectories(configFile.getParentFile().toPath());
            if (!configFile.exists()) {
                Relink.getLogger().info("Creating new data file");
                Files.createFile(configFile.toPath());
            }
            return new Toml().read(configFile);
        } catch (IOException exception) {
            Relink.getLogger().warn("Failed to read data file: {}", exception.getMessage());
            return null;
        }
    }

    public void insertUserConnection(UUID uuid, String lastServer) {
        Map<String, Object> data = dataFile.toMap();
        data.put(uuid.toString(), lastServer);
        updateDataFile(data);
    }

    public void deleteUserConnection(UUID uuid, String lastServer) {
        Map<String, Object> data = dataFile.toMap();
        data.remove(uuid.toString());
        updateDataFile(data);
    }

    private void updateDataFile(Map<String, Object> data) {
        TomlWriter writer = new TomlWriter();
        String update = writer.write(data);

        try {
            Files.write(Paths.get((directory.toString() + "/" + DATA_FILE)), update.getBytes());
            refreshData();
        } catch (IOException exception) {
            Relink.getLogger().warn("Failed to perform edit on file: {}", exception.getMessage());
        }
    }

    private void refreshData() {
        dataFile = getData();
    }

    public String getUserConnection(UUID uuid) {
        return dataFile.getString(uuid.toString());
    }
}
