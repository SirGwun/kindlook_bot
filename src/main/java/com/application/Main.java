package com.application;

import com.application.Controller.SystemEnvProxy;
import com.application.serves.HttpTelegramServer;
import com.application.serves.HttpsTelegramServer;
import com.application.serves.Manager;
import com.application.serves.Server;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;


public class Main {
    private static Server server;

    public static void main(String[] args) {
//        try {
//            Manager.getInstance();
//            server = new HttpsTelegramServer();
//        } catch (UnrecoverableKeyException e) {
//            log("Невозможно восстановить ключ из хранилища: " + e.getMessage(), Level.SEVERE);
//        } catch (CertificateException e) {
//            log("Ошибка при загрузке сертификата: " + e.getMessage(), Level.SEVERE);
//        } catch (IOException e) {
//            log("Ошибка ввода-вывода при запуске HTTPS сервера: " + e.getMessage(), Level.SEVERE);
//        } catch (KeyStoreException e) {
//            log("Ошибка при работе с хранилищем ключей: " + e.getMessage(), Level.SEVERE);
//        } catch (NoSuchAlgorithmException e) {
//            log("Указанный алгоритм шифрования не поддерживается: " + e.getMessage(), Level.SEVERE);
//        } catch (KeyManagementException e) {
//            log("Ошибка инициализации SSL контекста: " + e.getMessage(), Level.SEVERE);
//        }
        Manager.getInstance();
        server = new HttpTelegramServer();
    }

    public static String getEnvVar(String var) {
        if (inAmvera()) {
            SystemEnvProxy proxy = new SystemEnvProxy();
            return proxy.get(var);
        } else {
            Dotenv dotenv = Dotenv.configure().load();
            return dotenv.get(var);
        }
    }

    public static boolean inAmvera() {
        String amv = System.getenv("AMVERA");
        if (amv == null) {
            return false;
        }
        return Integer.parseInt(amv) == 1;
    }

    public static Server getServer() {
        return server;
    }

    public static void log(String str) {
        System.out.println(str);
    }

    public static void log(String str, Level level) {
        System.out.println(str);
    }
}