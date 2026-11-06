package com.bookstore;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class Database {

    // private static final String DB_URL =
    // "jdbc:sqlite:../data/catalog/catalog.db";

    private static final String DB_URL = System.getenv().getOrDefault(
            "CATALOG_DB_URL",
            "jdbc:sqlite:../data/catalog/catalog.db");

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createBooksTable() throws SQLException {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "topic TEXT NOT NULL," +
                "quantity INTEGER NOT NULL CHECK (quantity >= 0)," +
                "price INTEGER NOT NULL CHECK (price >= 0)" +
                ");";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

}
