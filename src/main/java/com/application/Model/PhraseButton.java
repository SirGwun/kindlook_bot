package com.application.Model;

import com.application.serves.Manager;

import java.util.List;

public class PhraseButton extends Button {
    String text;
    int id;
    List<Phrase> phraseList;

    public PhraseButton(String text, List<Phrase> phraseList) {
        this.text = text;
        id = Manager.getNextId();
        this.phraseList = phraseList;
    }

    @Override
    public int getId() {
        return id;
    }

    public List<Phrase> getPhraseList() {
        return phraseList;

    }

    @Override
    public String getText() {
        return text;
    }

    public void setPhraseList(List<Phrase> phraseList) {
        this.phraseList = phraseList;
    }
}
