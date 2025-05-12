package com.application.Controller;

import com.application.Main;

public class SystemEnvProxy {
    public String get(String varName) {
        if (!Main.inAmvera()) {
            throw new RuntimeException("Не в облаке амверы");
        }
        String var = System.getenv(varName);
        if (var.isEmpty()) {
            throw new RuntimeException("Не обнаруженна переменная или не заданно ее значение");
        }
        return var;
    }
}
