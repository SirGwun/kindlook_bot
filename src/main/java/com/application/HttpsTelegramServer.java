package com.application;

import com.application.Controller.TelegramWebhookHandler;
import com.application.Model.InlineKeyboard;
import com.application.Model.User;
import com.application.serves.FileManager;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HttpsTelegramServer {
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
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        server.createContext("/webhook", new TelegramWebhookHandler());
        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("HTTPS Сервер запущен на порту " + port);
    }

    public static void sendMessage(User user, String text) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + Main.getEnvVar("TOKEN") + "/sendMessage";
        String jsonPayload = "{\"chat_id\":\"" + user.getId() + "\", \"text\":\"" + text + "\"}";
        sendJson(apiUrl, jsonPayload);
    }

    public static void sendMessageWithImages(User user, String text, List<String> imageList) throws IOException {
        if (imageList.isEmpty()) return;

        File imageFile = FileManager.getFilePatch(imageList.getFirst()).toFile();
        if (!imageFile.exists()) {
            throw new FileNotFoundException("Файл не найден: " + imageFile.getAbsolutePath());
        }

        String apiUrl = "https://api.telegram.org/bot" + Main.getEnvVar("TOKEN") + "/sendPhoto";
        String boundary = Long.toHexString(System.currentTimeMillis());

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            writer.append(String.valueOf(user.getId())).append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
            writer.append(text).append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"")
                    .append(imageFile.getName()).append("\"\r\n");
            writer.append("Content-Type: image/jpeg\r\n\r\n");
            writer.flush();

            Files.copy(imageFile.toPath(), output);
            output.flush();
            writer.append("\r\n").flush();

            writer.append("--").append(boundary).append("--\r\n").flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                responseCode < HttpURLConnection.HTTP_BAD_REQUEST ? conn.getInputStream() : conn.getErrorStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }



    public static void sendInlineKeyboard(InlineKeyboard keyboard, User user) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + Main.getEnvVar("TOKEN") + "/sendMessage";
        String jsonPayload = """
        {
            "chat_id": "%s",
            "text": "%s:",
            "reply_markup": %s
        }
        """.formatted(user.getId(), "Временное сообщение", keyboard.toJson());
        sendJson(apiUrl, jsonPayload);
    }

    public static void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + Main.getEnvVar("TOKEN") + "/answerCallbackQuery";

        String jsonPayload = String.format("""
            {
              "callback_query_id": "%s",
              "text": "%s",
              "show_alert": %s
            }
            """, callbackQueryId, text, showAlert);
        sendJson(apiUrl, jsonPayload);
    }

    public static void sendJson(String apiUrl, String jsonPayload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        InputStream responseStream = (responseCode < HttpURLConnection.HTTP_BAD_REQUEST)
                ? conn.getInputStream()
                : conn.getErrorStream();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(responseStream))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }

        System.out.println("Sent JSON: " + jsonPayload);
    }

}
