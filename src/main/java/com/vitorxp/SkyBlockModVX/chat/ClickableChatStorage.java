package com.vitorxp.SkyBlockModVX.chat;

import java.util.ArrayList;
import java.util.List;

public class ClickableChatStorage {
    private static final List<ClickableChatLine> lines = new ArrayList<>();

    public static void addClickable(ClickableChatLine line) {
        lines.add(line);
    }

    public static List<ClickableChatLine> getClickables() {
        return new ArrayList<>(lines);
    }

    public static void clear() {
        lines.clear();
    }
}
