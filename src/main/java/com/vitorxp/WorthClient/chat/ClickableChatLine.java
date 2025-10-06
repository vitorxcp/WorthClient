package com.vitorxp.WorthClient.chat;

public class ClickableChatLine {
    public int x, y, width;
    public String message;

    public ClickableChatLine(int x, int y, int width, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.message = message;
    }
}
