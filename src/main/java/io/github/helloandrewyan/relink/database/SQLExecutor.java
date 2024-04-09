package io.github.helloandrewyan.relink.database;

import io.github.helloandrewyan.relink.Relink;

import java.sql.*;

public class SQLExecutor {
    private final Connection connection;
    private Statement statement;
    public SQLExecutor(DatabaseManager databaseManager) {
        this.connection = databaseManager.getConnection();
        try {
            this.statement = databaseManager.getConnection().createStatement();
            initialiseTable();
        } catch (NullPointerException | SQLException exception) {
            Relink.getLogger().warn("Failed to create statement from connection: {}", exception.getMessage());
        }
    }
    private void initialiseTable() {
        if (statement == null) return;
        try {
            PreparedStatement tableExistsStatement = connection.prepareStatement(SQLQueries.TABLE_EXISTS_STATEMENT);
            tableExistsStatement.setString(1, connection.getSchema());
            tableExistsStatement.setString(2, "relink");

            ResultSet resultSet = tableExistsStatement.executeQuery();
            boolean tableExists = resultSet.next() && resultSet.getBoolean("table_exists");
            if (!tableExists) {
                Relink.getLogger().info("Relink table not found. Generating new table.");
                connection.createStatement().executeUpdate(SQLQueries.CREATE_TABLE_STATEMENT);
            } else {
                Relink.getLogger().info("Relink table found.");
            }
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to create table: {}", exception.getMessage());
        }
    }
}
