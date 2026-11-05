
package com.bookstore;

public class Book {

    // Book attributes
    private final int id;
    private final String title;
    private final String topic;
    private final int quantity;
    private final int price;

    // Constructor
    public Book(int id, String title, String topic,
            int quantity, int price) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Book ID must be positive");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Book title cannot be empty");
        }

        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(
                    "Book topic cannot be empty");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative");
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative");
        }

        this.id = id;
        this.title = title;
        this.topic = topic;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTopic() {
        return topic;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}
