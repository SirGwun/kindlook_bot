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
import java.time.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.application.serves.DBProxy.addBroadcastMark;
import static com.application.serves.DBProxy.getBroadcastMark;

public class BroadcastService {
    private static final User GLOBAL_USER = new User(-1, "GLOBAL");
    PhraseButton root;
    TelegramBotAPI api = new TelegramBotAPI();
    public void startBroadcastScheduler() {
        ZoneOffset offset = ZoneOffset.of("+3");
        LocalTime targetTime = LocalTime.of(12, 0);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        long initialDelay = computeInitialDelayUntilNextBroadcast(offset, targetTime);

        scheduler.scheduleAtFixedRate(() -> {
            LocalDate today = getTodayAtOffset(offset);
            if (!getBroadcastMark(today)) {
                sendBroadcastToAllUsers();
                addBroadcastMark(today);
            }
        }, initialDelay, Duration.ofDays(1).toMillis(), TimeUnit.MILLISECONDS);
    }


    public long computeInitialDelayUntilNextBroadcast(ZoneOffset offset, LocalTime targetTime) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(offset);
        ZonedDateTime nextBroadcast = now.withHour(targetTime.getHour())
                .withMinute(targetTime.getMinute())
                .withSecond(0)
                .withNano(0);

        if (now.isAfter(nextBroadcast)) {
            nextBroadcast = nextBroadcast.plusDays(1);
        }

        Duration delay = Duration.between(now, nextBroadcast);
        return delay.toMillis();
    }

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
    boolean hasBroadcastBeenSentToday(LocalDate date) {
        return getBroadcastMark(date);
    }
    void markBroadcastAsSent(LocalDate date) {
        addBroadcastMark(date);
    }
    public LocalDate getTodayAtOffset(ZoneOffset offset) {
        return ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(offset).toLocalDate();
    }

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
