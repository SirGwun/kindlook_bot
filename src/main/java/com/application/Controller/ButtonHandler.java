package com.application.Controller;

import com.application.Main;
import com.application.Model.*;
import com.application.serves.DBProxy;
import com.application.serves.Manager;
import com.application.serves.TelegramAPI;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ButtonHandler {
    private static final Logger log = LoggerFactory.getLogger(ButtonHandler.class);
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
            TelegramAPI.answerCallbackQuery(callbackId, "Принято", false);
            handleSubscribedUser(user, query);
        } catch (SQLException e) {
            log.error("Failed to get user from SQL: {}", id);
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error("IO failure with Telegram for user: {}", id);
            log.error(e.getMessage());
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
        TelegramAPI.answerCallbackQuery(callbackId, SubChecker.getShortSubText(), true);
        TelegramAPI.sendMessage(user, SubChecker.getSubText());
    }

    private static void handleSubscribedUser(User user, JsonNode query) throws IOException {
        String callbackData = query.path("data").asText();
        String callbackId = query.path("id").asText();

        Optional<Button> buttonOpt = Manager.getInstance().findButton(Integer.parseInt(callbackData));
        if (buttonOpt.isEmpty()) {
            TelegramAPI.answerCallbackQuery(callbackId, "Кнопка не найдена", true);
            return;
        }

        Button button = buttonOpt.get();

        if (button instanceof TopLevelButton topButton) {
            InlineKeyboard keyboard = new InlineKeyboard(topButton.getSubButtonList(), topButton.getText());
            TelegramAPI.sendInlineKeyboard(keyboard, user);
        } else {
            String text;
            try {
                Phrase phrase = Manager.getInstance().getNextPhrase(
                        Manager.getInstance().findButton(button.getId()).orElseThrow(()
                                -> new IllegalArgumentException("Button not found")), user);
                text = phrase != null ? phrase.getText() : "Фразы закончились.";
                if (phrase.getImageNames() != null && !phrase.getImageNames().isEmpty()) {
                    TelegramAPI.sendMessageWithImages(user, text, phrase.getImageNames());
                } else {
                    TelegramAPI.sendMessage(user, text);
                }
            } catch (SQLException e) {
                log.error("Failed to getNextPhrase from SQL: {}", user.getName());
                throw new RuntimeException(e);
            }
        }
    }

}
