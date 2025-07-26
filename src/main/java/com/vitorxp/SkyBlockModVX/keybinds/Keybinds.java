package com.vitorxp.SkyBlockModVX.keybinds;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Keybinds {
    public static KeyBinding openConfig;
    public static KeyBinding openConfigHud;

    public static void init() {
        openConfig = new KeyBinding("Abrir menu de Configurações", Keyboard.KEY_K, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(openConfig);
        openConfigHud = new KeyBinding("Abrir menu de Overlays", Keyboard.KEY_RSHIFT, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(openConfigHud);
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (openConfig.isPressed()) {
            SkyBlockMod.pendingOpenMenu = true;
        }
        if (openConfigHud.isPressed()) {
            SkyBlockMod.pendingOpenMenuHud = true;
        }
    }
}
