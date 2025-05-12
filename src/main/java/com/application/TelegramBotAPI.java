package com.application;

import com.application.Model.Phrase;
import com.application.Model.User;

import java.io.IOException;

public class TelegramBotAPI {
    public boolean sendPhrase(User user, Phrase phrase) throws IOException {
        if (!phrase.getImageNames().isEmpty()) {
            HttpsTelegramServer.sendMessageWithImages(user, phrase.getText(), phrase.getImageNames());
        } else {
            HttpsTelegramServer.sendMessage(user, phrase.getText());
        }
        return true;
    }
}
