package com.application.Controller;

import com.application.HttpsTelegramServer;
import com.application.Main;
import com.application.Model.*;
import com.application.TelegramBotAPI;
import com.application.serves.ButtonXmlLoader;
import com.application.serves.DBProxy;
import com.application.serves.Manager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public class BroadcastService {
    private static final User GLOBAL_USER = new User(-1, "GLOBAL");
    PhraseButton root;
    TelegramBotAPI api = new TelegramBotAPI();
    void startBroadcastScheduler(); // Запуск всей логики при старте бота
    long computeInitialDelayUntilNextBroadcast(ZoneOffset offset, LocalTime targetTime);
    private void sendBroadcastToAllUsers() {
        try {
            List<User> userList = DBProxy.getUsers();
            userList.remove(GLOBAL_USER);
            for (User user : userList) {
                api.sendPhrase(user, getTodayPhrase());
            }
        } catch (SQLException e) {
            Main.log(e.getMessage());
        } catch (IOException e) {
            Main.log(e.getMessage());
        }
    }
    boolean hasBroadcastBeenSentToday(LocalDate date); // Проверяет, была ли рассылка сегодня
    void markBroadcastAsSent(LocalDate date); // Отмечает, что рассылка выполнена
    LocalDate getTodayAtOffset(ZoneOffset offset); // Возвращает дату с учётом смещения UTC+3

    private Phrase getTodayPhrase() throws SQLException {
        if (root == null) {
            loadPhrases();
        }
        return Manager.getInstance().getNextPhrase(root, GLOBAL_USER.getId());
    }

    private void loadPhrases() {
        ButtonXmlLoader loader = new ButtonXmlLoader("broadcastData.xml");
        List<Button> phrases = loader.loadButtons();
        if (phrases.size() > 1) {
            throw new IllegalArgumentException("Больше одной кнопке в конфигурации рассылки");
        }
        Button button = phrases.getFirst();
        if (button instanceof TopLevelButton) {
            throw new IllegalArgumentException("Прочитана кнопка верхнего уровня на рассылке");
        }
        root = (PhraseButton) button;
        for (Phrase phrase : root.getPhraseList()) {
            try {
                DBProxy.insertPhrase(phrase.getText());
            } catch (SQLException e) {
                Main.log(e.getMessage());
            }
        }
    }
}
