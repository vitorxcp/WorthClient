package com.vitorxp.WorthClient.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.vitorxp.WorthClient.utils.SSLTrustManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MicrosoftAuth {

    private static final String CLIENT_ID = "cc134b19-746a-4838-b397-e8eaee467b55";
    private static final JsonParser JSON_PARSER = new JsonParser();

    public static CompletableFuture<Account> login(Consumer<String> statusConsumer, Consumer<String> userCodeConsumer) {
        CompletableFuture<Account> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                statusConsumer.accept("§eGerando código de dispositivo...");
                JsonObject deviceCodeResponse = post("https://login.microsoftonline.com/common/oauth2/v2.0/devicecode",
                        "client_id=" + CLIENT_ID + "&scope=XboxLive.signin%20offline_access");

                String deviceCode = deviceCodeResponse.get("device_code").getAsString();
                String userCode = deviceCodeResponse.get("user_code").getAsString();
                String verificationUri = deviceCodeResponse.get("verification_uri").getAsString();
                long interval = deviceCodeResponse.get("interval").getAsLong();
                long expiresIn = deviceCodeResponse.get("expires_in").getAsLong();
                long startTime = System.currentTimeMillis();

                userCodeConsumer.accept(userCode);
                statusConsumer.accept("§fAcesse §b" + verificationUri + "§f e insira o código:");

                JsonObject msTokenResponse = null;
                while (System.currentTimeMillis() - startTime < expiresIn * 1000) {
                    Thread.sleep(interval * 1000);
                    msTokenResponse = post("https://login.microsoftonline.com/common/oauth2/v2.0/token",
                            "grant_type=urn:ietf:params:oauth:grant-type:device_code&client_id=" + CLIENT_ID + "&device_code=" + deviceCode);
                    if (msTokenResponse.has("access_token")) {
                        break;
                    }
                }

                if (msTokenResponse == null || !msTokenResponse.has("access_token")) {
                    throw new Exception("Login expirado ou cancelado.");
                }

                String msAccessToken = msTokenResponse.get("access_token").getAsString();

                statusConsumer.accept("§eAutenticando com Xbox Live...");
                JsonObject xblBody = new JsonObject();
                JsonObject properties = new JsonObject();
                properties.addProperty("AuthMethod", "RPS");
                properties.addProperty("SiteName", "user.auth.xboxlive.com");
                properties.addProperty("RpsTicket", "d=" + msAccessToken);
                xblBody.add("Properties", properties);
                xblBody.addProperty("RelyingParty", "http://auth.xboxlive.com");
                xblBody.addProperty("TokenType", "JWT");

                JsonObject xblResponse = postJson("https://user.auth.xboxlive.com/user/authenticate", xblBody);
                String xblToken = xblResponse.get("Token").getAsString();
                String userHash = xblResponse.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

                statusConsumer.accept("§eVerificando segurança (XSTS)...");
                JsonObject xstsBody = new JsonObject();
                properties = new JsonObject();
                properties.addProperty("SandboxId", "RETAIL");
                JsonArray userTokens = new JsonArray();
                userTokens.add(new JsonPrimitive(xblToken));
                properties.add("UserTokens", userTokens);
                xstsBody.add("Properties", properties);
                xstsBody.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
                xstsBody.addProperty("TokenType", "JWT");

                JsonObject xstsResponse = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsBody);
                String xstsToken = xstsResponse.get("Token").getAsString();

                statusConsumer.accept("§eEntrando no Minecraft...");
                JsonObject mcLoginBody = new JsonObject();
                mcLoginBody.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);

                JsonObject mcLoginResponse = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcLoginBody);
                String mcAccessToken = mcLoginResponse.get("access_token").getAsString();

                statusConsumer.accept("§eObtendo perfil...");
                JsonObject profileResponse = get("https://api.minecraftservices.com/minecraft/profile", mcAccessToken);

                String username = profileResponse.get("name").getAsString();
                String uuid = profileResponse.get("id").getAsString();

                Account account = new Account(username, uuid, mcAccessToken);
                future.complete(account);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    private static JsonObject post(String url, String content) throws Exception {
        return request("POST", url, content, null);
    }

    private static JsonObject postJson(String url, JsonObject content) throws Exception {
        return request("POST", url, content.toString(), null);
    }

    private static JsonObject get(String url, String token) throws Exception {
        return request("GET", url, null, token);
    }

    private static JsonObject request(String method, String url, String content, String token) throws Exception {
        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();

        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(SSLTrustManager.getSocketFactory());
        }

        connection.setRequestMethod(method);

        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (content != null) {
            connection.setDoOutput(true);
            if(content.startsWith("{"))
                connection.setRequestProperty("Content-Type", "application/json");
            else
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return JSON_PARSER.parse(reader).getAsJsonObject();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String error = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
                throw new Exception("HTTP " + responseCode + ": " + error);
            }
        }
    }
}