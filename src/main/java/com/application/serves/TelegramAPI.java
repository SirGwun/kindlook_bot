package com.application.serves;

import com.application.Main;
import com.application.Model.InlineKeyboard;
import com.application.Model.Phrase;
import com.application.Model.User;
import com.application.View.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TelegramAPI {
    public static boolean sendPhrase(User user, Phrase phrase) throws IOException {
        if (!phrase.getImageNames().isEmpty()) {
            sendMessageWithImages(user, phrase.getText(), phrase.getImageNames());
        } else {
           sendMessage(user, phrase.getText());
        }
        return true;
    }

    public static void sendMessageWithImages(User user, String text, List<String> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            Logger.log("Вызов метода sendMessageWithImages, без картинок", Level.WARNING);
            return;
        }

        try {
            List<Path> imagePaths = FileManager.getImagePaths(imageList);
            HttpURLConnection conn = createTelegramPhotoRequest(Main.getEnvVar("TOKEN"), user, text, imagePaths);
            int responseCode = conn.getResponseCode();
            Logger.logResponse(conn, responseCode);
            closeResponse(conn);
        } catch (IOException e) {
            Logger.log("Exception with sending imagers: " + e, Level.WARNING);
        }

    }


    private static HttpURLConnection createTelegramPhotoRequest(String token, User user, String text, List<Path> imagePaths) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + token + "/sendMediaGroup");
        String boundary = Long.toHexString(System.currentTimeMillis());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream()) {
            writeFormatField(out, boundary, "chat_id", String.valueOf(user.getId()));

            String mediaNamesJson = buildMediaNamesJson(imagePaths, text, "photo");
            writeFormatField(out, boundary, "media", mediaNamesJson);

            for (Path path : imagePaths) {
                String mimeType = Files.probeContentType(path);
                if (mimeType == null || !(mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
                    throw new IllegalArgumentException("Unsupported file type for sendMediaGroup: " + path);
                }
                writeFilePart(out, boundary, path, mimeType);
            }

            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
        return conn;
    }

    private static void writeFormatField(OutputStream out, String boundary, String fieldName, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFilePart(OutputStream out, String boundary, Path filePath, String mimeType) throws IOException {
        String fileName = filePath.getFileName().toString();
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"media[]\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        Files.copy(filePath, out);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static String buildMediaNamesJson(List<Path> mediaFileList, String text, String mediaType) throws JsonProcessingException {
        List<Map<String, Object>> mediaList = new ArrayList<>();

        for (int i = 0; i < mediaFileList.size(); i++) {
            String fileName = mediaFileList.get(i).getFileName().toString();
            Map<String, Object> item = new HashMap<>();
            item.put("type", mediaType);
            item.put("media", "attach://" + fileName);
            if (i == 0 && text != null && !text.isEmpty()) {
                item.put("caption", text);
            }
            mediaList.add(item);
        }
        return new ObjectMapper().writeValueAsString(mediaList);
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

    private static void closeResponse(HttpURLConnection conn) {
        InputStream responseStream = null;
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                responseStream = conn.getInputStream();
            } else {
                responseStream = conn.getErrorStream();
            }
            if (responseStream != null) {
                byte[] buffer = new byte[1024];
                while (responseStream.read(buffer) != -1) { }
            }
        } catch (IOException ignored) {
            // Можно логировать, если нужно
        } finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException ignored) { }
            }
        }
    }
}
