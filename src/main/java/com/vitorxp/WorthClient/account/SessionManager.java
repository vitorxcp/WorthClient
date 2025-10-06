package com.vitorxp.WorthClient.account;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.UUID;

public class SessionManager {

    public static String login(String email, String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(email);
        auth.setPassword(password);

        try {
            auth.logIn();
            String uuid = auth.getSelectedProfile().getId().toString().replace("-", "");
            Account account = new Account(auth.getSelectedProfile().getName(), uuid, auth.getAuthenticatedToken());
            AccountManager.addAccount(account);
            setSession(new Session(account.username, account.uuid, account.accessToken, "mojang"));
            return "§aLogado com sucesso como " + account.username;
        } catch (AuthenticationException e) {
            return "§cErro: " + e.getLocalizedMessage();
        }
    }

    public static void loginCracked(String username) {
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()).toString();
        Account account = new Account(username, uuid, "0");
        AccountManager.addAccount(account);
        setSession(new Session(account.username, account.uuid, account.accessToken, "legacy"));
    }

    public static void switchAccount(Account account) {
        String type = account.isCracked() ? "legacy" : "mojang";
        setSession(new Session(account.username, account.uuid, account.accessToken, type));
    }

    private static void setSession(Session session) {
        try {
            Field sessionField = Minecraft.class.getDeclaredField("field_71449_j");
            sessionField.setAccessible(true);
            sessionField.set(Minecraft.getMinecraft(), session);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Não foi possível trocar a sessão!");
        }
    }
}