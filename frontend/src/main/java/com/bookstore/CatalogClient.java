package com.bookstore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CatalogClient {

    private final HttpClient client = HttpClient.newHttpClient();

    private static final String CATALOG_URL = "http://localhost:8081";

    public HttpResponse<String> getBookInfo(String id)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CATALOG_URL + "/info/" + id))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> searchBooks(String topic)
            throws IOException, InterruptedException {

        String encodedTopic = URLEncoder
                .encode(topic, StandardCharsets.UTF_8)
                .replace("+", "%20");

        String url = CATALOG_URL + "/search/" + encodedTopic;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }
}