package com.bookstore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderRepository {

    public void saveOrder(int bookId, String title, int price)
            throws SQLException {

        String sql = """
                INSERT INTO orders (book_id, title, price)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            pstmt.setString(2, title);
            pstmt.setInt(3, price);

            pstmt.executeUpdate();
        }
    }
}