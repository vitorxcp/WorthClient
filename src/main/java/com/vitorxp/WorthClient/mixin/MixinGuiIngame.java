package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.hud.ScoreboardHUD;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void onRenderVignette(float lightLevel, ScaledResolution scaledRes, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * @author vitorxp
     * @reason Cancela a Scoreboard Vanilla para que possamos desenhar a nossa Customizável (ScoreboardHUD)
     */
    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
        if (ScoreboardHUD.toggled) {
            ci.cancel();
        }
    }
}