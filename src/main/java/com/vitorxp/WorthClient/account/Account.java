package com.vitorxp.WorthClient.account;

public class Account {
    public String username;
    public String uuid;
    public String accessToken;

    public Account(String username, String uuid, String accessToken) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
    }

    public boolean isCracked() {
        return "0".equals(this.accessToken);
    }
}