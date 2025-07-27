package com.vitorxp.SkyBlockModVX.logger;

import com.vitorxp.SkyBlockModVX.chat.ChatCommandTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InventoryLossLogger {

    private final Minecraft mc = Minecraft.getMinecraft();
    private ItemStack[] lastInventory = new ItemStack[36];
    private static final List<String> logs = new ArrayList<>();

    public InventoryLossLogger() {
        saveCurrentInventory();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) return;

        InventoryPlayer inv = mc.thePlayer.inventory;

        for (int i = 0; i < 36; i++) {
            ItemStack current = inv.mainInventory[i];
            ItemStack previous = lastInventory[i];

            if (previous != null && (current == null || !ItemStack.areItemStacksEqual(current, previous) || current.stackSize < previous.stackSize)) {
                String itemName = previous.getDisplayName();
                String location = mc.thePlayer.getPosition().toString();
                String screen = mc.currentScreen != null ? mc.currentScreen.getClass().getSimpleName() : "null";
                String lastCommand = ChatCommandTracker.getLastCommand();

                String log = String.format("[Perda de Item] Nome: %s | Posição: %s | Tela: %s | Último Comando: %s | Tempo: %s",
                        itemName,
                        location,
                        screen,
                        lastCommand,
                        new Date());

                logs.add(log);
            }
        }

        saveCurrentInventory();
    }

    private void saveCurrentInventory() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        saveToFile();
        InventoryPlayer inv = mc.thePlayer.inventory;
        for (int i = 0; i < 36; i++) {
            lastInventory[i] = inv.mainInventory[i] == null ? null : inv.mainInventory[i].copy();
        }
    }

    public List<String> getLogs() {
        return logs;
    }

    public static void saveToFile() {
        try {
            File dir = new File("skyblockmodvx_logs");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "item_loss_log.txt");

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                for (String log : logs) {
                    writer.write(log);
                    writer.newLine();
                }
            }

            //System.out.println("Logs de perda de item salvos com sucesso!");
        } catch (IOException e) {
            //System.err.println("Erro ao salvar logs de perda de item:");
            e.printStackTrace();
        }
    }
}