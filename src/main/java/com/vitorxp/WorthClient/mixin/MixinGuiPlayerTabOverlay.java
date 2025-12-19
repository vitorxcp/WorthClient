package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.socket.ClientSocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {

    private static final ResourceLocation WORTH_ICON = new ResourceLocation("worthclient", "icons/icon.png");
    private static final Pattern CLEANER = Pattern.compile("[^a-zA-Z0-9_]");

    private final Set<String> renderedIconsThisFrame = new HashSet<>();
    private NetworkPlayerInfo currentPlayerInfo;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void onRenderHeader(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        renderedIconsThisFrame.clear();
    }

    @Redirect(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;getPlayerName(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;")
    )
    public String onGetPlayerName(GuiPlayerTabOverlay instance, NetworkPlayerInfo networkPlayerInfoIn) {
        this.currentPlayerInfo = networkPlayerInfoIn;
        return instance.getPlayerName(networkPlayerInfoIn);
    }

    @Redirect(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I")
    )
    public int onCalcWidth(FontRenderer instance, String text) {
        int originalWidth = instance.getStringWidth(text);

        if (currentPlayerInfo != null) {
            String cleanName = cleanString(text);
            String myNick = "";
            try {
                if (Minecraft.getMinecraft().getSession() != null) {
                    myNick = Minecraft.getMinecraft().getSession().getUsername();
                }
            } catch (Exception ignored) {}

            if (checkMatch(cleanName, myNick)) {
                return originalWidth + 12;
            }
        }
        return originalWidth;
    }

    @Redirect(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I")
    )
    public int onDrawStringWithShadow(FontRenderer instance, String text, float x, float y, int color) {
        if (currentPlayerInfo != null) {
            String cleanDisplay = cleanString(text);
            String myNick = "";
            try {
                if (Minecraft.getMinecraft().getSession() != null) {
                    myNick = Minecraft.getMinecraft().getSession().getUsername();
                }
            } catch (Exception ignored) {}

            boolean isUser = checkMatch(cleanDisplay, myNick);

            boolean alreadyRendered = renderedIconsThisFrame.contains(cleanDisplay);

            if (isUser && !alreadyRendered) {
                renderedIconsThisFrame.add(cleanDisplay);
                Minecraft mc = Minecraft.getMinecraft();

                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableAlpha();

                try {
                    mc.getTextureManager().bindTexture(WORTH_ICON);
                    drawModalRectWithCustomSizedTexture((int)x + 1, (int)y, 0, 0, 8, 8, 8, 8);
                } catch (Exception ignored) {}

                GlStateManager.bindTexture(0);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.popMatrix();
                GL11.glPopAttrib();

                this.currentPlayerInfo = null;
                return instance.drawStringWithShadow(text, x + 11, y, color);
            }
        }

        this.currentPlayerInfo = null;
        return instance.drawStringWithShadow(text, x, y, color);
    }

    private String cleanString(String input) {
        if (input == null) return "";
        return CLEANER.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(input)).replaceAll("").toLowerCase();
    }

    private boolean checkMatch(String cleanDisplay, String myNick) {
        if (cleanDisplay.isEmpty()) return false;
        if (cleanDisplay.contains(cleanString("Perfil: " + myNick))) return false;
        if (myNick != null && cleanDisplay.contains(cleanString(myNick))) return true;

        if (ClientSocket.playerCosmetics != null) {

            for (String socketUser : ClientSocket.playerCosmetics.keySet()) {
                if (socketUser != null && cleanDisplay.contains(socketUser)) {
                    return true;
                }
            }
        }
        return false;
    }
}