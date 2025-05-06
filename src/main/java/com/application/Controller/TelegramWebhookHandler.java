package com.application.Controller;

import com.application.HttpsTelegramServer;
import com.application.Model.Button;
import com.application.Model.User;
import com.application.serves.Manager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TelegramWebhookHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            System.out.println("работает функция handle, метод не POST");
            return;
        }
        System.out.println("работает функция handle");
        // 2. Читаем тело запроса (JSON от Telegram)
        InputStream requestBody = exchange.getRequestBody();

        String body = new Scanner(requestBody, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        JsonNode node = objectMapper.readTree(body);
        String chatID = node.path("message").path("chat").path("id").asText();
        String message = node.path("message").path("text").asText();
        String userName = node.path("message").path("from").path("username").asText();

        if (message.startsWith("/")) {
            commandHandler(message, chatID);
        } else {
            System.out.println("Пришло обновление от: " + chatID + " - " + userName);
            System.out.println("Текст: " + message);
            User user = new User(Long.parseLong(chatID), userName);

            Manager phraseManager = Manager.getInstance();
//            Phrase phrase = phraseManager.getNextPhrase(button, user);
//
//            HttpsTelegramServer.sendMessage(phrase.getText());
        }

        String response = "OK";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void commandHandler(String message, String chatID) {
        String[] command = message.split(" ");

        switch (command[0]) {
            case "/start": {
                try {
                    HttpsTelegramServer.sendInlineKeyboard(chatID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            default: {
                HttpsTelegramServer.sendMessage("Неизвестная команда");
            }
        }
    }
}