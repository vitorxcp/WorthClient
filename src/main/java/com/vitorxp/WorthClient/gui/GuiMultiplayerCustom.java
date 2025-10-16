package com.vitorxp.WorthClient.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;
import java.lang.reflect.Field;

public class GuiMultiplayerCustom extends GuiMultiplayer {

    private ServerData serverToAutoJoin;

    public GuiMultiplayerCustom(GuiScreen parentScreen) {
        super(parentScreen);
    }

    public GuiMultiplayerCustom(GuiScreen parentScreen, ServerData serverToAutoJoin) {
        super(parentScreen);
        this.serverToAutoJoin = serverToAutoJoin;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.serverToAutoJoin != null) {
            ServerData server = this.serverToAutoJoin;
            this.serverToAutoJoin = null;

            // USA A NOVA FUNÇÃO AUXILIAR
            GuiScreen theParentScreen = getPrivateParentScreen(this);
            this.mc.displayGuiScreen(new GuiConnecting(theParentScreen, this.mc, server));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled && button.id == 1) {
            ServerData selectedServerData = getSelectedServerData(this);

            if (selectedServerData != null && this.mc.theWorld != null) {
                this.mc.addScheduledTask(() -> {
                    if (this.mc.theWorld == null) return;

                    GuiScreen parent = getPrivateParentScreen(this);
                    NetHandlerPlayClient netHandler = this.mc.getNetHandler();

                    if (netHandler != null) {
                        netHandler.getNetworkManager().closeChannel(new ChatComponentText("Disconnecting"));
                    }
                    this.mc.loadWorld(null);

                    this.mc.displayGuiScreen(new GuiMultiplayerCustom(parent, selectedServerData));
                });
                return;
            }
        }
        super.actionPerformed(button);
    }


    private ServerData getSelectedServerData(GuiMultiplayer gui) {
        try {
            Field selectedServerField = GuiMultiplayer.class.getDeclaredField("selectedServer");
            selectedServerField.setAccessible(true);
            int serverIndex = selectedServerField.getInt(gui);

            if (serverIndex >= 0) {
                Field serverListField = GuiMultiplayer.class.getDeclaredField("savedServerList");
                serverListField.setAccessible(true);
                ServerList savedServerList = (ServerList) serverListField.get(gui);
                return savedServerList.getServerData(serverIndex);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * NOVA FUNÇÃO AUXILIAR
     * Pega o campo 'parentScreen' privado da GuiMultiplayer de forma segura.
     */
    private GuiScreen getPrivateParentScreen(GuiMultiplayer gui) {
        try {
            Field parentScreenField = GuiMultiplayer.class.getDeclaredField("parentScreen");
            parentScreenField.setAccessible(true);
            return (GuiScreen) parentScreenField.get(gui);
        } catch (Exception e) {
            e.printStackTrace();
            // Retorna o menu principal como um fallback seguro para evitar crash
            return new GuiClientMainMenu();
        }
    }
}