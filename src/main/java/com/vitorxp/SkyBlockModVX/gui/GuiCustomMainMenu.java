package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Random;

public class GuiCustomMainMenu extends GuiScreen {

    private final ResourceLocation customLogo = new ResourceLocation("skyblockmodvx", "textures/gui/logo.png");
    private final String[] frases = {
            "SkyBlock, mas mais gostoso.",
            "Detector de macro?! 👀",
            "Mod feito por VitorXP!",
            "RedeWorth é top. 🧠",
            "FPS modo turbo!",
            "Você viu um Mutante?"
    };
    private String fraseAtual;
    private long lastFraseChange;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int y = this.height / 4 + 48;

        this.buttonList.add(new GuiButton(0, centerX - 100, y, "Singleplayer"));
        this.buttonList.add(new GuiButton(1, centerX - 100, y + 24, "Multiplayer"));
        this.buttonList.add(new GuiButton(2, centerX - 100, y + 48, "Mods"));
        this.buttonList.add(new GuiButton(4, centerX - 100, y + 72, "Options"));
        this.buttonList.add(new GuiButton(5, centerX - 100, y + 96, "Quit"));

        this.fraseAtual = frases[new Random().nextInt(frases.length)];
        this.lastFraseChange = System.currentTimeMillis();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);

        drawGradientRect(0, 0, width, height, 0xFF2E003E, 0xFF000000);

        //mc.getTextureManager().bindTexture(customLogo);
        //GlStateManager.color(1, 1, 1, 1);
        //drawModalRectWithCustomSizedTexture(width / 2 - 100, height / 4 - 60, 0, 0, 200, 80, 200, 80);

        if (System.currentTimeMillis() - lastFraseChange > 5000) {
            fraseAtual = frases[new Random().nextInt(frases.length)];
            lastFraseChange = System.currentTimeMillis();
        }

        int fraseAlpha = (int)(Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI) * 255);
        drawCenteredString(fontRendererObj, fraseAtual, width / 2, height - 40, (fraseAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: // Singleplayer
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 1: // Multiplayer
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 2: // Mods (não funciona no Forge 1.8.9 normalmente)
                break;
            case 4: // Options
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case 5: // Quit
                mc.shutdown();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
