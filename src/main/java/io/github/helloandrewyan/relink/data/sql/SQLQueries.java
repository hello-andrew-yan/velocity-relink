package io.github.helloandrewyan.relink.data.sql;

import io.github.helloandrewyan.relink.Relink;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLQueries {
    private static final String TABLE_NAME = "relink";
    private static final String UUID_COLUMN = "uuid";
    private static final String LAST_SERVER_COLUMN = "last_server";
    public static String TABLE_EXISTS_STATEMENT = "SELECT EXISTS ("
            + " SELECT 1 "
            + " FROM information_schema.TABLES "
            + " WHERE TABLE_SCHEMA = ? "
            + " AND TABLE_NAME = '" + TABLE_NAME + "') AS table_exists;";

    public static String CREATE_TABLE_STATEMENT = "CREATE TABLE "
            + TABLE_NAME + " ("
            + UUID_COLUMN + " VARCHAR(64) NOT NULL, "
            + LAST_SERVER_COLUMN + " VARCHAR(64)  NOT NULL, "
            + "PRIMARY KEY (" + UUID_COLUMN + ")"
            + ");";
    public static String INSERT_DUPLICATE_KEY_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
                + UUID_COLUMN + ", "
                + LAST_SERVER_COLUMN + ") "
                + "VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + LAST_SERVER_COLUMN + " = "
                + "VALUES(" + LAST_SERVER_COLUMN + ");";
    public static String DELETE_STATEMENT = "DELETE FROM "
                + TABLE_NAME
                + " WHERE "
                + UUID_COLUMN
                + " = ?;";
    public static String SELECT_STATEMENT = "SELECT " + LAST_SERVER_COLUMN
                + " FROM " + TABLE_NAME
                + " WHERE " + UUID_COLUMN + " = ?;";

    public static String getUserConnection(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return resultSet.getString(SQLQueries.LAST_SERVER_COLUMN);
            }
            return null;
        } catch (SQLException exception) {
            Relink.getLogger().warn("Failed to query user connection: {}", exception.getMessage());
            return null;
        }
    }
}
