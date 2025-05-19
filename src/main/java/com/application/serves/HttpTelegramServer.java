package com.application.serves;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Main;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class HttpTelegramServer extends Server {
    HttpServer server;
    int port = 8080;
    public HttpTelegramServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/webhook", new TelegramWebhookHandler());
            server.setExecutor(null);
            start();
        } catch (IOException e) {
            Main.log(e.toString(), Level.WARNING);
        }
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }
}
