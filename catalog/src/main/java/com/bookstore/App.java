package com.bookstore;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

import java.sql.SQLException;
//import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class App {
    public static void main(String[] args) throws SQLException {
        // Database.createBooksTable();
        // System.out.println("Catalog Service");

        // CatalogRepository catalogRepository = new CatalogRepository();
        // catalogRepository.seedBooks();

        // Book book = catalogRepository.findBookById(1);

        // if (book != null) {
        // System.out.println("Title: " + book.getTitle());
        // System.out.println("Price: " + book.getPrice());
        // System.out.println("Quantity: " + book.getQuantity());
        // }

        // boolean priceUpdated = catalogRepository.updatePrice(1, 80);
        // boolean stockUpdated = catalogRepository.updateStock(1, -1);

        // System.out.println("Price updated: " + priceUpdated);
        // System.out.println("Stock updated: " + stockUpdated);

        // Book updatedBook = catalogRepository.findBookById(1);

        // if (updatedBook != null) {
        // System.out.println("Updated Title: " + updatedBook.getTitle());
        // System.out.println("Updated Price: " + updatedBook.getPrice());
        // System.out.println("Updated Quantity: " + updatedBook.getQuantity());
        // }

        // // List<Book> books = catalogRepository.findBooksByTopic("distributed
        // systems");

        // // for (Book b : books) {
        // // System.out.println("Title: " + b.getTitle());
        // // System.out.println("Price: " + b.getPrice());
        // // System.out.println("Quantity: " + b.getQuantity());
        // // }

        Database.createBooksTable();

        CatalogRepository catalog = new CatalogRepository();
        catalog.seedBooks();

        Gson gson = new Gson();// to convert Java objects to JSON

        port(8081);// open port 8081 for the catalog service

        // Define a route to get book information by ID
        // the flow of the request is as follows:
        // 1. The client sends a GET request to the endpoint /info/:id,
        // where :id is a placeholder for the book ID.
        // 2. The server extracts the book ID from the request parameters.
        // 3. The server queries the catalog repository to find the book with the
        // specified ID.
        // 4. If the book is found, the server converts the book object to JSON
        // format and sends it back to the client as the response.
        get("/info/:id", (req, res) -> {

            res.type("application/json");

            int id;

            try {
                id = Integer.parseInt(req.params(":id"));
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\":\"Invalid book ID\"}";
            }

            Book book = catalog.findBookById(id);

            if (book == null) {
                res.status(404);
                return "{\"error\":\"Book not found\"}";
            }

            return gson.toJson(book);
        });

        get("/search/:topic", (req, res) -> {

            res.type("application/json");

            String topic = req.params(":topic");

            List<Book> books = catalog.findBooksByTopic(topic);

            List<Map<String, Object>> results = new ArrayList<>();

            for (Book book : books) {
                results.add(Map.of(
                        "id", book.getId(),
                        "title", book.getTitle()));
            }

            return gson.toJson(results);
        });

        // we will use terminal to test the update price endpoint, because
        // the browser does not allow us to send PUT requests directly. We can use the
        // following command in terminal:
        // curl -X PUT "http://localhost:8081/update/price/1?
        put("/update/price/:id", (req, res) -> {

            res.type("application/json");

            int id;
            int newPrice;

            try {
                id = Integer.parseInt(req.params(":id"));
                newPrice = Integer.parseInt(req.queryParams("price"));
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\":\"Invalid ID or price\"}";
            }

            if (newPrice < 0) {
                res.status(400);
                return "{\"error\":\"Price cannot be negative\"}";
            }

            boolean updated = catalog.updatePrice(id, newPrice);

            if (!updated) {
                res.status(404);
                return "{\"error\":\"Book not found\"}";
            }

            return "{\"message\":\"Price updated successfully\"}";
        });

        put("/update/stock/:id", (req, res) -> {

            res.type("application/json");

            int id;
            int change;

            try {
                id = Integer.parseInt(req.params(":id"));
                change = Integer.parseInt(req.queryParams("change"));
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\":\"Invalid ID or change value\"}";
            }

            boolean updated = catalog.updateStock(id, change);

            if (!updated) {
                res.status(409);
                return "{\"error\":\"Book not found or insufficient stock\"}";
            }

            return "{\"message\":\"Stock updated successfully\"}";
        });

        System.out.println("Catalog Service started on port 8081");
    }
}
