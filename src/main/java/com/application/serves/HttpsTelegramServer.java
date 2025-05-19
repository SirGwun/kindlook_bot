package com.application.serves;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Main;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Objects;

public class HttpsTelegramServer extends Server {
    int port = 8443;
    HttpsServer server;

    public HttpsTelegramServer() throws IOException, KeyManagementException, UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException {
        char[] password = Objects.requireNonNull(Main.getEnvVar("HTTPS_PAS")).toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fileInput = new FileInputStream(FileManager.getFilePatch("keystore.jks").toFile());
        ks.load(fileInput, password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);
        Arrays.fill(password, '*');

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress(port), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                try {
                    System.out.println("Настройка HTTPS-параметров");
                    SSLContext context = getSSLContext();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    params.setSSLParameters(context.getDefaultSSLParameters());
                } catch (Exception e) {
                    System.err.println("Ошибка конфигурации HTTPS: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        server.createContext("/webhook", new TelegramWebhookHandler());
        server.setExecutor(null);
        start();
    }

    @Override
    public void start() {
        server.start();
        System.out.println("HTTPS Сервер запущен на порту " + port);
    }
}
