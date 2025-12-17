package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.GuiScreenWorthPacks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    protected void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.enabled) {
            if (button.id == 105) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiScreenWorthPacks(this));

                ci.cancel();
            }
        }
    }
}