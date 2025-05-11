package com.application;

import com.application.Controller.BroadcastService;
import com.application.serves.DBProxy;
import com.application.serves.Manager;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.logging.Level;


public class Main {
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static HttpsTelegramServer server;

    public static void main(String[] args) {
        try {
            Manager.getInstance();
            server = new HttpsTelegramServer();
            server.start();

//            BroadcastService service = new BroadcastService();
//            service.startBroadcastScheduler();
        } catch (UnrecoverableKeyException e) {
            log("Невозможно восстановить ключ из хранилища: " + e.getMessage(), Level.SEVERE);
        } catch (CertificateException e) {
            log("Ошибка при загрузке сертификата: " + e.getMessage(), Level.SEVERE);
        } catch (IOException e) {
            log("Ошибка ввода-вывода при запуске HTTPS сервера: " + e.getMessage(), Level.SEVERE);
        } catch (KeyStoreException e) {
            log("Ошибка при работе с хранилищем ключей: " + e.getMessage(), Level.SEVERE);
        } catch (NoSuchAlgorithmException e) {
            log("Указанный алгоритм шифрования не поддерживается: " + e.getMessage(), Level.SEVERE);
        } catch (KeyManagementException e) {
            log("Ошибка инициализации SSL контекста: " + e.getMessage(), Level.SEVERE);
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

    public static void log(String str, Level level) {
        System.out.println(str);
    }
}