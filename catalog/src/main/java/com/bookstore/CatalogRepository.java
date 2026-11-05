package com.bookstore;

import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class CatalogRepository {

    // Insert a new book normally
    public void insertBook(Book book) throws SQLException {
        String sql = """
                INSERT INTO books (id, title, topic, quantity, price)
                VALUES (?, ?, ?, ?, ?)
                """;

        executeInsert(book, sql);
    }

    // Insert initial books only if they don't already exist
    public void seedBooks() throws SQLException {
        String sql = """
                INSERT INTO books (id, title, topic, quantity, price)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(id) DO NOTHING
                """;

        Book[] books = {
                new Book(1, "How to get a good grade in DOS in 40 minutes a day",
                        "distributed systems", 10, 100),

                new Book(2, "RPCs for Noobs",
                        "distributed systems", 5, 50),

                new Book(3, "Xen and the Art of Surviving Undergraduate School",
                        "undergraduate school", 15, 75),

                new Book(4, "Cooking for the Impatient Undergrad",
                        "undergraduate school", 20, 60)
        };

        for (Book book : books) {
            executeInsert(book, sql);
        }
    }

    // Shared logic for executing an INSERT statement
    private void executeInsert(Book book, String sql) throws SQLException {
        try (var conn = Database.connect();
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getTopic());
            pstmt.setInt(4, book.getQuantity());
            pstmt.setInt(5, book.getPrice());

            pstmt.executeUpdate();
        }
    }

    public Book findBookById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (var conn = Database.connect();
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("topic"),
                            rs.getInt("quantity"),
                            rs.getInt("price"));
                } else {
                    return null; // Book not found
                }
            }
        }
    }

    public List<Book> findBooksByTopic(String topic) throws SQLException {

        String sql = "SELECT * FROM books WHERE topic = ?";

        List<Book> books = new ArrayList<>();

        try (var conn = Database.connect();
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, topic);

            try (var rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("topic"),
                            rs.getInt("quantity"),
                            rs.getInt("price")));
                }
            }
        }

        return books;
    }

    public boolean updatePrice(int id, int newPrice) throws SQLException {

        if (newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        String sql = "UPDATE books SET price = ? WHERE id = ?";

        try (var conn = Database.connect();
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newPrice);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected == 1;
        }
    }

    public boolean updateStock(int id, int change) throws SQLException {

        String sql = """
                UPDATE books
                SET quantity = quantity + ?
                WHERE id = ?
                AND quantity + ? >= 0
                """;

        try (var conn = Database.connect();
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, change);
            pstmt.setInt(2, id);
            pstmt.setInt(3, change);

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected == 1;
        }
    }

}