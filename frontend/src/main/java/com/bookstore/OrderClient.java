package com.bookstore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OrderClient {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String ORDER_URL = "http://localhost:8082";

    public HttpResponse<String> purchaseBook(int id)
            throws IOException, InterruptedException {

        String url = ORDER_URL + "/purchase/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }
}