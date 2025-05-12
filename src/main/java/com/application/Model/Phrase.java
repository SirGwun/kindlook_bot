package com.application.Model;

import com.application.serves.DBProxy;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class Phrase {
    int id;
    String text;
    private List<String> imageNames;

    public Phrase(String phrase) {
        this.text = phrase;
        try {
            this.id = DBProxy.insertPhrase(text);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Phrase(String phrase, List<String> imagePaths) {
        this.text = phrase;
        if (!imagePaths.isEmpty()) {
            this.imageNames = imagePaths;
        }
        try {
            this.id = DBProxy.insertPhrase(text);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Phrase(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(List<String> imageNames) {
        this.imageNames = imageNames;
    }
}
