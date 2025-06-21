package com.application.serves;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Main;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(HttpsTelegramServer.class);
    int port = 8080;
    HttpsServer server;

    public HttpsTelegramServer() throws IOException, KeyManagementException, UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException {
        char[] password = Objects.requireNonNull(Main.getEnvVar("HTTPS_PAS")).toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fileInput = new FileInputStream(FileManager.getDataFilePath("keystore.jks").toFile());
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
                    log.info("Настройка HTTPS-параметров");
                    SSLContext context = getSSLContext();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    params.setSSLParameters(context.getDefaultSSLParameters());
                } catch (Exception e) {
                    log.error("Ошибка конфигурации HTTPS: {}", e.getMessage(), e);
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
        log.info("HTTPS Сервер запущен на порту {}", port);
    }
}
