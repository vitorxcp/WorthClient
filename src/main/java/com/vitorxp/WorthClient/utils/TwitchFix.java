package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class TwitchFix {

    public static void disableTwitchKeys() {
        try {
            GameSettings settings = Minecraft.getMinecraft().gameSettings;

            if (settings.keyBindStreamStartStop.getKeyCode() != 0) {
                settings.keyBindStreamStartStop.setKeyCode(0);
            }
            if (settings.keyBindStreamCommercials.getKeyCode() != 0) {
                settings.keyBindStreamCommercials.setKeyCode(0);
            }
            if (settings.keyBindStreamToggleMic.getKeyCode() != 0) {
                settings.keyBindStreamToggleMic.setKeyCode(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}