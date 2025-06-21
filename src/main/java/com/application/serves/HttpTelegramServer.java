package com.application.serves;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Main;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTelegramServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(HttpTelegramServer.class);
    HttpServer server;
    int port = 8080;
    public HttpTelegramServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/webhook", new TelegramWebhookHandler());
            server.setExecutor(null);
            start();
        } catch (IOException e) {
            log.warn(e.toString());
        }
    }

    public void start() {
        server.start();
        log.info("HTTP-сервер запущен на порту {}", port);
    }
}
