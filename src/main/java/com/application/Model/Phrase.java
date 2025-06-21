package com.application.Model;

import com.application.serves.DBProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Phrase {
    private static final Logger log = LoggerFactory.getLogger(Phrase.class);
    int id;
    String text;
    private List<String> imageNames;

    public Phrase(String phrase) {
        this.text = phrase;
        try {
            this.id = DBProxy.insertPhrase(text);
        } catch (SQLException e) {
            log.error("", e);
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
            log.error("", e);
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
        if (imageNames == null) {
            return new ArrayList<>();
        }
        return imageNames;
    }

    public void setImageNames(List<String> imageNames) {
        this.imageNames = imageNames;
    }
}
