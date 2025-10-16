package com.vitorxp.WorthClient.utils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class SSLTrustBypasser {

    public static void install() {
        try {
            // Cria um TrustManager que não valida cadeias de certificados
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            // Obtém o contexto SSL e inicializa com nosso TrustManager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            // **A MUDANÇA PRINCIPAL:**
            // Usa nosso SocketFactory personalizado que força o TLSv1.2 e cifras compatíveis
            HttpsURLConnection.setDefaultSSLSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

            // Cria um HostnameVerifier que confia em todos os hosts
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("### AVISO: Bypass de SSL com SocketFactory personalizado foi ativado! ###");

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("### ERRO: Falha ao instalar o bypass de SSL. ###");
            e.printStackTrace();
        }
    }

    /**
     * Uma implementação de SSLSocketFactory que delega a criação de sockets,
     * mas força a ativação dos protocolos TLSv1.1 e TLSv1.2 nos sockets criados.
     */
    private static class Tls12SocketFactory extends SSLSocketFactory {

        private static final String[] TLS_VERSIONS = {"TLSv1.1", "TLSv1.2"};
        final SSLSocketFactory delegate;

        public Tls12SocketFactory(SSLSocketFactory base) {
            this.delegate = base;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return patch(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return patch(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return patch(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket patch(Socket s) {
            if (s instanceof SSLSocket) {
                ((SSLSocket) s).setEnabledProtocols(TLS_VERSIONS);
            }
            return s;
        }
    }
}