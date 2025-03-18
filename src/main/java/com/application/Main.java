package com.application;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

public class Main {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        System.out.println(dotenv.get("HTTPS_PAS"));
        for (DotenvEntry e : dotenv.entries()) {
            System.out.println(e);
        }
    }
}