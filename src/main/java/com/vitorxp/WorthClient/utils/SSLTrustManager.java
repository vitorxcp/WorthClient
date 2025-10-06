package com.vitorxp.WorthClient.utils;

import org.apache.commons.io.IOUtils;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

public class SSLTrustManager {

    private static SSLSocketFactory customSocketFactory;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        try {
            String defaultTrustStorePassword = "changeit";
            File trustStoreFile = File.createTempFile("cacerts", "jks");
            trustStoreFile.deleteOnExit();

            try (InputStream is = SSLTrustManager.class.getResourceAsStream("/assets/worthclient/security/cacerts");
                 OutputStream os = new FileOutputStream(trustStoreFile)) {
                if (is == null) throw new RuntimeException("Arquivo 'cacerts' não encontrado!");
                IOUtils.copy(is, os);
            }

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream is = new FileInputStream(trustStoreFile)) {
                trustStore.load(is, defaultTrustStorePassword.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            customSocketFactory = sslContext.getSocketFactory();

            System.out.println("SSLTrustManager inicializado com sucesso.");
            initialized = true;

        } catch (Exception e) {
            System.err.println("Falha ao inicializar o SSLTrustManager.");
            e.printStackTrace();
        }
    }

    public static SSLSocketFactory getSocketFactory() {
        if (!initialized) initialize();
        return customSocketFactory;
    }
}