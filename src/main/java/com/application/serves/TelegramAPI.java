package com.application.serves;

import com.application.Main;
import com.application.Model.InlineKeyboard;
import com.application.Model.Phrase;
import com.application.Model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TelegramAPI {
    private static final Logger log = LoggerFactory.getLogger(TelegramAPI.class);
    public static boolean sendPhrase(User user, Phrase phrase) throws IOException {
        if (!phrase.getImageNames().isEmpty()) {
            sendMessageWithImages(user, phrase.getText(), phrase.getImageNames());
        } else {
           sendMessage(user, phrase.getText());
        }
        return true;
    }

    public static void sendMessageWithImages(User user, String text, List<String> imageList) throws IOException {
        if (imageList.isEmpty()) return;

        File imageFile = FileManager.getImagePath(imageList.getFirst()).toFile();
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
        log.info("Response Code: {}", responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                responseCode < HttpURLConnection.HTTP_BAD_REQUEST ? conn.getInputStream() : conn.getErrorStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                log.info(line);
            }
        }
    }

    public static void sendMessage(User user, String text) throws IOException {
        String apiUrl = "https://api.telegram.org/bot" + Main.getEnvVar("TOKEN") + "/sendMessage";
        String jsonPayload = "{\"chat_id\":\"" + user.getId() + "\", \"text\":\"" + text + "\"}";
        sendJson(apiUrl, jsonPayload);
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

    public static void sendJson(String apiUrl, String jsonPayload) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        log.info("Response Code: {}", responseCode);

        InputStream responseStream = (responseCode < HttpURLConnection.HTTP_BAD_REQUEST)
                ? conn.getInputStream()
                : conn.getErrorStream();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(responseStream))) {
            String line;
            while ((line = in.readLine()) != null) {
                log.info(line);
            }
        }

        log.info("Sent JSON: {}", jsonPayload);
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
}
