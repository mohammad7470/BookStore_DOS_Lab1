package com.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:../data/order/orders.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createOrdersTable() throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    price INTEGER NOT NULL CHECK (price >= 0),
                    ordered_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
        }
    }
}