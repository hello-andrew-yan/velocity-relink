package io.github.helloandrewyan.relink.database;

import io.github.helloandrewyan.relink.Relink;

import java.sql.*;
import java.util.UUID;

public class SQLExecutor {
    private Connection connection;

    public SQLExecutor(DatabaseManager databaseManager) {
        try {
            this.connection = databaseManager.getConnection();
            initialiseTable();
        } catch (NullPointerException exception) {
            Relink.getLogger().warn("Failed to create statement from connection: {}", exception.getMessage());
        }
    }
    private void initialiseTable() {
        try {
            PreparedStatement statement = connection.prepareStatement(SQLQueries.TABLE_EXISTS_STATEMENT);
            statement.setString(1, connection.getSchema());
            statement.setString(2, SQLQueries.TABLE_NAME);

            ResultSet resultSet = statement.executeQuery();
            statement.close();

            boolean tableExists = resultSet.next() && resultSet.getBoolean("table_exists");
            if (!tableExists) {
                Relink.getLogger().info("Relink table not found. Generating new table.");
                connection.createStatement().executeUpdate(SQLQueries.CREATE_TABLE_STATEMENT);
            } else {
                Relink.getLogger().info("Relink table found.");
            }

            resultSet.close();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to create table: {}", exception.getMessage());
        }
    }
    public void insertUserConnection(UUID uuid, String lastServer) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQLQueries.INSERT_DUPLICATE_KEY_STATEMENT);
            statement.setString(1, uuid.toString());
            statement.setString(2, lastServer);
            statement.executeQuery();
            statement.close();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to insert into table: {}", exception.getMessage());
        }
    }
    public void deleteUserConnection(UUID uuid) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQLQueries.DELETE_STATEMENT);
            statement.setString(1, uuid.toString());
            statement.executeQuery();
            statement.close();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to remove UUID from table: {}", exception.getMessage());
        }
    }

    public String getUserConnection(UUID uuid) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQLQueries.SELECT_STATEMENT);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            statement.close();

            if (resultSet.next()) {
                resultSet.close();
                return resultSet.getString(SQLQueries.UUID_COLUMN);
            }
            resultSet.close();
            return null;
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to insert into table: {}", exception.getMessage());
            return null;
        }
    }
}
