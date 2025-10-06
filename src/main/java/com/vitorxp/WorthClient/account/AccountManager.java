package com.vitorxp.WorthClient.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session; // NOVO: Import necessário
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static final File ACCOUNT_FILE = new File(Minecraft.getMinecraft().mcDataDir, "WorthLauncherAccounts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Account> accounts = new ArrayList<>();

    public static void loadAccounts() {
        accounts.clear();
        if (ACCOUNT_FILE.exists()) {
            try (Reader reader = new FileReader(ACCOUNT_FILE)) {
                Type listType = new TypeToken<ArrayList<Account>>() {}.getType();
                List<Account> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) accounts.addAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Session currentSession = Minecraft.getMinecraft().getSession();

        boolean alreadyExists = accounts.stream()
                .anyMatch(acc -> acc.username.equalsIgnoreCase(currentSession.getUsername()));

        if (!"Player".equals(currentSession.getUsername()) && !alreadyExists) {
            Account sessionAccount = new Account(
                    currentSession.getUsername(),
                    currentSession.getPlayerID(),
                    currentSession.getToken()
            );

            addAccount(sessionAccount);
            System.out.println("Sessão atual de '" + sessionAccount.username + "' foi salva automaticamente.");
        }
    }

    public static void saveAccounts() {
        try (Writer writer = new FileWriter(ACCOUNT_FILE)) {
            GSON.toJson(accounts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Account> getAccounts() { return accounts; }

    public static void addAccount(Account account) {
        accounts.removeIf(acc -> acc.username.equalsIgnoreCase(account.username));
        accounts.add(account);
        saveAccounts();
    }

    public static void removeAccount(Account account) {
        accounts.remove(account);
        saveAccounts();
    }
}