package com.application;

import java.security.KeyStore;
import java.security.KeyStoreException;

public class HttpsTelegramServer {
    int port = 8443;

    public HttpsTelegramServer() throws KeyStoreException {
        char[] password = "".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");

    }
}
