package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    /**
     * @author vitorxp
     * @reason OTIMIZAÇÃO: Chat "Clear" (Fast Chat).
     * Impede que o jogo desenhe o fundo preto atrás de cada linha do chat.
     * Isso reduz drasticamente o uso da GPU em servidores com muito spam no chat.
     */
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void disableChatBackground(int left, int top, int right, int bottom, int color) {
        // Não desenha o fundo preto.
        // O texto continua sendo desenhado normalmente pelas chamadas de drawString subsequentes.
    }
}