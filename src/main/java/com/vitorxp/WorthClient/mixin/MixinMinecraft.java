package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WindowUtils;
import com.vitorxp.WorthClient.config.AnimationsConfig;
import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import com.vitorxp.WorthClient.utils.LoadingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.profiler.Profiler;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 1)
public abstract class MixinMinecraft {

    @Shadow public int displayWidth;
    @Shadow public int displayHeight;
    @Shadow private int leftClickCounter;
    @Shadow public GuiScreen currentScreen;
    @Shadow public net.minecraft.client.multiplayer.WorldClient theWorld;
    @Shadow public TextureManager renderEngine;
    @Shadow public GameSettings gameSettings;
    @Shadow public Profiler mcProfiler;
    @Shadow public abstract void checkGLError(String message);

    private static WorthLoadingGUI startupGui;

    @Inject(
            method = "startGame",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OpenGlHelper;initializeTextures()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onStartGameEarly(CallbackInfo ci) {
        WindowUtils.applyWindowStyle();

        LoadingUtils.setCurrentText("Inicializando Engine...");
        LoadingUtils.setCurrentProgress(0.0f);

        if (startupGui == null) {
            startupGui = new WorthLoadingGUI();
        }

        try {
            int w = Display.getWidth();
            int h = Display.getHeight();

            GlStateManager.viewport(0, 0, w, h);
            GlStateManager.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, w / 2.0, h / 2.0, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            startupGui.drawManual(w, h);

            Display.update();

        } catch (Exception ignored) {
        }
    }

    /**
     * @author vitorxp
     * @reason Tela de carregamento estilo Lunar Client (Modo Normal)
     */
    @Overwrite
    public void drawSplashScreen(TextureManager textureManagerInstance) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen instanceof WorthLoadingGUI) {
            startupGui = (WorthLoadingGUI) mc.currentScreen;
        } else if (startupGui == null) {
            startupGui = new WorthLoadingGUI();
        }

        String text = LoadingUtils.getCurrentText();
        float progress = LoadingUtils.getCurrentProgress();

        updateBar(text, progress);
    }

    private void updateBar(String text, float progress) {
        if (this.theWorld != null) {
            startupGui = null;
            return;
        }

        if (this.currentScreen != null && !(this.currentScreen instanceof WorthLoadingGUI)) {
            startupGui = null;
            return;
        }

        if (startupGui == null) return;

        try {
            if (Minecraft.getMinecraft().displayWidth <= 0) return;

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

            startupGui.drawContent(w, h);

            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();

            Display.update();

        } catch (Exception ignored) {
        }
    }

    @Inject(method = "checkGLError", at = @At("HEAD"), cancellable = true)
    private void onCheckGLError(String message, CallbackInfo ci) {
        if (startupGui != null) {
            if (this.theWorld != null || (this.currentScreen != null && !(this.currentScreen instanceof WorthLoadingGUI))) {
                startupGui = null;
            } else {
                String currentStep = LoadingUtils.getCurrentText();
                float currentProgress = LoadingUtils.getCurrentProgress();
                updateBar(currentStep, currentProgress);
            }
        }
    }

    @Redirect(method = {"freeMemory", "shutdown"}, at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"))
    private void smartGC() {
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;

        if ((float)used / max > 0.85F) {
            System.gc();
        }
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void onClickMouse(CallbackInfo ci) {
        this.leftClickCounter = 0;
    }

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (startupGui != null && guiScreenIn != null && !(guiScreenIn instanceof WorthLoadingGUI)) {
            startupGui = null;
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"))
    private void onSendClickBlock(boolean leftClick, CallbackInfo ci) {
        if (AnimationsConfig.enabled && AnimationsConfig.userItemWhileDigging && !leftClick) {}
    }

    @Inject(method = "dispatchKeypresses", at = @At("HEAD"))
    public void killTwitchKeys(CallbackInfo ci) {
        if (this.gameSettings.keyBindStreamStartStop.getKeyCode() != 0)
            this.gameSettings.keyBindStreamStartStop.setKeyCode(0);
        if (this.gameSettings.keyBindStreamCommercials.getKeyCode() != 0)
            this.gameSettings.keyBindStreamCommercials.setKeyCode(0);
        if (this.gameSettings.keyBindStreamToggleMic.getKeyCode() != 0)
            this.gameSettings.keyBindStreamToggleMic.setKeyCode(0);
    }

    /**
     * @author VitorXP
     * @reason Smoother FPS Loop (Remove stuttering do LWJGL 2)
     */
    @Overwrite
    public void updateDisplay() {
        this.mcProfiler.startSection("display_update");
        Display.update();
        this.checkGLError("Post render");
        this.mcProfiler.endSection();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onWorldChange(net.minecraft.client.multiplayer.WorldClient world, String message, CallbackInfo ci) {
        System.gc();
    }
}