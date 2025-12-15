package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.manager.AutoTextManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class AutoTextHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen != null) return;
        if (mc.thePlayer == null) return;

        if (Keyboard.getEventKeyState()) {
            int pressedKey = Keyboard.getEventKey();

            for (AutoTextManager.TextMacro macro : AutoTextManager.macros) {
                if (macro.keyCode == pressedKey) {
                    mc.thePlayer.sendChatMessage(macro.message);
                }
            }
        }
    }
}