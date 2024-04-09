package io.github.helloandrewyan.relink.database;

public class SQLQueries {
    public static final String TABLE_NAME = "relink_table";
    public static final String UUID_COLUMN = "uuid";
    public static final String LAST_SERVER_COLUMN = "last_server";
    public static String TABLE_EXISTS_STATEMENT = "SELECT EXISTS ("
                + " SELECT 1 "
                + " FROM information_schema.TABLES "
                + " WHERE TABLE_SCHEMA = ? "
                + " AND TABLE_NAME = ?) AS table_exists;";

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
}
