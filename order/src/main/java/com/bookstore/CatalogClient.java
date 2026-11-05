package com.bookstore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class CatalogClient {

    private final HttpClient client = HttpClient.newHttpClient();

    public HttpResponse<String> getBookInfo(int id)
            throws IOException, InterruptedException {

        String url = "http://localhost:8081/info/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> decreaseStock(int id)
            throws IOException, InterruptedException {

        String url = "http://localhost:8081/update/stock/" + id
                + "?change=-1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }
}