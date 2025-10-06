package com.vitorxp.WorthClient.utils; // Coloque no seu pacote de utilidades

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class SSLTrustBypasser {

    public static void install() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("### AVISO: Bypass de verificação SSL foi ativado! Suas conexões HTTPS não são seguras. ###");

        } catch (Exception e) {
            System.err.println("### ERRO: Falha ao instalar o bypass de SSL. ###");
            e.printStackTrace();
        }
    }
}