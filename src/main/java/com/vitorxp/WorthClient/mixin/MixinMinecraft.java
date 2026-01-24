package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import com.vitorxp.WorthClient.utils.LoadingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.IStream;
import net.minecraft.client.stream.NullStream;
import org.lwjgl.opengl.Display;
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
    @Shadow public GuiScreen currentScreen;
    @Shadow public GameSettings gameSettings;
    @Shadow public IStream stream;
    @Shadow public TextureManager renderEngine;

    private WorthLoadingGUI worthSplashScreen;

    @Inject(method = "startGame", at = @At("HEAD"))
    private void initSplash(CallbackInfo ci) {
        worthSplashScreen = new WorthLoadingGUI();
        updateSplash("Iniciando Engine...", 0.0f);
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;initializeTextures()V", shift = At.Shift.AFTER))
    private void onDisplayInit(CallbackInfo ci) {
        updateSplash("Configurando OpenGL...", 0.15f);
    }

    @Inject(method = "startGame", at = @At(value = "NEW", target = "net/minecraft/client/renderer/texture/TextureManager"))
    private void onTextureManagerStart(CallbackInfo ci) {
        updateSplash("Carregando Texturas...", 0.30f);
    }

    @Inject(method = "startGame", at = @At(value = "NEW", target = "net/minecraft/client/renderer/entity/RenderManager"))
    private void onRenderManager(CallbackInfo ci) {
        updateSplash("Preparando Entidades...", 0.80f);
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    public void drawSplashScreen(TextureManager textureManagerInstance) {
        String txt = LoadingUtils.getCurrentText();
        float prog = LoadingUtils.getCurrentProgress();

        if (txt.equals("Iniciando...")) txt = "Carregando Recursos...";

        if (prog < 0.2f) prog = 0.45f;

        updateSplash(txt, prog);
    }

    private void updateSplash(String text, float percent) {
        LoadingUtils.setCurrentText(text);
        LoadingUtils.setCurrentProgress(percent);

        if (worthSplashScreen == null) worthSplashScreen = new WorthLoadingGUI();

        try {
            worthSplashScreen.drawProgress(text, percent);

            if (Display.isCreated()) {
                Display.update();
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "checkGLError", at = @At("HEAD"))
    private void onCheckGLError(String message, CallbackInfo ci) {
        if (Minecraft.getMinecraft().theWorld == null && worthSplashScreen != null) {
            updateSplash(LoadingUtils.getCurrentText(), LoadingUtils.getCurrentProgress());
        }
    }

    @Redirect(method = {"freeMemory", "shutdown"}, at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"))
    private void smartGC() {
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        if ((float) used / max > 0.90F) {
            System.gc();
        }
    }

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (worthSplashScreen != null && guiScreenIn != null && !(guiScreenIn instanceof WorthLoadingGUI)) {
            worthSplashScreen = null;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void initStream() {
        this.stream = new NullStream(new Throwable("Twitch Support Removed by WorthClient"));
    }
}