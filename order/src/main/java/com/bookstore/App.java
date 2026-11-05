package com.bookstore;

import static spark.Spark.port;
import static spark.Spark.post;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {

    public static void main(String[] args) throws Exception {

        // CatalogClient catalog = new CatalogClient();

        // HttpResponse<String> response = catalog.getBookInfo(2);

        // if (response.statusCode() != 200) {
        // System.out.println("Catalog request failed: " + response.body());
        // return;
        // }

        // JsonObject book = JsonParser.parseString(response.body())
        // .getAsJsonObject();

        // String title = book.get("title").getAsString();
        // int quantity = book.get("quantity").getAsInt();
        // int price = book.get("price").getAsInt();

        // System.out.println("Book: " + title);
        // System.out.println("Price: " + price);
        // System.out.println("Available quantity: " + quantity);

        // if (quantity > 0) {
        // System.out.println("Book is available for purchase.");
        // } else {
        // System.out.println("Book is out of stock.");
        // }

        // Database.createOrdersTable();
        // System.out.println("Order database initialized successfully.");

        // if (args.length != 1) {
        // System.out.println("Please provide a book ID.");
        // return;
        // }

        // int id = Integer.parseInt(args[0]);

        // OrderService orderService = new OrderService(new CatalogClient());

        // System.out.println(orderService.purchaseBook(id));

        // Initialize Order database
        Database.createOrdersTable();

        // Create service objects
        CatalogClient catalogClient = new CatalogClient();
        OrderService orderService = new OrderService(catalogClient);

        Gson gson = new Gson();

        // Order Service port
        port(8082);

        // Purchase endpoint
        post("/purchase/:id", (req, res) -> {

            res.type("application/json");

            int id;

            try {
                id = Integer.parseInt(req.params(":id"));
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid book ID"));
            }

            if (id <= 0) {
                res.status(400);
                return gson.toJson(Map.of("error", "Invalid book ID"));
            }

            try {
                String result = orderService.purchaseBook(id);

                if (result.startsWith("bought book ")) {
                    res.status(200);
                    return gson.toJson(Map.of("message", result));
                }

                if (result.equals("Book not found")) {
                    res.status(404);
                } else if (result.equals("Book is out of stock")
                        || result.equals("Purchase failed: insufficient stock")) {
                    res.status(409);
                } else {
                    res.status(502);
                }

                return gson.toJson(Map.of("error", result));

            } catch (IOException e) {
                res.status(502);
                return gson.toJson(Map.of("error", "Catalog communication failed"));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                res.status(503);
                return gson.toJson(Map.of("error", "Request interrupted"));

            } catch (SQLException e) {
                res.status(500);
                return gson.toJson(Map.of(
                        "error", "Order database error; check records before retrying"));
            }
        });

        System.out.println("Order Service started on port 8082");
    }
}