package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public abstract void updateDisplay();
    @Shadow public int displayWidth;
    @Shadow public int displayHeight;
    @Shadow public abstract Framebuffer getFramebuffer();

    private static WorthLoadingGUI startupGui;

    /**
     * @author vitorxp
     * @reason Tela de carregamento estilo Lunar Client (Tela Cheia + ScaledResolution)
     */
    @Overwrite
    public void drawSplashScreen(TextureManager textureManagerInstance) {
        if (startupGui == null) {
            startupGui = new WorthLoadingGUI(Minecraft.getMinecraft());
        }

        ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft());
        int i = scaledRes.getScaleFactor();

        GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledRes.getScaledWidth_double(), scaledRes.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        startupGui.update("Carregando o Jogo...", 0.5f);
        startupGui.drawScreen(scaledRes.getScaledWidth(), scaledRes.getScaledHeight());

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        this.updateDisplay();

        Thread.yield();
    }

    @Redirect(method = { "startGame", "func_71384_a", "refreshResources", "func_147115_a" }, at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"), require = 0)
    private void cancelForcedGC() {}

    @Redirect(method = {"runGameLoop", "func_71411_J"}, at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V"), require = 0)
    private void removeThreadYield() {}
}