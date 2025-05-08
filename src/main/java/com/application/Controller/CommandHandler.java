package com.application.Controller;

import com.application.HttpsTelegramServer;
import com.application.Main;
import com.application.Model.InlineKeyboard;
import com.application.Model.User;
import com.application.serves.DBProxy;
import com.application.serves.Manager;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.sql.SQLException;

public class CommandHandler {
    public static void handle(JsonNode node) {
        String userId = node.path("message").path("chat").path("id").asText();
        String message = node.path("message").path("text").asText();
        String userName = node.path("message").path("from").path("username").asText();

        User user;
        try {
            user = getOrCreateUser(Long.parseLong(userId), userName);
            if (!SubChecker.isThisUserSubscribed(user)) {
                HttpsTelegramServer.sendMessage(user, SubChecker.getSubText());
                return;
            }

            InlineKeyboard keyboard;
            if (message.startsWith("/start")) {
                keyboard = new InlineKeyboard(Manager.getInstance().getRootList(), Manager.getInstance().getStartMessage());
            } else {
                keyboard = new InlineKeyboard(Manager.getInstance().getRootList(), Manager.getInstance().getUnknownMessage());
            }
            HttpsTelegramServer.sendInlineKeyboard(keyboard, user);
        } catch (SQLException | IOException e) {
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
}
