package com.application;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Model.Phrase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Scanner;

public class HttpsTelegramServer {
    int port = 8443;
    HttpsServer server;


    public HttpsTelegramServer() throws Exception {
        char[] password = Main.getDotenv().get("HTTPS_PAS").toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fileInput = new FileInputStream("keystore.jks");
        ks.load(fileInput, password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress(port), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        server.createContext("/webhook", new TelegramWebhookHandler());
        server.setExecutor(null);
    }

    public static void sendMessage(String phrase) {
    }

    public void start() {
        server.start();
        System.out.println("HTTPS Сервер запущен на порту " + port);
    }

    public void sendTestMessage(String chatId, String text) throws Exception {
        String apiUrl = "https://api.telegram.org/bot" + Main.getDotenv().get("TOKEN") + "/sendMessage";
        String jsonPayload = "{\"chat_id\":\"" + chatId + "\", \"text\":\"" + text + "\"}";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] out = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(out, 0, out.length);
        }

        System.out.println("Ответ от Telegram: " + conn.getResponseCode());
    }

    public static void sendInlineKeyboard(String chatId) throws Exception {
        String apiUrl = "https://api.telegram.org/bot" + Main.getDotenv().get("TOKEN") + "/sendMessage";
        String jsonPayload = """
        {
            "chat_id": "%s",
            "text": "Choose an option:",
            "reply_markup": {
                "keyboard": [
                    [{"text": "Option 1"}, {"text": "Option 2"}],
                    [{"text": "Option 3"}, {"text": "Option 4"}]
                ],
                "resize_keyboard": true,
                "one_time_keyboard": true
            }
        }
        """.formatted(chatId);


        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        System.out.println("Ответ от Telegram: " + conn.getResponseCode());
    }
}
