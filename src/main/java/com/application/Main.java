package com.application;

import com.application.serves.Manager;
import io.github.cdimascio.dotenv.Dotenv;


public class Main {
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static Manager manager;
    private static HttpsTelegramServer server;

    public static void main(String[] args) {
        try {
            server = new HttpsTelegramServer();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Dotenv getDotenv() {
        return dotenv;
    }

    public static HttpsTelegramServer getServer() {
        return server;
    }

    public static void log(String str) {
        System.out.println(str);
    }
}