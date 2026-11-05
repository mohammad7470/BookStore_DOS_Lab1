package com.bookstore;

import java.io.IOException;
import java.net.http.HttpResponse;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {

        port(8080);

        CatalogClient catalog = new CatalogClient();
        OrderClient order = new OrderClient();

        get("/info/:id", (req, res) -> {

            res.type("application/json");

            try {
                HttpResponse<String> response = catalog.getBookInfo(req.params(":id"));

                res.status(response.statusCode());

                return response.body();

            } catch (IOException e) {
                res.status(502);
                return "{\"error\":\"Catalog service unavailable\"}";

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                res.status(503);
                return "{\"error\":\"Request interrupted\"}";
            }
        });

        get("/search/:topic", (req, res) -> {

            res.type("application/json");

            String topic = req.params(":topic");

            try {
                HttpResponse<String> response = catalog.searchBooks(topic);

                res.status(response.statusCode());

                return response.body();

            } catch (IOException e) {
                res.status(502);
                return "{\"error\":\"Catalog service unavailable\"}";

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                res.status(503);
                return "{\"error\":\"Request interrupted\"}";
            }
        });

        post("/purchase/:id", (req, res) -> {

            res.type("application/json");

            int id;

            try {
                id = Integer.parseInt(req.params(":id"));
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\":\"Invalid book ID\"}";
            }

            if (id <= 0) {
                res.status(400);
                return "{\"error\":\"Invalid book ID\"}";
            }

            try {
                HttpResponse<String> response = order.purchaseBook(id);

                res.status(response.statusCode());

                return response.body();

            } catch (IOException e) {
                res.status(502);
                return "{\"error\":\"Order service unavailable\"}";

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                res.status(503);
                return "{\"error\":\"Request interrupted\"}";
            }
        });

        System.out.println("Frontend started on port 8080");
    }
}