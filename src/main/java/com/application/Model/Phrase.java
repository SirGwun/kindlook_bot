package com.application.Model;

import com.application.serves.DBProxy;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Phrase {
    int id;
    String text;
    private List<Path> imagePaths;

    public Phrase(String phrase) {
        this.text = phrase;
        try {
            this.id = DBProxy.insertPhrase(text);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Phrase(String phrase, List<Path> imagePaths) {
        this.text = phrase;
        if (!imagePaths.isEmpty()) {
            this.imagePaths = imagePaths;
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

    public List<Path> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<Path> imagePaths) {
        this.imagePaths = imagePaths;
    }
}
