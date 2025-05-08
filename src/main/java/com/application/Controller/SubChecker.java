package com.application.Controller;


import com.application.Main;
import com.application.Model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SubChecker {
    public static boolean isThisUserSubscribed(User user) throws IOException {
        String tocken = Main.getDotenv().get("TOKEN");
        String chanal = Main.getDotenv().get("CHANNEL_NAME");
        if (tocken == null || chanal == null) {
            throw new IOException("Не удалось прочитать env файл");
        }
        String url = "https://api.telegram.org/bot" + tocken
                + "/getChatMember?chat_id=" + URLEncoder.encode(chanal, StandardCharsets.UTF_8)
                + "&user_id=" + user.getId();

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        if (status != 200) {
            return false;
        }

        InputStream inputStream = connection.getInputStream();
        String responseJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(responseJson);

        String memberStatus = root.path("result").path("status").asText();
        return !memberStatus.equals("left") && !memberStatus.equals("kicked");
    }



    public static String getShortSubText() {
        return "Нет подписки не группу ***";
    }

    public static String getSubText() {
        return "Подпишитесь на группу ***";
    }
}
