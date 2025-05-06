package com.application.serves;

import com.application.Model.*;

import java.util.*;

public class Manager {
    private static final Random RANDOM = new Random();
    private final static Manager MANAGER = new Manager();
    private static int id = -1;
    private final List<User> users = new ArrayList<>();
    List<Button> buttonTree = new ArrayList<>();
    List<Phrase> phraseList = new ArrayList<>();

    private Manager() {
        init();
    }

    public static int getNextId() {
        id += 1;
        return id;
    }

    public void init() {
        ButtonXmlLoader loader = new ButtonXmlLoader("data.xml");
        buttonTree = loader.loadButtons();
        extractPhrases(buttonTree);
    }

    public static Manager getInstance() {
        return MANAGER;
    }

    public Button findButton(int buttonId, List<Button> bTree) {
        for (Button button : bTree) {
            if (button.getId() == buttonId) {
                return button;
            }
            if (button instanceof TopLevelButton) {
                Button foundButton = findButton(buttonId, ((TopLevelButton) button).getSubButtonList());
                if (foundButton != null) {
                    return foundButton;
                }
            }
        }
        return null;
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

    public Phrase getNextPhrase(int buttonId, long userId) {
        Button button = findButton(buttonId, buttonTree);
        if (button instanceof TopLevelButton || button == null) {
            throw new IllegalArgumentException();
        }
        PhraseButton pButton = (PhraseButton) button;
        User user = DBProxy.getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException();
        }

        List<Integer> sentPhrases = DBProxy.getSentPhraseId(user);
        List<Phrase> availablePhrases = new ArrayList<>();
        for (Phrase phrase : pButton.getPhraseList()) {
            if (!sentPhrases.contains(phrase.getId())) {
                availablePhrases.add(phrase);
            }
        }
        if (availablePhrases.isEmpty()) {
            return null;
        }
        Phrase answer = availablePhrases.get(RANDOM.nextInt(0, availablePhrases.size()));

        user.addSendedPhrase(answer);
        return answer;
    }

}
