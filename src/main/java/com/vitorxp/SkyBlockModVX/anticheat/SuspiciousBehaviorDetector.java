package com.vitorxp.SkyBlockModVX.anticheat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class SuspiciousBehaviorDetector {

    private final Minecraft mc = Minecraft.getMinecraft();
    private long lastClickTime = 0;

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1393202623913529466/kw52CPqVSJ8_O61ybx2b_nW_8GzG4skKRPrNh-GX5QwYMTpgT4-F_pTqdz9cZeirqVoh";

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) return;

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            long now = System.currentTimeMillis();
            long delta = now - lastClickTime;

            if (delta == 2) {
                System.out.println("⚠️ Clique abaixo de 3ms detectado! (" + delta + "ms)");

                sendWebhook("⚠️ Suspeita de Macro de Click Ultra-Rápido",
                        "Jogador: " + mc.thePlayer.getName() +
                                "\nClique abaixo de 3ms detectado (" + delta + "ms)");
            }

            detectKillAura();
            detectKnownHackMods();
            detectMacroPattern(now);
            detectBlink();
            detectSprintHitExploit();
            detectAutoArmor();
            detectFlyHack();
            detectNoSlow();
            lastClickTime = now;
        }
    }

    private void sendWebhook(String title, String content) {
        if (!isAllowedServer()) return;

        String lobby = getCurrentLobby();
        String fullContent = content + "\n📍 Lobby: `" + lobby + "`";

        new Thread(() -> {
            try {
                NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
                long ping = info.getResponseTime();

                System.out.println("Preparando webhook...");
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "SkyBlockModVX");

                String safeTitle = escapeJson(title);
                String safeContent = escapeJson(fullContent);

                String json = "{"
                        + "\"username\": \"SkyBlockModVX - AntiCheat\","
                        + "\"content\": \"\","
                        + "\"embeds\": [{"
                        + "  \"title\": \"" + safeTitle + "\","
                        + "  \"description\": \"" + safeContent + "\n**Ping**\n " + ping + "\","
                        + "  \"color\": 16711680"
                        + "}]"
                        + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Resposta Webhook: " + responseCode + " - " + connection.getResponseMessage());

                if (responseCode != 204 && responseCode != 200) {
                    System.out.println("Erro ao enviar webhook: " + responseCode + " - " + connection.getResponseMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private boolean isAllowedServer() {
        String ip = Minecraft.getMinecraft().getCurrentServerData() != null ?
                Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase() : "";

        return ip.contains("redeworth.com") || ip.contains("redesky.net");
    }
    private String getCurrentLobby() {
        try {
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
            if (sidebar == null) return "Desconhecido";

            String displayName = sidebar.getDisplayName();
            if (displayName != null) {
                String cleaned = displayName.replaceAll("(?i)§.", "").toLowerCase();

                if (cleaned.contains("lobby") || cleaned.contains("sky block") || cleaned.contains("sky wars") || cleaned.contains("bed wars")) {
                    return cleaned;
                }
            }

            List<String> lines = scoreboard.getSortedScores(sidebar).stream()
                    .map(score -> {
                        String s = score.getPlayerName();
                        return s != null ? s : "";
                    })
                    .collect(Collectors.toList());

            for (String line : lines) {
                if (line.toLowerCase().contains("lobby") || line.toLowerCase().contains("sky block")  || line.toLowerCase().contains("sky wars")  || line.toLowerCase().contains("bed wars")) {
                    return line;
                }
            }
        } catch (Exception ignored) {}

        return "Desconhecido";
    }
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private long lastActionTime = 0;
    private int identicalDelayCount = 0;
    private long previousDelay = -1;

    private void detectMacroPattern(long now) {
        long delay = now - lastActionTime;
        if (delay == previousDelay && delay > 0 && delay < 1000) {
            identicalDelayCount++;
        } else {
            identicalDelayCount = 0;
        }

        if (identicalDelayCount > 14) {
            sendWebhook("⚠️ Suspeita de Macro Padrão",
                    "Jogador: " + mc.thePlayer.getName() +
                            "\nDelay idêntico entre ações detectado (" + delay + "ms)\nTotal repetição: " + identicalDelayCount);
            identicalDelayCount = 0;
        }

        previousDelay = delay;
        lastActionTime = now;
    }

    private Entity lastTarget = null;
    private int auraHits = 0;
    private long lastAuraCheck = 0;

    private void detectKillAura() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;

        Entity target = mc.objectMouseOver.entityHit;

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (target != null && target != mc.thePlayer && target == lastTarget) {
                auraHits = 0;
            } else if (target != null && target != mc.thePlayer) {
                long now = System.currentTimeMillis();
                if (now - lastAuraCheck < 100) {
                    auraHits++;
                } else {
                    auraHits = 1;
                }
                lastAuraCheck = now;

                if (auraHits >= 5) {
                    sendWebhook("⚠️ Suspeita de KillAura",
                            "Jogador: " + mc.thePlayer.getName() +
                                    "\nMira pulando entre entidades muito rapidamente.");
                    auraHits = 0;
                }

                lastTarget = target;
            }
        }
    }

    private long lastHackCheck = 0;
    private static final long CHECK_INTERVAL = 30000L;

    private void detectKnownHackMods() {
        String[] knownHacks = {
                "net.ccbluex.liquidbounce.LiquidBounce",
                "net.ccbluex.liquidbounce.features.module.modules.combat.KillAura",
                "com.krazzzz.mods.huzuni.Huzuni",
                "net.minecraft.client.wurst.WurstClient",
                "net.minecraft.client.wurst.mods.combat.KillauraMod",
                "net.minecraft.client.jigsaw.Jigsaw",
                "jigsaw.modules.combat.KillAura",
                "net.aristois.Aristois",
                "net.aristois.mods.killaura.KillAura",
                "me.sigma.client.Sigma",
                "me.sigma.modules.impl.combat.KillAura",
                "com.aresclient.Ares",
                "com.aresclient.module.modules.combat.KillAura",
                "org.vape.Vape",
                "vape.VapeMain",
                "me.futureclient.client.FutureClient",
                "me.futureclient.client.modules.combat.KillAura",
                "com.impact.client.Impact",
                "com.impact.features.modules.combat.Killaura",
                "com.inertia.client.Inertia",
                "com.inertia.modules.combat.KillAura",
                "com.flux.Flux",
                "com.flux.client.modules.combat.KillAura",
                "org.bonge.BleachHack",
                "org.bonge.mods.combat.KillAuraMod",
                "me.rush.RusherHack",
                "com.tool.box.ToolboxModLoader",
                "com.bigrat.client.BigRat",
                "me.phobos.api.Phobos",
                "dev.client.ClientMain",
                "cheatbase.Client",
                "hacks.client.Client",
                "exploit.client.Main"
        };

        long now = System.currentTimeMillis();
        if (now - lastHackCheck < CHECK_INTERVAL) return;
        lastHackCheck = now;

        for (String name : knownHacks) {
            try {
                Class.forName(name);
                sendWebhook("⚠️ Mod Hack Detectado", "Jogador: " + mc.thePlayer.getName() + "\nMod: `" + name + "`");
            } catch (ClassNotFoundException ignored) {}
        }
    }

    private int inventoryOpenCount = 0;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiInventory) {
            inventoryOpenCount++;
            if (inventoryOpenCount > 15) {
                sendWebhook("⚠️ Abertura excessiva de inventário",
                        "Jogador: " + mc.thePlayer.getName() + "\nDetectado possível uso de macro duper.");
                inventoryOpenCount = 0;
            }
        }
    }

    private long lastSprintStart = 0;
    private long lastAttackTime = 0;
    private long lastSprintExploitAlert = 0;
    private int suspiciousSprintHits = 0;

    private void detectSprintHitExploit() {
        long now = System.currentTimeMillis();

        if (mc.thePlayer.isSprinting()) {
            if (now - lastSprintStart > 500) {
                lastSprintStart = now;
            }
        }

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (now - lastAttackTime > 200) {
                long delay = now - lastSprintStart;

                if (delay >= 100 && delay < 350) {
                    suspiciousSprintHits++;

                    if (suspiciousSprintHits >= 5 && now - lastSprintExploitAlert > 10000) {
                        sendWebhook("⚠️ Suspeita de TriggerBot / Sprint-Hit Exploit",
                                "Jogador: " + mc.thePlayer.getName() +
                                        "\nAtacou " + delay + "ms após iniciar corrida.\nRepetições: " + suspiciousSprintHits);
                        lastSprintExploitAlert = now;
                        suspiciousSprintHits = 0;
                    }
                } else {
                    suspiciousSprintHits = 0;
                }

                lastAttackTime = now;
            }
        }
    }

    private final ItemStack[] lastArmorContents = new ItemStack[4];
    private int fastArmorSwaps = 0;
    private long lastArmorSwapTime = 0;

    private void detectAutoArmor() {
        ItemStack[] currentArmor = mc.thePlayer.inventory.armorInventory;
        long now = System.currentTimeMillis();

        boolean changed = false;

        for (int i = 0; i < currentArmor.length; i++) {
            ItemStack current = currentArmor[i];
            ItemStack last = lastArmorContents[i];

            if (current != null && (last == null || !ItemStack.areItemStacksEqual(current, last))) {
                changed = true;
            }

            lastArmorContents[i] = current != null ? current.copy() : null;
        }

        if (changed) {
            if (now - lastArmorSwapTime < 100) {
                fastArmorSwaps++;
                if (fastArmorSwaps > 3) {
                    sendWebhook("⚠️ Suspeita de AutoArmor",
                            "Jogador: " + mc.thePlayer.getName() + "\nTrocas de armadura rápidas detectadas.");
                    fastArmorSwaps = 0;
                }
            } else {
                fastArmorSwaps = 0;
            }
            lastArmorSwapTime = now;
        }
    }

    private double lastY = -1;
    private int flyTicks = 0;

    private void detectFlyHack() {
        if (mc.thePlayer.onGround) {
            flyTicks = 0;
            lastY = -1;
            return;
        }

        double currentY = mc.thePlayer.posY;
        if (lastY != -1 && Math.abs(currentY - lastY) > 2) {
            flyTicks++;
            if (flyTicks > 7) {
                sendWebhook("⚠️ Suspeita de Fly/LongJump",
                        "Jogador: " + mc.thePlayer.getName() + "\nMovimentação vertical incomum detectada.");
                flyTicks = 0;
            }
        }

        lastY = currentY;
    }

    private void detectNoSlow() {
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.moveForward > 0.1f) {
            sendWebhook("⚠️ Suspeita de NoSlow",
                    "Jogador: " + mc.thePlayer.getName() + "\nMovendo-se enquanto segura item utilizável.");
        }
    }

    private double lastPosX = -1, lastPosZ = -1;
    private int blinkMoves = 0;

    private void detectBlink() {
        if (lastPosX != -1 && lastPosZ != -1) {
            double dx = Math.abs(mc.thePlayer.posX - lastPosX);
            double dz = Math.abs(mc.thePlayer.posZ - lastPosZ);

            if (dx > 10 || dz > 10) {
                blinkMoves++;
                if (blinkMoves > 2) {
                    sendWebhook("⚠️ Suspeita de Blink Hack",
                            "Jogador: " + mc.thePlayer.getName() + "\nTeleportes rápidos detectados.");
                    blinkMoves = 0;
                }
            } else {
                blinkMoves = 0;
            }
        }

        lastPosX = mc.thePlayer.posX;
        lastPosZ = mc.thePlayer.posZ;
    }
}
