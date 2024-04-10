package io.github.helloandrewyan.relink.data.sql;

import io.github.helloandrewyan.relink.Relink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    public DatabaseManager(String url, String username, String password) {
        loadDatabase(url, username, password);
    }
    private void loadDatabase(String url, String username, String password) {
        try {
            Class.forName(DRIVER_CLASS);
            connection = DriverManager.getConnection(url, username, password);
            Relink.getLogger().info("Database connection established.");
        } catch (ClassNotFoundException | SQLException exception) {
            Relink.getLogger().warn("Database connection could not be established: {}", exception.getMessage());
        }
    }
    public Connection getConnection() {
        return connection;
    }
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            Relink.getLogger().warn("Connection failed to close: {}", exception.getMessage());
        }
    }
}
