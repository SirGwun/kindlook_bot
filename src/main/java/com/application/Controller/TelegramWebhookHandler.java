package com.application.Controller;

import com.application.Model.User;
import com.application.serves.DBProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

public class TelegramWebhookHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("== Получен запрос ==");
        log.info("Метод: {}", exchange.getRequestMethod());
        log.info("URI: {}", exchange.getRequestURI());
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
            log.info("Пришло сообщение от: {}", node.path("message").path("from").path("username").asText());
            log.info(node.path("message").path("text").asText());
        } catch (NumberFormatException e) {
            log.warn("Не удалось рапспарсить сообщение в отладочной вставке");
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