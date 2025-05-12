package com.application.Controller;

public class SystemEnvProxy {
    public String get(String varName) {
        if (!(Integer.parseInt(System.getenv("AMVERA")) == 1)) {
            throw new RuntimeException("Не в облаке амверы");
        }
        String var = System.getenv(varName);
        if (var.isEmpty()) {
            throw new RuntimeException("Не обнаруженна переменная или не заданно ее значение");
        }
        return var;
    }
}
