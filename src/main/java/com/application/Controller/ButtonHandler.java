package com.application.Controller;

import com.application.HttpsTelegramServer;
import com.application.Main;
import com.application.Model.*;
import com.application.serves.DBProxy;
import com.application.serves.Manager;
import com.fasterxml.jackson.databind.JsonNode;
import org.w3c.dom.Node;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.spi.ToolProvider;

public class ButtonHandler {
    public static void handle(JsonNode node) {
        JsonNode query = node.get("callback_query");
        JsonNode from = query.path("from");

        long id = Long.parseLong(from.path("id").asText());
        String userName = from.path("first_name").asText();
        String callbackId = query.path("id").asText();

        try {
            User user = getOrCreateUser(id, userName);

            if (!SubChecker.isThisUserSubscribed(user)) {
                handleUnsubscribedUser(user, callbackId);
                return;
            }
            HttpsTelegramServer.answerCallbackQuery(callbackId, "Принято", false);
            handleSubscribedUser(user, query);
        } catch (SQLException e) {
            Main.log("Failed to get user from SQL: " + id);
            Main.log(e.getMessage());
        } catch (IOException e) {
            Main.log("IO failure with Telegram for user: " + id);
            Main.log(e.getMessage());
        }
    }


    private static User getOrCreateUser(long id, String userName) throws SQLException {
        User user = DBProxy.getUser(id);
        if (user == null) {
            user = new User(id, userName);
            DBProxy.addUser(user);
        }
        return user;
    }

    private static void handleUnsubscribedUser(User user, String callbackId) throws IOException {
        HttpsTelegramServer.answerCallbackQuery(callbackId, SubChecker.getShortSubText(), true);
        HttpsTelegramServer.sendMessage(user, SubChecker.getSubText());
    }

    private static void handleSubscribedUser(User user, JsonNode query) throws IOException {
        String callbackData = query.path("data").asText();
        String callbackId = query.path("id").asText();

        Optional<Button> buttonOpt = Manager.getInstance().findButton(Integer.parseInt(callbackData));
        if (buttonOpt.isEmpty()) {
            HttpsTelegramServer.answerCallbackQuery(callbackId, "Кнопка не найдена", true);
            return;
        }

        Button button = buttonOpt.get();

        if (button instanceof TopLevelButton topButton) {
            InlineKeyboard keyboard = new InlineKeyboard(topButton.getSubButtonList(), topButton.getText());
            HttpsTelegramServer.sendInlineKeyboard(keyboard, user);
        } else {
            String text;
            try {
                Phrase phrase = Manager.getInstance().getNextPhrase(
                        Manager.getInstance().findButton(button.getId()).orElseThrow(()
                                -> new IllegalArgumentException("Button not found")), user.getId());
                text = phrase != null ? phrase.getText() : "Фразы закончились.";
                if (phrase.getImagePaths() != null && !phrase.getImagePaths().isEmpty()) {
                    HttpsTelegramServer.sendMessageWithImages(user, text, phrase.getImagePaths());
                } else {
                    HttpsTelegramServer.sendMessage(user, text);
                }
            } catch (SQLException e) {
                Main.log("Failed to getNextPhrase from SQL: " + user.getName());
                throw new RuntimeException(e);
            }
        }
    }

}
