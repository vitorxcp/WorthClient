package com.vitorxp.WorthClient.logger;

import com.vitorxp.WorthClient.chat.ChatCommandTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InventoryLossLogger {

    private static final Logger logger = LogManager.getLogger("InventoryLogger");
    private final Minecraft mc = Minecraft.getMinecraft();
    private ItemStack[] lastInventory = new ItemStack[36];
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 10;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public InventoryLossLogger() {
        updateInventoryCache();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        checkInventoryDiff();
    }

    private void checkInventoryDiff() {
        InventoryPlayer inv = mc.thePlayer.inventory;

        for (int i = 0; i < 36; i++) {
            ItemStack current = inv.mainInventory[i];
            ItemStack previous = lastInventory[i];

            if (previous != null && current == null) {
                logItemLoss(previous, i, "REMOVIDO", previous.stackSize);
            }
            else if (previous != null && !ItemStack.areItemsEqual(previous, current)) {
                logItemLoss(previous, i, "SUBSTITUIDO", previous.stackSize);
            }
            else if (previous != null && current.stackSize < previous.stackSize) {
                int diff = previous.stackSize - current.stackSize;
                logItemLoss(previous, i, "DIMINUIU", diff);
            }
        }

        updateInventoryCache();
    }

    private void updateInventoryCache() {
        if (mc.thePlayer == null) return;
        InventoryPlayer inv = mc.thePlayer.inventory;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.mainInventory[i];
            lastInventory[i] = (stack == null) ? null : stack.copy();
        }
    }

    private void logItemLoss(ItemStack item, int slot, String type, int amount) {
        try {
            String itemName = item.getDisplayName();
            String location = String.format("X:%.0f Y:%.0f Z:%.0f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            String screen = mc.currentScreen != null ? mc.currentScreen.getClass().getSimpleName() : "Ingame";
            String lastCommand = ChatCommandTracker.getLastCommand() != null ? ChatCommandTracker.getLastCommand() : "Nenhum";
            String time = TIME_FORMAT.format(new Date());
            
            String logLine = String.format("[%s] [%s] Item: %s (x%d) | Slot: %d | Tela: %s | Loc: %s | Cmd: %s",
                    time, type, itemName, amount, slot, screen, location, lastCommand);

            writeLogToDisk(logLine);

        } catch (Exception e) {
            logger.error("Erro ao registrar perda de item", e);
        }
    }

    public static void saveToFile() {
        writeLogToDisk("--- FIM DA SESSÃO ---");
    }

    private static synchronized void writeLogToDisk(String line) {
        File dir = new File("WorthClient_logs");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "inventory_loss_" + DATE_FORMAT.format(new Date()) + ".txt";
        File file = new File(dir, fileName);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            writer.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}