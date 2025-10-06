package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ServerIcon {

    private final ServerData server;
    private ResourceLocation resourceLocation;
    private DynamicTexture texture;

    public ServerIcon(ServerData server) {
        this.server = server;
        loadIcon();
    }

    private void loadIcon() {
        if (server.getBase64EncodedIconData() != null) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(server.getBase64EncodedIconData());
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage bufferedImage = ImageIO.read(bis);
                Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high");

                this.texture = new DynamicTexture(bufferedImage);
                this.resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("server_icon_" + server.serverIP, this.texture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(int x, int y) {
        if (this.resourceLocation != null) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
            GuiScreen.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 32, 32, 32, 32);
        }
    }

    public void cleanup() {
        if (this.texture != null) {
            this.texture.deleteGlTexture();
        }
    }
}