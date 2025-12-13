package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public abstract void updateDisplay();
    @Shadow public int displayWidth;
    @Shadow public int displayHeight;

    private static WorthLoadingGUI startupGui;
    private static long startTime = System.currentTimeMillis();

    /**
     * @author vitorxp
     * @reason Tela de carregamento estilo Lunar Client
     */
    @Overwrite
    public void drawSplashScreen(TextureManager textureManagerInstance) {
        if (startupGui == null) {
            startupGui = new WorthLoadingGUI(Minecraft.getMinecraft());
            startTime = System.currentTimeMillis();
        }
        updateBar("Iniciando...", 0.0f);
    }

    private void updateBar(String text, float progress) {
        if (startupGui == null) return;

        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
        GlStateManager.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        startupGui.update(text, progress);
        startupGui.drawScreen(w, h);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        this.updateDisplay();
        Thread.yield();
    }

    @Inject(method = "checkGLError", at = @At("HEAD"))
    private void onCheckGLError(String message, CallbackInfo ci) {
        if (startupGui != null && Minecraft.getMinecraft().currentScreen == null) {

            long now = System.currentTimeMillis();
            float rawProgress = (now - startTime) / 10000f;

            float visualProgress;
            if (rawProgress < 0.5f) {
                visualProgress = rawProgress * 1.2f;
            } else {
                visualProgress = 0.6f + (rawProgress - 0.5f) * 0.5f;
            }

            if (visualProgress > 0.99f) visualProgress = 0.99f;

            String step = "Iniciando...";
            if (visualProgress > 0.05) step = "Lendo Configurações...";
            if (visualProgress > 0.15) step = "Carregando Bibliotecas...";
            if (visualProgress > 0.30) step = "Verificando Assets...";
            if (visualProgress > 0.45) step = "Construindo Itens...";
            if (visualProgress > 0.60) step = "Processando Texturas...";
            if (visualProgress > 0.75) step = "Renderizando Modelos...";
            if (visualProgress > 0.85) step = "Finalizando...";
            if (visualProgress > 0.98) step = "Iniciando...";

            updateBar(step, visualProgress);
        }
    }

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (startupGui != null && guiScreenIn != null) {
            startupGui = null;
        }
    }

    @Redirect(method = { "startGame", "func_71384_a", "refreshResources", "func_147115_a" }, at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"), require = 0)
    private void cancelForcedGC() {}

    @Redirect(method = {"runGameLoop", "func_71411_J"}, at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V"), require = 0)
    private void removeThreadYield() {}
}