package com.application.Model;

import com.application.serves.Manager;

import java.util.List;

public class TopLevelButton extends Button {
    String text;
    int id;
    List<Button> subButtonList;

    public TopLevelButton(String text, List<Button> subButtonList) {
        this.text = text;
        id = Manager.getNextId();
        this.subButtonList = subButtonList;
    }

    @Override
    public int getId() {
        return id;
    }

    public List<Button> getSubButtonList() {
        return subButtonList;
    }

    public void setSubButtonList(List<Button> subButtonList) {
        this.subButtonList = subButtonList;
    }
}
