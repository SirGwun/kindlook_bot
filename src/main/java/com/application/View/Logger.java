package com.application.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.logging.Level;

public class Logger {
    public static void logResponse(HttpURLConnection conn, int responseCode) {
        try {
            log("Response Code: " + responseCode, Level.INFO);

            InputStream stream = responseCode < HttpURLConnection.HTTP_BAD_REQUEST
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (stream == null) {
                log("No response body available.", Level.WARNING);
                return;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = in.readLine()) != null) {
                    log(line, Level.INFO);
                }
            }
        } catch (IOException e) {
            log("Exception while reading response: " + e, Level.WARNING);
        }
    }

    public static void log(String str) {
        System.out.println(str);
    }

    public static void log(String str, Level level) {
        System.out.println(str);
    }
}
