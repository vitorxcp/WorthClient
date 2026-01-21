package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.stream.GuiStreamUnavailable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiStreamUnavailable.class)
public class MixinGuiStreamUnavailable extends GuiScreen {

    /**
     * @author VitorXP
     * @reason Fix Crash: A API da Twitch morreu, então essa tela causa NullPointerException.
     * Esta correção fecha a tela imediatamente se ela tentar abrir.
     */
    @Overwrite
    public void initGui() {
        if (this.mc.thePlayer != null) {
            this.mc.displayGuiScreen(null);
        } else {
            this.mc.displayGuiScreen(new com.vitorxp.WorthClient.gui.GuiClientMainMenu());
        }
    }

    /**
     * @author VitorXP
     * @reason Fix Crash: Impede que o código original desenhe texto nulo.
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Twitch Desativada", this.width / 2, this.height / 2, 0xFF0000);
    }
}