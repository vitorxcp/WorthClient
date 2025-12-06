package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.LoadingScreenHook;
import com.vitorxp.WorthClient.gui.WorthLoadingGUI; // Import necessário
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingScreenRenderer.class)
public class MixinLoadingScreenRenderer {

    @Shadow private Minecraft mc;

    @Inject(method = "setLoadingProgress", at = @At("HEAD"), cancellable = true)
    private void hookProgress(int progress, CallbackInfo ci) {
        if (LoadingScreenHook.customGUI == null) {
            LoadingScreenHook.customGUI = new WorthLoadingGUI(mc);
        }

        float p = Math.min(1f, Math.max(0f, progress / 100f));
        LoadingScreenHook.customGUI.update(LoadingScreenHook.customGUI.text, p);

        ScaledResolution res = new ScaledResolution(mc);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        int scaleFactor = res.getScaleFactor();

        Framebuffer framebuffer = mc.getFramebuffer();
        if (framebuffer != null) {
            framebuffer.unbindFramebuffer();
        }

        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        LoadingScreenHook.customGUI.setWorldAndResolution(mc, width, height);
        LoadingScreenHook.customGUI.drawScreen(0, 0, 0);

        mc.updateDisplay();
        ci.cancel();
    }

    @Inject(method = "displayLoadingString", at = @At("HEAD"), cancellable = true)
    private void hookDisplayText(String text, CallbackInfo ci) {
        if (LoadingScreenHook.customGUI == null) {
            LoadingScreenHook.customGUI = new WorthLoadingGUI(mc);
        }

        LoadingScreenHook.customGUI.update(text, LoadingScreenHook.customGUI.progress);
        ci.cancel();
    }
}