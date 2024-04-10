package io.github.helloandrewyan.relink.data.sql;

import io.github.helloandrewyan.relink.Relink;

import java.sql.*;
import java.util.UUID;

public class SQLExecutor {
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    public SQLExecutor(DatabaseManager databaseManager) {
        try {
            this.connection = databaseManager.getConnection();
            if (connection != null) initialiseTable();
        } catch (NullPointerException exception) {
            Relink.getLogger().warn("Failed to create statement from connection: {}", exception.getMessage());
        }
    }

    private void closeResources() {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to close database resources: {}", exception.getMessage());
        }
    }

    private void initialiseTable() {
        try {
            statement = connection.prepareStatement(SQLQueries.TABLE_EXISTS_STATEMENT);
            statement.setString(1, connection.getCatalog());
            resultSet = statement.executeQuery();

            boolean tableExists = resultSet.next() && resultSet.getBoolean("table_exists");
            if (!tableExists) {
                Relink.getLogger().info("Relink table not found. Generating new table.");
                connection.createStatement().executeUpdate(SQLQueries.CREATE_TABLE_STATEMENT);
            } else {
                Relink.getLogger().info("Relink table found.");
            }
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to create table: {}", exception.getMessage());
        } finally {
            closeResources();
        }
    }

    public void insertUserConnection(UUID uuid, String lastServer) {
        try {
            statement = connection.prepareStatement(SQLQueries.INSERT_DUPLICATE_KEY_STATEMENT);
            statement.setString(1, uuid.toString());
            statement.setString(2, lastServer);
            statement.executeUpdate();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to insert into table: {}", exception.getMessage());
        } finally {
            closeResources();
        }
    }

    public void deleteUserConnection(UUID uuid) {
        try {
            statement = connection.prepareStatement(SQLQueries.DELETE_STATEMENT);
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to remove UUID from table: {}", exception.getMessage());
        } finally {
            closeResources();
        }
    }

    public String getUserConnection(UUID uuid) {
        try {
            statement = connection.prepareStatement(SQLQueries.SELECT_STATEMENT);
            statement.setString(1, uuid.toString());
            resultSet = statement.executeQuery();

            return SQLQueries.getUserConnection(resultSet);
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to query user connection: {}", exception.getMessage());
            return null;
        } finally {
            closeResources();
        }
    }
}
