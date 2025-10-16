package com.vitorxp.WorthClient.events;

import com.vitorxp.WorthClient.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.vitorxp.WorthClient.WorthClient.*;

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
            Minecraft.getMinecraft().displayGuiScreen(new GuiChatEnvEditor());
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

        if (event.phase == TickEvent.Phase.END && GuiAdminPainel) {
            Minecraft.getMinecraft().displayGuiScreen(new PainelAdminGui());
            GuiAdminPainel = false;
        }

        if (event.phase == TickEvent.Phase.END && guiEditorAdmin) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditorAdmin());
            guiEditorAdmin = false;
        }

        if (event.phase == TickEvent.Phase.END && GuiAdminazw) {
            Minecraft.getMinecraft().displayGuiScreen(new AdminGui(nameArsAdmin));
            GuiAdminazw = false;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            event.gui = new GuiClientMainMenu();
        }

        if (event.gui instanceof GuiIngameMenu) {
            event.gui = new GuiPauseMenuCustom();
        }
    }
}
