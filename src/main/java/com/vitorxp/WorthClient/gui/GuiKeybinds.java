package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.keybinds.Keybinds;
import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiKeybinds extends GuiScreen {

    private final GuiScreen parent;
    private final List<KeyBinding> binds = new ArrayList<>();
    private KeyBinding selectedBind = null;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    public GuiKeybinds(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        binds.clear();
        binds.add(Keybinds.openConfig);
        binds.add(Keybinds.openConfigHud);
        binds.add(Keybinds.perspectiveM);
        binds.add(Keybinds.ZoomM);
        binds.add(Keybinds.openSocialKey);
        binds.add(Keybinds.screenshotKey);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xCC000000);

        drawCenteredString(fontRendererObj, "ATALHOS DO CLIENT", width / 2, 20, 0xFFFFAA00);

        int listW = 300;
        int listH = height - 80;
        int startX = (width - listW) / 2;
        int startY = 50;
        int itemH = 25;
        int totalH = binds.size() * (itemH + 5);
        maxScroll = Math.max(0, totalH - listH);

        int scale = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        org.lwjgl.opengl.GL11.glScissor(startX * scale, 30 * scale, listW * scale, listH * scale);

        int currentY = startY + scrollOffset;

        for (KeyBinding kb : binds) {
            boolean hover = mouseX >= startX && mouseX <= startX + listW && mouseY >= currentY && mouseY <= currentY + itemH;

            GuiModMenu.drawRoundedRect(startX, currentY, listW, itemH, 4, hover ? 0xFF333333 : 0xFF222222);
            GuiModMenu.drawRoundedOutline(startX, currentY, listW, itemH, 4, 1.0f, hover ? 0xFF666666 : 0xFF444444);

            fontRendererObj.drawString(kb.getKeyDescription(), startX + 10, currentY + 8, 0xFFFFFFFF);

            String keyName = (selectedBind == kb) ? "> ... <" : Keyboard.getKeyName(kb.getKeyCode());
            int keyW = fontRendererObj.getStringWidth(keyName) + 20;
            int keyX = startX + listW - keyW - 5;
            int keyColor = (selectedBind == kb) ? 0xFFFFFF55 : 0xFFFFFFFF;
            int keyBg = (selectedBind == kb) ? 0xFF555522 : 0xFF444444;

            GuiModMenu.drawRoundedRect(keyX, currentY + 3, keyW, itemH - 6, 3, keyBg);
            drawCenteredString(fontRendererObj, keyName, keyX + keyW / 2, currentY + 8, keyColor);

            currentY += itemH + 5;
        }

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);

        drawButton(width / 2 - 50, height - 30, 100, 20, "VOLTAR", mouseX, mouseY);
    }

    private void drawButton(int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        GuiModMenu.drawRoundedRect(x, y, w, h, 4, hover ? 0xFF555555 : 0xFF333333);
        GuiModMenu.drawRoundedOutline(x, y, w, h, 4, 1.0f, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, text, x + w / 2, y + 6, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (mouseX >= width / 2 - 50 && mouseX <= width / 2 + 50 && mouseY >= height - 30 && mouseY <= height - 10) {
                mc.displayGuiScreen(parent);
                return;
            }

            int listW = 300;
            int startX = (width - listW) / 2;
            int currentY = 50 + scrollOffset;
            int itemH = 25;

            for (KeyBinding kb : binds) {
                String keyName = Keyboard.getKeyName(kb.getKeyCode());
                int keyW = fontRendererObj.getStringWidth(keyName) + 20;
                int keyX = startX + listW - keyW - 5;

                if (mouseY >= currentY + 3 && mouseY <= currentY + itemH - 3 && mouseX >= keyX && mouseX <= keyX + keyW) {
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    selectedBind = kb;
                    return;
                }
                currentY += itemH + 5;
            }

            selectedBind = null;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (selectedBind != null) {
            if (keyCode == 1) {
                selectedBind.setKeyCode(0);
            } else {
                selectedBind.setKeyCode(keyCode);
            }
            selectedBind = null;
            KeyBinding.resetKeyBindingArrayAndHash();
            ConfigManager.save();
            return;
        }

        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) scrollOffset += 20;
            else scrollOffset -= 20;
            if (scrollOffset > 0) scrollOffset = 0;
            if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
        }
    }
}