package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiDisconnected.class)
public class MixinGuiDisconnected extends GuiScreen {

    @Shadow private GuiScreen parentScreen;
    @Shadow private IChatComponent message;
    @Shadow private List<String> multilineMessage;
    @Shadow private int field_175353_i;

    @Inject(method = "initGui", at = @At("RETURN"))
    public void onInitGui(CallbackInfo ci) {
        if (!this.buttonList.isEmpty()) {
            GuiButton backButton = this.buttonList.get(0);
            if (backButton != null) {
                backButton.yPosition = this.height / 2 + this.field_175353_i / 2 + 20;
            }
        }

        this.buttonList.add(new GuiButton(
                1,
                this.width / 2 - 100,
                (this.height / 2 + this.field_175353_i / 2 + 70) - 24,
                "Reconectar"
        ));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    protected void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 1) {
            ServerData serverData = this.mc.getCurrentServerData();

            if (serverData != null) {
                this.mc.displayGuiScreen(new GuiConnecting(this.parentScreen, this.mc, serverData));
            } else {
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }
}