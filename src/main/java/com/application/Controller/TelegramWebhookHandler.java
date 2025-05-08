package com.application.Controller;

import com.application.Model.User;
import com.application.serves.DBProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

public class TelegramWebhookHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body;
        try (Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8).useDelimiter("\\A")) {
            body = scanner.hasNext() ? scanner.next() : "";
        }

        JsonNode node = objectMapper.readTree(body);
        processNode(node);

        try {
            System.out.println("Пришло сообщение от: " + node.path("message").path("from").path("username").asText());
            System.out.println(node.path("message").path("text").asText());
        } catch (NumberFormatException e) {
            System.out.println("Не удалось рапспарсить сообщение в отладочной вставке");
        }

        String response = "OK";
        exchange.sendResponseHeaders(200, response.length());
        try (exchange; OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void processNode(JsonNode node) {
        if (node.has("callback_query")) {
            ButtonHandler.handle(node);
        } else {
            CommandHandler.handle(node);
        }
    }

    private static User getOrCreateUser(long id, String userName) throws SQLException {
        User user = DBProxy.getUser(id);
        if (user == null) {
            user = new User(id, userName);
            DBProxy.addUser(user);
        }
        return user;
    }
}