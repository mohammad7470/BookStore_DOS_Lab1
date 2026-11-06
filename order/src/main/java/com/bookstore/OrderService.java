package com.bookstore;

import java.io.IOException;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.SQLException;

public class OrderService {

    private final CatalogClient catalogClient;
    private final OrderRepository orderRepository = new OrderRepository();

    public OrderService(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    public String purchaseBook(int id)
            throws IOException, InterruptedException, SQLException {

        // 1. Validate book ID
        if (id <= 0) {
            return "Invalid book ID";
        }

        // 2. Request book information from Catalog
        HttpResponse<String> response = catalogClient.getBookInfo(id);

        if (response.statusCode() == 404) {
            return "Book not found";
        }

        if (response.statusCode() != 200) {
            return "Failed to retrieve book information";
        }

        // 3. Parse JSON response
        JsonObject book = JsonParser
                .parseString(response.body())
                .getAsJsonObject();

        String title = book.get("title").getAsString();
        int quantity = book.get("quantity").getAsInt();
        int price = book.get("price").getAsInt();

        // 4. Check availability
        if (quantity <= 0) {
            return "Book is out of stock";
        }

        // 5. Request stock decrease
        HttpResponse<String> updateResponse = catalogClient.decreaseStock(id);

        // 6. Verify the stock update
        if (updateResponse.statusCode() == 200) {

            try {

                if ("true".equalsIgnoreCase(
                        System.getenv("SIMULATE_ORDER_DB_FAILURE"))) {

                    throw new SQLException("Simulated order database failure");
                }

                orderRepository.saveOrder(id, title, price);

                return "bought book " + title;

            } catch (SQLException e) {

                // Attempt to restore the deducted stock
                try {
                    HttpResponse<String> restoreResponse = catalogClient.increaseStock(id);

                    if (restoreResponse.statusCode() == 200) {
                        System.err.println(
                                "Order save failed. Stock restored for book " + id);
                    } else {
                        System.err.println(
                                "CRITICAL: Stock restoration failed. HTTP "
                                        + restoreResponse.statusCode());
                    }

                } catch (IOException | InterruptedException restoreError) {

                    System.err.println(
                            "CRITICAL: Could not restore stock for book " + id);

                    e.addSuppressed(restoreError);

                    if (restoreError instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }

                throw e;
            }
        }

        if (updateResponse.statusCode() == 409) {
            return "Purchase failed: insufficient stock";
        }

        return "Purchase failed: stock update was unsuccessful";
    }
}
