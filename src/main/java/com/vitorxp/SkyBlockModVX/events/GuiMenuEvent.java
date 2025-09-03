package com.vitorxp.SkyBlockModVX.events;

import com.vitorxp.SkyBlockModVX.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.*;

public class GuiMenuEvent {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase == TickEvent.Phase.END && pendingOpenMenu) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiModMenu());
            pendingOpenMenu = false;
        }

        if (event.phase == TickEvent.Phase.END && pendingOpenMenuHud) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiHudEditor());
            pendingOpenMenuHud = false;
        }

        if (event.phase == TickEvent.Phase.END && guiEditorArmor) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditorArmor());
            guiEditorArmor = false;
        }

        if (event.phase == TickEvent.Phase.END && guiEditorPet) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditorPet());
            guiEditorPet = false;
        }

        if (event.phase == TickEvent.Phase.END && guiEditorChat) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiChatEditor());
            guiEditorChat = false;
        }

        if (event.phase == TickEvent.Phase.END && GuiKeyEditor) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiKeystrokesColorEditor());
            GuiKeyEditor = false;
        }

        if (event.phase == TickEvent.Phase.END && GuiOverlay) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditorOverlays());
            GuiOverlay = false;
        }

        if (event.phase == TickEvent.Phase.END && GuiPerspective) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiPerspectiveMod());
            GuiPerspective = false;
        }
    }
}
