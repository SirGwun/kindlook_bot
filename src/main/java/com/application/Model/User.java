package com.application.Model;

import com.application.serves.DBProxy;

import java.util.List;

public class User {
    String name;
    long id;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
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
