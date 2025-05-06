package com.application.Model;

import java.util.List;

public class User {
    String name;
    long id;
    List<Phrase> sendedPhrase;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addSendedPhrase(Phrase phrase) {
        sendedPhrase.add(phrase);
        //DBProxy.addSentPhrase(this, phrase);
    }

    public List<Phrase> getSentPhrase() {
        //DBProxy.getSentPhrase(this);
        return sendedPhrase;
    }

    public void setSendedPhrase(List<Phrase> sendedPhrase) {
        this.sendedPhrase = sendedPhrase;
    }

    public void addSetSendedPhrase(Phrase phrase) {
        sendedPhrase.add(phrase);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " " + id;
    }
}
