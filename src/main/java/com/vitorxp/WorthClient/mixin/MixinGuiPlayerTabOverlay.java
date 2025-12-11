package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.socket.ClientSocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.regex.Pattern;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {

    private static final ResourceLocation WORTH_ICON = new ResourceLocation("worthclient", "icons/icon.png");
    private NetworkPlayerInfo currentPlayerInfo;

    private static final Pattern CLEANER = Pattern.compile("(?i)§[0-9a-fk-or]|[^a-zA-Z0-9_]");

    @Redirect(
            method = {"renderPlayerlist", "func_175249_a"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;getPlayerName(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;")
    )
    public String onGetPlayerName(GuiPlayerTabOverlay instance, NetworkPlayerInfo networkPlayerInfoIn) {
        this.currentPlayerInfo = networkPlayerInfoIn;
        return instance.getPlayerName(networkPlayerInfoIn);
    }

    @Redirect(
            method = {"renderPlayerlist", "func_175249_a"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I")
    )
    public int onDrawStringWithShadow(FontRenderer instance, String text, float x, float y, int color) {

        if (currentPlayerInfo != null) {
            String rawName = currentPlayerInfo.getGameProfile().getName();

            String cleanName = CLEANER.matcher(rawName).replaceAll("");

            boolean isUser = checkUser(cleanName);

            if (isUser) {
                Minecraft mc = Minecraft.getMinecraft();

                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();

                try {
                    mc.getTextureManager().bindTexture(WORTH_ICON);

                    this.zLevel += 100;

                    drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, 8, 8, 8, 8);

                    this.zLevel -= 100;
                } catch (Exception ignored) {}

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                return instance.drawStringWithShadow(text, x + 12, y, color);
            }
        }

        return instance.drawStringWithShadow(text, x, y, color);
    }

    private boolean checkUser(String cleanName) {
        if (ClientSocket.usersUsingClient.isEmpty()) return false;

        if (Minecraft.getMinecraft().getSession().getUsername().equalsIgnoreCase(cleanName)) return true;

        for (String socketUser : ClientSocket.usersUsingClient) {
            if (cleanName.toLowerCase().contains(socketUser.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}