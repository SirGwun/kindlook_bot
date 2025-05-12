package com.application.Controller;

import com.application.Main;

public class SystemEnvProxi {
    public String get(String varName) {
        if (!(Integer.parseInt(System.getenv("AMVERA")) == 1)) {
            throw new RuntimeException("Не в облаке амверы");
            return null;
        }
        String var = System.getenv(varName);
        if (var.isEmpty()) {
            throw new RuntimeException("Не обнаруженна переменная или не заданно ее значение");
            return null;
        }
        return var;
    }
}
