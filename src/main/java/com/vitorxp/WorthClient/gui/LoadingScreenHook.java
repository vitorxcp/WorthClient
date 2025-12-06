package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;

public class LoadingScreenHook {

    public static LoadingScreenHook INSTANCE = new LoadingScreenHook();
    public static WorthLoadingGUI customGUI;

    public static void inject() {
        if (customGUI == null)
            customGUI = new WorthLoadingGUI(Minecraft.getMinecraft());
    }
}
