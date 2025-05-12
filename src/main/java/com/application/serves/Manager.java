package com.application.serves;

import com.application.Main;
import com.application.Model.*;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class Manager {
    private static final Random RANDOM = new Random();
    private final static Manager MANAGER = new Manager();
    private static int currentButtonId = 0;
    List<Button> buttonTree = new ArrayList<>();
    List<Phrase> phraseList = new ArrayList<>();

    private static final String START_MESSAGE = "Стартовое сообщение";
    private static final String UNKNOWN_MESSAGE = "Сообщение на неопознанную команду или текст";


    private Manager() {
        init();
    }

    public static int getNextId() {
        return currentButtonId++;
    }

    public void init() {
        ButtonXmlLoader loader = new ButtonXmlLoader(FileManager.getFilePatch("data.xml"));
        buttonTree = loader.loadButtons();
        extractPhrases(buttonTree);
    }

    public static Manager getInstance() {
        return MANAGER;
    }

    public Optional<Button> findButton(int buttonId) {
        return findButton(buttonId, buttonTree);
    }

    private Optional<Button> findButton(int buttonId, List<Button> bTree) {
        for (Button button : bTree) {
            if (button.getId() == buttonId) {
                return Optional.of(button);
            }
            if (button instanceof TopLevelButton) {
                Optional<Button> foundButton = findButton(buttonId, ((TopLevelButton) button).getSubButtonList());
                if (foundButton.isPresent()) {
                    return foundButton;
                }
            }
        }
        return Optional.empty();
    }

    public void extractPhrases(List<Button> buttonTree) {
        for (Button button : buttonTree) {
            if (button instanceof PhraseButton phraseButton) {
                phraseList.addAll(phraseButton.getPhraseList());
            } else if (button instanceof TopLevelButton topLevelButton) {
                extractPhrases(topLevelButton.getSubButtonList());
            }
        }
    }

    public Phrase getNextPhrase(Button button, User user) throws SQLException {
        if (button instanceof TopLevelButton) {
            throw new IllegalArgumentException("TopLevelButton cannot be used for phrases");
        }
        PhraseButton pButton = (PhraseButton) button;

        if (DBProxy.getUser(user.getId()) == null) {
            DBProxy.addUser(user);
        }

        Set<Integer> sentPhrases = new HashSet<>(DBProxy.getSentPhraseId(user));
        List<Phrase> availablePhrases = new ArrayList<>();
        for (Phrase phrase : pButton.getPhraseList()) {
            if (!sentPhrases.contains(phrase.getId())) {
                availablePhrases.add(phrase);
            }
        }
        if (availablePhrases.isEmpty()) {
            availablePhrases = pButton.getPhraseList();
            DBProxy.resetSendedPhrases(user, pButton.getPhraseList());
        }
        Phrase answer;
        try {
            answer = availablePhrases.get(RANDOM.nextInt(availablePhrases.size()));
        } catch (IllegalArgumentException e) {
            return new Phrase("Упс, все закончилось");
        }
        DBProxy.addSentPhrase(user, answer);
        return answer;
    }

    public List<Button> getRootList() {
        return buttonTree;
    }

    public String getStartMessage() {
        return START_MESSAGE;
    }

    public String getUnknownMessage() {
        return UNKNOWN_MESSAGE;
    }
}
