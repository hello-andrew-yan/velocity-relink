package io.github.helloandrewyan.relink.data.local;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.helloandrewyan.relink.Relink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public void insertUserConnection(UUID uuid, String lastServer) {
        Map<String, Object> data = dataFile.toMap();
        data.put(uuid.toString(), lastServer);

        TomlWriter writer = new TomlWriter();
        String update = writer.write(data);

        try {
            Files.write(Paths.get((directory.toString() + "/" + DATA_FILE)), update.getBytes());
            updateData();
        } catch (IOException exception) {
            Relink.getLogger().warn("Failed to insert into file: {}", exception.getMessage());
        }
    }

    public void deleteUserConnection(UUID uuid, String lastServer) {
        Map<String, Object> data = dataFile.toMap();
        data.remove(uuid.toString());

        TomlWriter writer = new TomlWriter();
        String update = writer.write(data);

        try {
            Files.write(Paths.get((directory.toString() + "/" + DATA_FILE)), update.getBytes());
            updateData();
        } catch (IOException exception) {
            Relink.getLogger().warn("Failed to insert into file: {}", exception.getMessage());
        }
    }

    private void updateData() {
        dataFile = getData();
    }

    public String getUserConnection(UUID uuid) {
        return dataFile.getString(uuid.toString());
    }
}
