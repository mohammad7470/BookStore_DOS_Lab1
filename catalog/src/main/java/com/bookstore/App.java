
package com.bookstore;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {

        port(5001);

        get("/hello", (request, response) -> {
            return "Hello from Catalog Service!";
        });

        System.out.println("Catalog Server is running...");
    }
}
