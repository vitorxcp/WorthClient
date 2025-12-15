package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.manager.AutoTextManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class GuiAutoText extends GuiScreen {

    private final GuiScreen parent;
    private final Color themeColor = new Color(158, 96, 32);
    private final int colBackgroundTop = 0xF0141414;
    private final int colBackgroundBottom = 0xF0230F05;
    private int guiWidth = 600;
    private int guiHeight = 400;
    private int guiLeft;
    private int guiTop;
    private float currentScale = 0.0f;
    private boolean closing = false;
    private float scrollOffset = 0;
    private float maxScroll = 0;
    private GuiTextField messageField;
    private boolean isBinding = false;
    private int currentKey = Keyboard.KEY_NONE;

    public GuiAutoText(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;
        this.currentScale = 0.0f;
        this.closing = false;

        Keyboard.enableRepeatEvents(true);

        messageField = new GuiTextField(0, fontRendererObj, guiLeft + 40, guiTop + 75, guiWidth - 180, 20);
        messageField.setMaxStringLength(255);
        messageField.setText("/");
        messageField.setEnableBackgroundDrawing(false);
        messageField.setFocused(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) scrollOffset += 25;
            else scrollOffset -= 25;
            clampScroll();
        }
    }

    private void clampScroll() {
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);

        if (closing) {
            currentScale = lerp(currentScale, 0f, 0.5f);
            if (currentScale < 0.1f) {
                mc.displayGuiScreen(parent);
                return;
            }
        } else {
            currentScale = lerp(currentScale, 1f, 0.4f);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(currentScale, currentScale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);

        drawRoundedRect(guiLeft, guiTop, guiWidth, guiHeight, 15, colBackgroundTop);
        drawRoundedOutline(guiLeft, guiTop, guiWidth, guiHeight, 15, 2.0f, themeColor.getRGB());
        drawCenteredString(fontRendererObj, "Gerenciador de Macros (AutoText)", width / 2, guiTop + 20, themeColor.getRGB());
        boolean hoverBack = isHover(mouseX, mouseY, guiLeft + 20, guiTop + 20, 50, 20);
        drawRoundedRect(guiLeft + 20, guiTop + 20, 50, 20, 5, hoverBack ? 0xFF555555 : 0xFF333333);
        drawCenteredString(fontRendererObj, "< Voltar", guiLeft + 45, guiTop + 26, 0xFFFFFFFF);
        drawRect(guiLeft + 20, guiTop + 55, guiLeft + guiWidth - 20, guiTop + 56, 0x40FFFFFF);
        drawRoundedRect(guiLeft + 30, guiTop + 70, guiWidth - 160, 30, 6, 0xFF151515);
        messageField.drawTextBox();
        if (messageField.getText().isEmpty() && !messageField.isFocused()) {
            fontRendererObj.drawString("Digite o comando ou mensagem...", guiLeft + 40, guiTop + 75, 0xFF555555);
        }

        int keyBtnX = guiLeft + guiWidth - 120;
        int keyBtnY = guiTop + 70;
        int keyBtnW = 90;
        int keyBtnH = 30;
        boolean hoverKey = isHover(mouseX, mouseY, keyBtnX, keyBtnY, keyBtnW, keyBtnH);

        int keyColor = isBinding ? 0xFFFFAA00 : (hoverKey ? 0xFF444444 : 0xFF222222);
        drawRoundedRect(keyBtnX, keyBtnY, keyBtnW, keyBtnH, 6, keyColor);
        String keyText = isBinding ? "..." : Keyboard.getKeyName(currentKey);
        drawCenteredString(fontRendererObj, keyText, keyBtnX + keyBtnW / 2, keyBtnY + 11, isBinding ? 0xFF000000 : 0xFFFFFFFF);

        int addBtnX = guiLeft + 30;
        int addBtnY = guiTop + 110;
        int addBtnW = guiWidth - 60;
        int addBtnH = 25;
        boolean hoverAdd = isHover(mouseX, mouseY, addBtnX, addBtnY, addBtnW, addBtnH);

        drawRoundedRect(addBtnX, addBtnY, addBtnW, addBtnH, 6, hoverAdd ? 0xFF2ECC71 : 0xFF27AE60);
        drawCenteredString(fontRendererObj, "ADICIONAR MACRO", width / 2, addBtnY + 9, 0xFFFFFFFF);

        int listStartY = guiTop + 150;
        int listHeight = guiHeight - 170;
        int entryHeight = 35;

        int totalContentHeight = AutoTextManager.macros.size() * (entryHeight + 5);
        this.maxScroll = Math.max(0, totalContentHeight - listHeight);
        clampScroll();

        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                (guiLeft + 30) * scaleFactor,
                (mc.displayHeight - (listStartY + listHeight) * scaleFactor),
                (guiWidth - 60) * scaleFactor,
                listHeight * scaleFactor
        );

        int currentY = (int) (listStartY + scrollOffset);

        if (AutoTextManager.macros.isEmpty()) {
            drawCenteredString(fontRendererObj, "Nenhuma macro configurada.", width / 2, listStartY + 20, 0xFFAAAAAA);
        } else {
            for (int i = 0; i < AutoTextManager.macros.size(); i++) {
                AutoTextManager.TextMacro macro = AutoTextManager.macros.get(i);

                if (currentY + entryHeight > listStartY && currentY < listStartY + listHeight) {
                    int entryX = guiLeft + 30;
                    int entryW = guiWidth - 60;

                    boolean hoverItem = isHover(mouseX, mouseY, entryX, currentY, entryW, entryHeight);
                    drawRoundedRect(entryX, currentY, entryW, entryHeight, 6, hoverItem ? 0xFF353535 : 0xFF252525);

                    drawRoundedRect(entryX + 10, currentY + 7, 50, 21, 4, 0xFF151515);
                    drawCenteredString(fontRendererObj, Keyboard.getKeyName(macro.keyCode), entryX + 35, currentY + 14, themeColor.getRGB());

                    String displayMsg = fontRendererObj.trimStringToWidth(macro.message, entryW - 100);
                    fontRendererObj.drawString(displayMsg, entryX + 70, currentY + 14, 0xFFDDDDDD);

                    int delBtnSize = 20;
                    int delBtnX = entryX + entryW - 30;
                    int delBtnY = currentY + 7;
                    boolean hoverDel = isHover(mouseX, mouseY, delBtnX, delBtnY, delBtnSize, delBtnSize);

                    drawRoundedRect(delBtnX, delBtnY, delBtnSize, delBtnSize, 4, hoverDel ? 0xFFE74C3C : 0xFFC0392B);
                    drawCenteredString(fontRendererObj, "X", delBtnX + 10, delBtnY + 6, 0xFFFFFFFF);
                }
                currentY += (entryHeight + 5);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (maxScroll > 0) {
            int scrollBarH = (int) ((float) listHeight / totalContentHeight * listHeight);
            if (scrollBarH < 30) scrollBarH = 30;
            int scrollBarY = listStartY + (int)((-scrollOffset / maxScroll) * (listHeight - scrollBarH));
            drawRoundedRect(guiLeft + guiWidth - 25, scrollBarY, 4, scrollBarH, 2, 0x80FFFFFF);
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        messageField.mouseClicked(mouseX, mouseY, mouseButton);

        if (isHover(mouseX, mouseY, guiLeft + 20, guiTop + 20, 50, 20)) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            closing = true;
            return;
        }

        int keyBtnX = guiLeft + guiWidth - 120;
        int keyBtnY = guiTop + 70;
        if (isHover(mouseX, mouseY, keyBtnX, keyBtnY, 90, 30)) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            isBinding = !isBinding;
            return;
        } else if (isBinding) {
            isBinding = false;
        }

        int addBtnX = guiLeft + 30;
        int addBtnY = guiTop + 110;
        if (isHover(mouseX, mouseY, addBtnX, addBtnY, guiWidth - 60, 25)) {
            String msg = messageField.getText();
            if (msg.isEmpty() || currentKey == Keyboard.KEY_NONE) {
                NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Defina mensagem e tecla!");
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("note.bass"), 1.0F));
            } else {
                AutoTextManager.macros.add(new AutoTextManager.TextMacro(msg, currentKey));
                AutoTextManager.save();
                NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Macro adicionada!");
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("random.orb"), 1.0F));

                messageField.setText("");
                currentKey = Keyboard.KEY_NONE;
            }
            return;
        }

        int listStartY = guiTop + 150;
        int listHeight = guiHeight - 170;
        int entryHeight = 35;
        int currentY = (int) (listStartY + scrollOffset);

        if (mouseY >= listStartY && mouseY <= listStartY + listHeight) {
            for (int i = 0; i < AutoTextManager.macros.size(); i++) {
                int entryX = guiLeft + 30;
                int entryW = guiWidth - 60;
                int delBtnX = entryX + entryW - 30;
                int delBtnY = currentY + 7;

                if (isHover(mouseX, mouseY, delBtnX, delBtnY, 20, 20)) {
                    AutoTextManager.macros.remove(i);
                    AutoTextManager.save();
                    NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Configurações salvas!");
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    return;
                }
                currentY += (entryHeight + 5);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isBinding) {
            if (keyCode == 1) {
                isBinding = false;
            } else {
                currentKey = keyCode;
                isBinding = false;
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }
            return;
        }

        if (messageField.isFocused()) {
            messageField.textboxKeyTyped(typedChar, keyCode);
        }

        if (keyCode == 1) {
            closing = true;
        }
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static void drawGradientRoundedRect(float x, float y, float width, float height, float radius, int colorTop, int colorBottom) {
        drawRoundedRect(x, y, width, height, radius, colorTop);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);

        drawCircleSector(x + radius, y + radius, radius, 180, 270);
        drawCircleSector(x + width - radius, y + radius, radius, 90, 180);
        drawCircleSector(x + width - radius, y + height - radius, radius, 0, 90);
        drawCircleSector(x + radius, y + height - radius, radius, 270, 360);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x + radius, y + height, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y + height, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y, 0.0D).endVertex();
        worldrenderer.pos(x, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y + radius, 0.0D).endVertex();
        worldrenderer.pos(x, y + radius, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + width, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + width, y + radius, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y + radius, 0.0D).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= 90; i += 10) worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 90; i <= 180; i += 10) worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 180; i <= 270; i += 10) worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 270; i <= 360; i += 10) worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircleSector(float cx, float cy, float r, int startAngle, int endAngle) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = startAngle; i <= endAngle; i += 10) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }
}