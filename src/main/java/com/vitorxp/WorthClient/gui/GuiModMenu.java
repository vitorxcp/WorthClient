package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.config.KeystrokesSettings;
import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.hud.ScoreboardHUD;
import com.vitorxp.WorthClient.manager.ConfigManager;
import com.vitorxp.WorthClient.manager.PresetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.vitorxp.WorthClient.utils.RankUtils.isStaff;

public class GuiModMenu extends GuiScreen {

    private final Color themeColor = new Color(158, 96, 32);
    private final int colBackgroundTop = new Color(20, 20, 20, 250).getRGB();
    private final int colBackgroundBottom = new Color(30, 25, 25, 250).getRGB();
    private final int btnEnabledTop = 0xFF2ECC71;
    private final int btnEnabledBottom = 0xFF27AE60;
    private final int btnDisabledTop = 0xFFE74C3C;
    private final int btnDisabledBottom = 0xFFC0392B;
    private final int btnSettingsNormal = 0xFF444444;
    private final int btnSettingsHover = 0xFF666666;
    private GuiTextField presetNameField;

    private enum ScreenState {GRID, CONFIG, PRESETS}

    private ScreenState currentState = ScreenState.GRID;
    private final List<ModCard> allModules = new ArrayList<>();
    private final List<ModCard> visibleModules = new ArrayList<>();
    private ModCard selectedMod = null;
    private Category currentCategory = Category.ALL;
    private GuiTextField searchField;
    private String searchText = "";
    private int guiWidth = 680;
    private int guiHeight = 420;
    private int guiLeft, guiTop;
    private int settingWidth = 540;
    private int settingHeight = 32;
    private float scrollOffset = 0;
    private float maxScroll = 0;
    private float currentScale = 0.0f;
    private boolean closing = false;
    private float fitScale = 1.0f;
    private boolean hasUnsavedChanges = false;

    public GuiModMenu() {
    }

    @Override
    public void initGui() {
        this.currentState = ScreenState.GRID;
        this.selectedMod = null;
        this.closing = false;
        this.currentScale = 0.0f;
        this.scrollOffset = 0;
        this.hasUnsavedChanges = false;
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;
        this.settingWidth = guiWidth - 120;
        this.searchField = new GuiTextField(0, fontRendererObj, guiLeft + guiWidth - 170, guiTop + 18, 150, 16);
        this.searchField.setMaxStringLength(30);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setText(searchText);
        this.presetNameField = new GuiTextField(1, fontRendererObj, guiLeft + 65, guiTop + 65, 300, 20);
        this.presetNameField.setMaxStringLength(25);
        this.presetNameField.setEnableBackgroundDrawing(false);
        setupModules();
        filterModules();
    }

    private void markUnsaved() {
        this.hasUnsavedChanges = true;
    }

    private void saveChanges() {
        ConfigManager.save();
        this.hasUnsavedChanges = false;
        NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Alterações confirmadas!");
    }

    private void discardChanges() {
        ConfigManager.load();
        this.hasUnsavedChanges = false;
        NotificationRenderer.send(NotificationRenderer.Type.WARNING, "Alterações descartadas.");
    }

    private void updateFitScale() {
        float scaleX = (float) this.width / (guiWidth + 20);
        float scaleY = (float) this.height / (guiHeight + 20);
        this.fitScale = Math.min(1.0f, Math.min(scaleX, scaleY));
    }

    private int getAdjustedMouseX(int mouseX) {
        float centerX = this.width / 2.0f;
        return (int) ((mouseX - centerX) / fitScale + centerX);
    }

    private int getAdjustedMouseY(int mouseY) {
        float centerY = this.height / 2.0f;
        return (int) ((mouseY - centerY) / fitScale + centerY);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (currentState == ScreenState.CONFIG || currentState == ScreenState.PRESETS || currentState == ScreenState.GRID) {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                scrollOffset += dWheel > 0 ? 40 : -40;
                clampScroll();
            }
        }
    }

    private void clampScroll() {
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (currentState == ScreenState.GRID) {
            if (searchField.textboxKeyTyped(typedChar, keyCode)) {
                searchText = searchField.getText();
                filterModules();
                return;
            }
        } else if (currentState == ScreenState.PRESETS) {
            presetNameField.textboxKeyTyped(typedChar, keyCode);
        } else if (currentState == ScreenState.CONFIG && selectedMod != null) {
            for (Setting s : selectedMod.settings) {
                if (s instanceof KeybindSetting) {
                    KeybindSetting ks = (KeybindSetting) s;
                    if (ks.isBinding) {
                        ks.onKeyTyped(keyCode);
                        markUnsaved();
                        return;
                    }
                }
            }
        }

        if (keyCode == 1) {
            if (currentState != ScreenState.GRID) {
                currentState = ScreenState.GRID;
                selectedMod = null;
                scrollOffset = 0;
            } else {
                if (hasUnsavedChanges) {
                    discardChanges();
                }
                closing = true;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int btn) throws IOException {
        int adjX = getAdjustedMouseX(mouseX);
        int adjY = getAdjustedMouseY(mouseY);

        if (hasUnsavedChanges) {
            int boxWidth = 320;
            int boxX = guiLeft + (guiWidth - boxWidth) / 2;
            int boxY = guiTop + guiHeight - 55;
            int btnW = 100;
            int btnH = 18;
            int btnY = boxY + 20;
            int confirmX = boxX + (boxWidth / 2) - btnW - 10;
            if (adjX >= confirmX && adjX <= confirmX + btnW && adjY >= btnY && adjY <= btnY + btnH) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                saveChanges();
                return;
            }
            int cancelX = boxX + (boxWidth / 2) + 10;
            if (adjX >= cancelX && adjX <= cancelX + btnW && adjY >= btnY && adjY <= btnY + btnH) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                discardChanges();
                return;
            }
            if (adjX >= boxX && adjX <= boxX + boxWidth && adjY >= boxY && adjY <= boxY + 45) return;
        }

        if (currentState == ScreenState.GRID) {
            searchField.mouseClicked(adjX, adjY, btn);

            int btnPresetY = hasUnsavedChanges ? guiTop + guiHeight - 60 : guiTop + guiHeight - 30;
            if (adjX >= guiLeft + guiWidth - 100 && adjX <= guiLeft + guiWidth - 20 && adjY >= btnPresetY && adjY <= btnPresetY + 20) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                currentState = ScreenState.PRESETS;
                scrollOffset = 0;
                presetNameField.setText("");
                presetNameField.setFocused(true);
                return;
            }

            int catX = guiLeft + 140;
            if (searchField.getText().isEmpty()) {
                for (Category cat : Category.values()) {
                    int catW = fontRendererObj.getStringWidth(cat.name()) + 10;
                    if (adjX >= catX && adjX <= catX + catW && adjY >= guiTop + 15 && adjY <= guiTop + 35) {
                        currentCategory = cat;
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        filterModules();
                        return;
                    }
                    catX += 60;
                }
            }

            if (adjY < guiTop + 50 || adjY > guiTop + guiHeight - 10) return;

            int startX = guiLeft + 30;
            int startY = (int) (guiTop + 60 + scrollOffset);
            int gap = 15;
            int cardW = 145;
            int cardH = 100;
            int cols = 4;

            for (int i = 0; i < visibleModules.size(); i++) {
                ModCard mod = visibleModules.get(i);
                int col = i % cols;
                int row = i / cols;
                int x = startX + col * (cardW + gap);
                int y = startY + row * (cardH + gap);

                if (adjX >= x && adjX <= x + cardW && adjY >= y && adjY <= y + cardH) {
                    boolean hasSettings = !mod.settings.isEmpty() || mod.isMenuOnly();
                    int btnH = 18;
                    int padding = 5;
                    int statusY = y + cardH - btnH - padding;
                    int optY = statusY - btnH - 3;

                    if (hasSettings && adjX >= x + padding && adjX <= x + cardW - padding && adjY >= optY && adjY <= optY + btnH) {
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        selectedMod = mod;
                        currentState = ScreenState.CONFIG;
                        scrollOffset = 0;
                        return;
                    }

                    if (!mod.isMenuOnly() && adjX >= x + padding && adjX <= x + cardW - padding && adjY >= statusY && adjY <= statusY + btnH) {
                        if (!mod.isBlocked()) {
                            mod.toggle();
                            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                            markUnsaved();
                        }
                        return;
                    }

                    if (!mod.isMenuOnly() && !mod.isBlocked()) {
                        mod.toggle();
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        markUnsaved();
                    }
                }
            }
        } else if (currentState == ScreenState.CONFIG) {
            if (adjX >= guiLeft + 20 && adjX <= guiLeft + 70 && adjY >= guiTop + 15 && adjY <= guiTop + 35) {
                currentState = ScreenState.GRID;
                return;
            }

            int listY = (int) (guiTop + 60 + scrollOffset);
            for (Setting s : selectedMod.settings) {
                if (adjY > guiTop + 50 && adjY < guiTop + guiHeight - 20) {
                    if (s.mouseClicked(guiLeft + 60, listY, adjX, adjY, btn)) {
                        return;
                    }
                }
                listY += s.getHeight() + 5;
            }
        } else if (currentState == ScreenState.PRESETS) {
            if (adjX >= guiLeft + 20 && adjX <= guiLeft + 70 && adjY >= guiTop + 15 && adjY <= guiTop + 35) {
                currentState = ScreenState.GRID;
                return;
            }

            int startY = (int) (guiTop + 60 + scrollOffset);
            int inputX = guiLeft + 60;
            int inputY = startY;
            int btnSaveX = inputX + 315;
            int btnSaveW = 80;

            if (adjY > guiTop + 50 && adjY < guiTop + guiHeight - 20) {
                if (adjX >= btnSaveX && adjX <= btnSaveX + btnSaveW && adjY >= inputY && adjY <= inputY + 24) {
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    String name = presetNameField.getText().trim();
                    if (name.isEmpty()) {
                        NotificationRenderer.send(NotificationRenderer.Type.WARNING, "Digite um nome!");
                    } else {
                        PresetManager.savePreset(name);
                        presetNameField.setText("");
                        hasUnsavedChanges = false;
                    }
                    return;
                }
            }

            int updateBtnY = inputY + 40;
            if (PresetManager.currentActivePreset != null && hasUnsavedChanges) {
                if (adjY > guiTop + 50 && adjY < guiTop + guiHeight - 20) {
                    if (adjX >= inputX && adjX <= inputX + settingWidth && adjY >= updateBtnY && adjY <= updateBtnY + 24) {
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        PresetManager.savePreset(PresetManager.currentActivePreset);
                        hasUnsavedChanges = false;
                        return;
                    }
                }
                updateBtnY += 35;
            }

            int listY = updateBtnY;
            List<String> presets = PresetManager.getPresetList();

            for (String p : presets) {
                if (adjY > guiTop + 50 && adjY < guiTop + guiHeight - 20) {
                    if (adjX >= inputX + settingWidth - 80 && adjX <= inputX + settingWidth && adjY >= listY && adjY <= listY + 25) {
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        PresetManager.loadPreset(p);
                        hasUnsavedChanges = false;
                        return;
                    }
                    if (adjX >= inputX + settingWidth - 110 && adjX <= inputX + settingWidth - 85 && adjY >= listY && adjY <= listY + 25) {
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        PresetManager.deletePreset(p);
                        return;
                    }
                }
                listY += 30;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (currentState == ScreenState.CONFIG && selectedMod != null) {
            for (Setting s : selectedMod.settings) {
                if (s instanceof SliderSetting) {
                    SliderSetting slider = (SliderSetting) s;
                    if (slider.dragging) {
                        slider.dragging = false;
                        markUnsaved();
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateFitScale();
        int adjX = getAdjustedMouseX(mouseX);
        int adjY = getAdjustedMouseY(mouseY);

        drawDefaultBackground();

        if (closing) {
            currentScale = lerp(currentScale, 0f, 0.4f);
            if (currentScale < 0.1f) {
                if (hasUnsavedChanges) discardChanges();
                mc.displayGuiScreen(null);
                return;
            }
        } else {
            currentScale = lerp(currentScale, 1f, 0.3f);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(currentScale * fitScale, currentScale * fitScale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);

        drawGradientRoundedRect(guiLeft, guiTop, guiWidth, guiHeight, 12, colBackgroundTop, colBackgroundBottom);
        drawRoundedOutline(guiLeft, guiTop, guiWidth, guiHeight, 12, 1.5f, themeColor.getRGB());

        drawRect(guiLeft + 10, guiTop + 45, guiLeft + guiWidth - 10, guiTop + 46, 0x20FFFFFF);

        if (currentState == ScreenState.GRID) {
            drawGridScreen(adjX, adjY);
        } else if (currentState == ScreenState.CONFIG) {
            drawConfigScreen(adjX, adjY);
        } else if (currentState == ScreenState.PRESETS) {
            drawPresetsScreen(adjX, adjY);
        }

        if (hasUnsavedChanges) {
            int boxWidth = 320;
            int boxHeight = 45;
            int boxX = guiLeft + (guiWidth - boxWidth) / 2;
            int boxY = guiTop + guiHeight - 55;

            drawGradientRoundedRect(boxX, boxY, boxWidth, boxHeight, 8, 0xF0151515, 0xF00A0A0A);
            drawRoundedOutline(boxX, boxY, boxWidth, boxHeight, 8, 1.5f, 0xFFFFAA00);
            String warningText = "Você tem alterações pendentes";
            drawCenteredString(fontRendererObj, warningText, boxX + boxWidth / 2, boxY + 7, 0xFFDDDDDD);

            int btnW = 100;
            int btnH = 18;
            int btnY = boxY + 20;
            int confirmX = boxX + (boxWidth / 2) - btnW - 10;
            boolean hoverConfirm = adjX >= confirmX && adjX <= confirmX + btnW && adjY >= btnY && adjY <= btnY + btnH;
            if (!hoverConfirm) drawRoundedRect(confirmX + 1, btnY + 1, btnW, btnH, 4, 0x50000000);
            drawGradientRoundedRect(confirmX, btnY, btnW, btnH, 4, hoverConfirm ? 0xFF2ECC71 : 0xFF27AE60, hoverConfirm ? 0xFF27AE60 : 0xFF219150);
            drawCenteredString(fontRendererObj, "SALVAR", confirmX + btnW / 2, btnY + 5, -1);
            int cancelX = boxX + (boxWidth / 2) + 10;
            boolean hoverCancel = adjX >= cancelX && adjX <= cancelX + btnW && adjY >= btnY && adjY <= btnY + btnH;
            if (!hoverCancel) drawRoundedRect(cancelX + 1, btnY + 1, btnW, btnH, 4, 0x50000000);
            drawGradientRoundedRect(cancelX, btnY, btnW, btnH, 4, hoverCancel ? 0xFFE74C3C : 0xFFC0392B, hoverCancel ? 0xFFC0392B : 0xFFA93226);
            drawCenteredString(fontRendererObj, "DESCARTAR", cancelX + btnW / 2, btnY + 5, -1);
        }

        NotificationRenderer.render(mc);
        GlStateManager.popMatrix();
    }

    private void drawGridScreen(int mouseX, int mouseY) {
        drawCenteredString(fontRendererObj, "WorthClient", guiLeft + 60, guiTop + 18, themeColor.getRGB());

        drawRect(guiLeft + guiWidth - 175, guiTop + 35, guiLeft + guiWidth - 15, guiTop + 36, 0xFF555555);
        searchField.drawTextBox();
        if (searchField.getText().isEmpty() && !searchField.isFocused()) {
            fontRendererObj.drawString("Pesquisar...", guiLeft + guiWidth - 170, guiTop + 18, 0xFF777777);
        }

        if (searchField.getText().isEmpty()) {
            int catX = guiLeft + 140;
            for (Category cat : Category.values()) {
                boolean sel = cat == currentCategory;
                int color = sel ? 0xFFFFFFFF : 0xFFAAAAAA;
                int catW = fontRendererObj.getStringWidth(cat.name());
                if (mouseX >= catX && mouseX <= catX + catW && mouseY >= guiTop + 15 && mouseY <= guiTop + 35) {
                    color = 0xFFDDDDDD;
                }

                fontRendererObj.drawString(cat.name(), catX, guiTop + 18, color);

                if (sel) {
                    drawRect(catX, guiTop + 30, catX + catW, guiTop + 32, themeColor.getRGB());
                }
                catX += 60;
            }
        } else {
            fontRendererObj.drawString("Resultados da busca", guiLeft + 140, guiTop + 18, 0xFFFFAA00);
        }

        int btnPx = guiLeft + guiWidth - 100;
        int btnPy = hasUnsavedChanges ? guiTop + guiHeight - 60 : guiTop + guiHeight - 30;
        boolean hoverPreset = mouseX >= btnPx && mouseX <= btnPx + 80 && mouseY >= btnPy && mouseY <= btnPy + 20;
        drawRoundedRect(btnPx, btnPy, 80, 20, 5, hoverPreset ? 0xFF444444 : 0xFF333333);
        drawCenteredString(fontRendererObj, "PRESETS", btnPx + 40, btnPy + 6, -1);

        int startX = guiLeft + 30;
        int startY = (int) (guiTop + 60 + scrollOffset);
        int gap = 15;
        int cardW = 145;
        int cardH = 100;
        int cols = 4;

        glScissor(guiLeft + 20, guiTop + 50, guiWidth - 40, hasUnsavedChanges ? guiHeight - 90 : guiHeight - 85);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        for (int i = 0; i < visibleModules.size(); i++) {
            ModCard mod = visibleModules.get(i);
            int col = i % cols;
            int row = i / cols;
            mod.drawCard(mc, startX + col * (cardW + gap), startY + row * (cardH + gap), cardW, cardH, mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int totalRows = (int) Math.ceil((double) visibleModules.size() / cols);
        int totalHeight = totalRows * (cardH + gap);
        maxScroll = Math.max(0, totalHeight - (guiHeight - 100));
        drawScrollbar(totalHeight, guiHeight - 100);
    }

    private void drawConfigScreen(int mouseX, int mouseY) {
        boolean hoverBack = mouseX >= guiLeft + 20 && mouseX <= guiLeft + 70 && mouseY >= guiTop + 15 && mouseY <= guiTop + 35;
        fontRendererObj.drawString("< VOLTAR", guiLeft + 20, guiTop + 20, hoverBack ? 0xFFFFFFFF : 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, selectedMod.name + " Configurações", guiLeft + guiWidth / 2, guiTop + 20, 0xFFFFAA00);

        int listX = guiLeft + 60;
        int listY = (int) (guiTop + 60 + scrollOffset);

        glScissor(guiLeft + 20, guiTop + 50, guiWidth - 40, guiHeight - 70);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        if (selectedMod.settings.isEmpty()) {
            drawCenteredString(fontRendererObj, "Não há configurações disponíveis para este módulo.", guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFFAAAAAA);
        } else {
            for (Setting s : selectedMod.settings) {
                s.draw(mc, listX, listY, mouseX, mouseY);
                listY += s.getHeight() + 5;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int contentH = selectedMod.settings.stream().mapToInt(s -> s.getHeight() + 5).sum();
        maxScroll = Math.max(0, contentH - (guiHeight - 100));
        drawScrollbar(contentH, (guiHeight - 100));
    }

    private void drawPresetsScreen(int mouseX, int mouseY) {
        boolean hoverBack = mouseX >= guiLeft + 20 && mouseX <= guiLeft + 70 && mouseY >= guiTop + 15 && mouseY <= guiTop + 35;
        fontRendererObj.drawString("< VOLTAR", guiLeft + 20, guiTop + 20, hoverBack ? 0xFFFFFFFF : 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "Gerenciador de Presets", guiLeft + guiWidth / 2, guiTop + 20, 0xFFFFAA00);

        int startY = (int) (guiTop + 60 + scrollOffset);
        int inputX = guiLeft + 60;
        int inputY = startY;

        glScissor(guiLeft + 20, guiTop + 50, guiWidth - 40, hasUnsavedChanges ? guiHeight - 90 : guiHeight - 70);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawRoundedRect(inputX - 5, inputY - 5, 310, 30, 4, 0xFF333333);
        presetNameField.xPosition = inputX;
        presetNameField.yPosition = inputY + 6;
        presetNameField.drawTextBox();
        if (presetNameField.getText().isEmpty() && !presetNameField.isFocused())
            fontRendererObj.drawString("Nome do novo preset...", inputX + 4, inputY + 6, 0xFF777777);

        int btnSaveX = inputX + 315;
        int btnSaveW = 80;
        boolean hoverSave = mouseX >= btnSaveX && mouseX <= btnSaveX + btnSaveW && mouseY >= inputY && mouseY <= inputY + 24;
        drawRoundedRect(btnSaveX, inputY, btnSaveW, 24, 6, hoverSave ? 0xFF27AE60 : 0xFF2ECC71);
        drawCenteredString(fontRendererObj, "CRIAR", btnSaveX + 40, inputY + 8, -1);
        int listY = inputY + 40;

        if (PresetManager.currentActivePreset != null && hasUnsavedChanges) {
            boolean hoverUpd = mouseX >= inputX && mouseX <= inputX + settingWidth && mouseY >= listY && mouseY <= listY + 24;
            drawRoundedRect(inputX, listY, settingWidth, 24, 6, hoverUpd ? 0xFFD35400 : 0xFFE67E22);
            drawCenteredString(fontRendererObj, "ATUALIZAR PRESET: " + PresetManager.currentActivePreset, inputX + settingWidth / 2, listY + 8, -1);
            listY += 35;
        }

        List<String> presets = PresetManager.getPresetList();
        if (presets.isEmpty())
            drawCenteredString(fontRendererObj, "Nenhum preset salvo.", guiLeft + guiWidth / 2, listY + 30, 0xFF888888);

        for (String p : presets) {
            boolean active = p.equals(PresetManager.currentActivePreset);
            drawRoundedRect(inputX, listY, settingWidth, 25, 6, active ? 0xFF444444 : 0xFF333333);
            fontRendererObj.drawString(p + (active ? " (Ativo)" : ""), inputX + 10, listY + 8, active ? 0xFFFFAA00 : 0xFFDDDDDD);
            boolean hoverLoad = mouseX >= inputX + settingWidth - 80 && mouseX <= inputX + settingWidth && mouseY >= listY && mouseY <= listY + 25;
            drawRoundedRect(inputX + settingWidth - 80, listY, 80, 25, 6, hoverLoad ? 0xFF3498DB : 0xFF2980B9);
            drawCenteredString(fontRendererObj, "CARREGAR", inputX + settingWidth - 40, listY + 8, -1);
            boolean hoverDel = mouseX >= inputX + settingWidth - 110 && mouseX <= inputX + settingWidth - 85 && mouseY >= listY && mouseY <= listY + 25;
            drawRoundedRect(inputX + settingWidth - 110, listY, 25, 25, 6, hoverDel ? 0xFFC0392B : 0xFFE74C3C);
            drawCenteredString(fontRendererObj, "X", inputX + settingWidth - 97, listY + 8, -1);

            listY += 30;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int contentH = 40 + (PresetManager.currentActivePreset != null && hasUnsavedChanges ? 35 : 0) + (presets.size() * 30);
        maxScroll = Math.max(0, contentH - (guiHeight - 100));
        drawScrollbar(contentH, (guiHeight - 100));
    }

    private void drawScrollbar(int contentH, int viewH) {
        if (maxScroll > 0) {
            float ratio = (float) viewH / contentH;
            int barH = Math.max(30, (int) (viewH * ratio));
            int barY = guiTop + 60 + (int) ((-scrollOffset / maxScroll) * (viewH - barH));
            drawRoundedRect(guiLeft + guiWidth - 10, barY, 4, barH, 2, 0x80FFFFFF);
        }
    }

    private void filterModules() {
        visibleModules.clear();
        String search = searchField.getText().toLowerCase();
        for (ModCard mod : allModules) {
            if (!search.isEmpty()) {
                if (mod.name.toLowerCase().contains(search) || mod.description.toLowerCase().contains(search)) {
                    visibleModules.add(mod);
                }
            } else {
                if (currentCategory == Category.ALL || mod.category == currentCategory) {
                    visibleModules.add(mod);
                }
            }
        }
        scrollOffset = 0;
    }

    private void setupModules() {
        allModules.clear();

        allModules.add(new ModCard("FPS", "Exibe o framerate", "fps", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return WorthClient.fpsOverlay;
            }

            @Override
            public void toggle() {
                WorthClient.fpsOverlay = !WorthClient.fpsOverlay;
            }
        });
        allModules.add(new ModCard("Ping", "Latência do servidor", "ping", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return WorthClient.pingOverlay;
            }

            @Override
            public void toggle() {
                WorthClient.pingOverlay = !WorthClient.pingOverlay;
            }
        });
        allModules.add(new ModCard("Keystrokes", "Teclas na tela", "keys", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return KeystrokesSettings.enabled;
            }

            @Override
            public void toggle() {
                KeystrokesSettings.enabled = !KeystrokesSettings.enabled;
            }

            @Override
            public void initSettings() {
                settings.add(new BooleanSetting("Modo Chroma", () -> KeystrokesSettings.chromaMode, () -> KeystrokesSettings.chromaMode = !KeystrokesSettings.chromaMode));
                settings.add(new SliderSetting("Escala", 0.5f, 2.0f, () -> KeystrokesSettings.scale, v -> KeystrokesSettings.scale = v));
                settings.add(new SliderSetting("Tamanho da Caixa", 15f, 35f, () -> KeystrokesSettings.boxSize, v -> KeystrokesSettings.boxSize = v));
                CategorySetting toggles = new CategorySetting("Opções Gerais");
                toggles.add(new BooleanSetting("Mostrar Cliques", () -> KeystrokesSettings.showClicks, () -> KeystrokesSettings.showClicks = !KeystrokesSettings.showClicks));
                toggles.add(new BooleanSetting("Substituir Nomes por Setas", () -> KeystrokesSettings.useArrows, () -> KeystrokesSettings.useArrows = !KeystrokesSettings.useArrows));
                toggles.add(new BooleanSetting("Mostrar Teclas Movimento", () -> KeystrokesSettings.showMovement, () -> KeystrokesSettings.showMovement = !KeystrokesSettings.showMovement));
                toggles.add(new BooleanSetting("Mostrar Barra de Espaço", () -> KeystrokesSettings.showSpace, () -> KeystrokesSettings.showSpace = !KeystrokesSettings.showSpace));
                toggles.add(new BooleanSetting("Sombra do Texto", () -> KeystrokesSettings.textShadow, () -> KeystrokesSettings.textShadow = !KeystrokesSettings.textShadow));
                toggles.add(new BooleanSetting("Borda", () -> KeystrokesSettings.borderEnabled, () -> KeystrokesSettings.borderEnabled = !KeystrokesSettings.borderEnabled));
                settings.add(toggles);
                settings.add(new SliderSetting("Espessura da Borda", 0.5f, 3.0f, () -> KeystrokesSettings.borderThickness, v -> KeystrokesSettings.borderThickness = v));
                CategorySetting colors = new CategorySetting("Cores");
                colors.add(new ColorSetting("Cor da Borda", () -> KeystrokesSettings.borderColor, c -> KeystrokesSettings.borderColor = c));
                colors.add(new ColorSetting("Cor do Texto", () -> KeystrokesSettings.textColor, c -> KeystrokesSettings.textColor = c));
                colors.add(new ColorSetting("Cor Texto (Pressionado)", () -> KeystrokesSettings.textPressedColor, c -> KeystrokesSettings.textPressedColor = c));
                colors.add(new ColorSetting("Cor do Fundo", () -> KeystrokesSettings.backgroundDefault, c -> KeystrokesSettings.backgroundDefault = c));
                colors.add(new ColorSetting("Cor Fundo (Pressionado)", () -> KeystrokesSettings.backgroundPressed, c -> KeystrokesSettings.backgroundPressed = c));
                settings.add(colors);
                settings.add(new ActionSetting("Salvar Keystrokes", ConfigManager::save));
            }
        });
        allModules.add(new ModCard("Scoreboard", "Customiza a tabela", "sb", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return ScoreboardHUD.toggled;
            }

            @Override
            public void toggle() {
                ScoreboardHUD.toggled = !ScoreboardHUD.toggled;
            }

            @Override
            public void initSettings() {
                settings.add(new BooleanSetting("Mostrar Números", () -> ScoreboardHUD.showNumbers, () -> ScoreboardHUD.showNumbers = !ScoreboardHUD.showNumbers));
                settings.add(new BooleanSetting("Fundo", () -> ScoreboardHUD.background, () -> ScoreboardHUD.background = !ScoreboardHUD.background));
                settings.add(new BooleanSetting("Borda", () -> ScoreboardHUD.border, () -> ScoreboardHUD.border = !ScoreboardHUD.border));
                settings.add(new ModeSetting("Tamanho", "Normal", Arrays.asList("Pequeno", "Normal", "Grande", "Gigante")) {
                    @Override
                    boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
                        if (super.mouseClicked(x, y, mouseX, mouseY, mouseButton)) {
                            switch (this.currentValue) {
                                case "Pequeno":
                                    ScoreboardHUD.scale = 0.75f;
                                    break;
                                case "Normal":
                                    ScoreboardHUD.scale = 1.0f;
                                    break;
                                case "Grande":
                                    ScoreboardHUD.scale = 1.25f;
                                    break;
                                case "Gigante":
                                    ScoreboardHUD.scale = 1.5f;
                                    break;
                            }
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(new ColorSetting("Cor do Fundo", () -> new java.awt.Color(ScoreboardHUD.backgroundColor, true), (c) -> ScoreboardHUD.backgroundColor = c.getRGB()));
                settings.add(new ActionSetting("Resetar", () -> {
                    ScoreboardHUD.backgroundColor = 0x50000000;
                    ScoreboardHUD.borderColor = 0xFF000000;
                    ScoreboardHUD.background = true;
                    ScoreboardHUD.showNumbers = true;
                    ScoreboardHUD.border = false;
                    ScoreboardHUD.scale = 1.0f;
                }));
            }
        });
        allModules.add(new ModCard("Waila", "Info do Bloco", "waila", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return WorthClient.WailaMod;
            }

            @Override
            public void toggle() {
                WorthClient.WailaMod = !WorthClient.WailaMod;
            }
        });
        allModules.add(new ModCard("ArmorStatus", "Estado da Armadura", "armor", Category.HUD) {
            @Override
            public boolean isEnabled() {
                return WorthClient.ArmorsOverlays;
            }

            @Override
            public void toggle() {
                WorthClient.ArmorsOverlays = !WorthClient.ArmorsOverlays;
            }

            @Override
            public void initSettings() {
                List<String> styles = Arrays.asList("Padrão", "Valor", "Porcentagem");
                CategorySetting handCat = new CategorySetting("Item na Mão");
                handCat.add(new BooleanSetting("Habilitado", () -> WorthClient.mainHandHUDOverlay, () -> WorthClient.mainHandHUDOverlay = !WorthClient.mainHandHUDOverlay));
                handCat.add(new ModeSetting("Estilo", WorthClient.mainHandStyle, styles) {
                    @Override
                    boolean mouseClicked(int x, int y, int mx, int my, int mb) {
                        if (super.mouseClicked(x, y, mx, my, mb)) {
                            WorthClient.mainHandStyle = this.currentValue;
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(handCat);

                CategorySetting helmCat = new CategorySetting("Capacete");
                helmCat.add(new BooleanSetting("Habilitado", () -> WorthClient.helmetHUDOverlay, () -> WorthClient.helmetHUDOverlay = !WorthClient.helmetHUDOverlay));
                helmCat.add(new ModeSetting("Estilo", WorthClient.helmetStyle, styles) {
                    @Override
                    boolean mouseClicked(int x, int y, int mx, int my, int mb) {
                        if (super.mouseClicked(x, y, mx, my, mb)) {
                            WorthClient.helmetStyle = this.currentValue;
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(helmCat);

                CategorySetting chestCat = new CategorySetting("Peitoral");
                chestCat.add(new BooleanSetting("Habilitado", () -> WorthClient.chestplateHUDOverlay, () -> WorthClient.chestplateHUDOverlay = !WorthClient.chestplateHUDOverlay));
                chestCat.add(new ModeSetting("Estilo", WorthClient.chestplateStyle, styles) {
                    @Override
                    boolean mouseClicked(int x, int y, int mx, int my, int mb) {
                        if (super.mouseClicked(x, y, mx, my, mb)) {
                            WorthClient.chestplateStyle = this.currentValue;
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(chestCat);

                CategorySetting legCat = new CategorySetting("Calças");
                legCat.add(new BooleanSetting("Habilitado", () -> WorthClient.leggingsHUDOverlay, () -> WorthClient.leggingsHUDOverlay = !WorthClient.leggingsHUDOverlay));
                legCat.add(new ModeSetting("Estilo", WorthClient.leggingsStyle, styles) {
                    @Override
                    boolean mouseClicked(int x, int y, int mx, int my, int mb) {
                        if (super.mouseClicked(x, y, mx, my, mb)) {
                            WorthClient.leggingsStyle = this.currentValue;
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(legCat);

                CategorySetting bootCat = new CategorySetting("Botas");
                bootCat.add(new BooleanSetting("Habilitado", () -> WorthClient.bootsHUDOverlay, () -> WorthClient.bootsHUDOverlay = !WorthClient.bootsHUDOverlay));
                bootCat.add(new ModeSetting("Estilo", WorthClient.bootsStyle, styles) {
                    @Override
                    boolean mouseClicked(int x, int y, int mx, int my, int mb) {
                        if (super.mouseClicked(x, y, mx, my, mb)) {
                            WorthClient.bootsStyle = this.currentValue;
                            return true;
                        }
                        return false;
                    }
                });
                settings.add(bootCat);
            }
        });

        allModules.add(new ModCard("AutoLogin", "Login Automático", "key", Category.PLAYER) {
            @Override
            public boolean isEnabled() {
                return WorthClient.AutoLoginEnabled;
            }

            @Override
            public void toggle() {
                WorthClient.AutoLoginEnabled = !WorthClient.AutoLoginEnabled;
            }

            @Override
            public void initSettings() {
                settings.add(new ActionSetting("Configurar Servidores", () -> Minecraft.getMinecraft().displayGuiScreen(new GuiAutoLoginServers(Minecraft.getMinecraft().currentScreen))));
            }
        });
        allModules.add(new ModCard("AutoText", "Macros de Texto", "chat", Category.PLAYER) {
            @Override
            public boolean isMenuOnly() {
                return true;
            }

            @Override
            public void initSettings() {
                settings.add(new ActionSetting("Abrir Editor", () -> Minecraft.getMinecraft().displayGuiScreen(new GuiAutoText(Minecraft.getMinecraft().currentScreen))));
            }
        });
        allModules.add(new ModCard("Skin 3D", "Relevo na Skin", "skin", Category.PLAYER) {
            @Override
            public boolean isEnabled() {
                return WorthClient.skin3D;
            }

            @Override
            public void toggle() {
                WorthClient.skin3D = !WorthClient.skin3D;
            }

            @Override
            public void initSettings() {
                settings.add(new SliderSetting("Espessura", 0.1f, 5.0f, () -> WorthClient.pixelsThickness, (val) -> WorthClient.pixelsThickness = val));
            }
        });

        allModules.add(new ModCard("TimeChanger", "Controlar Horário", "time", Category.WORLD) {
            @Override
            public boolean isEnabled() {
                return WorthClient.timeChangerEnable;
            }

            @Override
            public void toggle() {
                WorthClient.timeChangerEnable = !WorthClient.timeChangerEnable;
            }

            @Override
            public void initSettings() {
                settings.add(new SliderSetting("Horário", 0.0f, 24000.0f, () -> WorthClient.clientTime, (val) -> WorthClient.clientTime = val));
            }
        });
        allModules.add(new ModCard("Perspective", "Visão 360", "360", Category.WORLD) {
            @Override
            public boolean isMenuOnly() {
                return true;
            }

            @Override
            public void initSettings() {
                settings.add(new KeybindSetting("Tecla", () -> WorthClient.KeyPerspective, (val) -> {
                    WorthClient.KeyPerspective = val;
                    com.vitorxp.WorthClient.keybinds.Keybinds.updatePerspectiveKey(val);
                }));
                settings.add(new KeybindSetting("Tecla do Perspective", () -> WorthClient.KeyPerspective, (val) -> {
                    WorthClient.KeyPerspective = val;
                    com.vitorxp.WorthClient.keybinds.Keybinds.updatePerspectiveKey(val);
                }));
                settings.add(new BooleanSetting("Modo Toggle (Ativar/Desativar)", () -> com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle, () -> {
                    WorthClient.PerspectiveModToggle = !WorthClient.PerspectiveModToggle;
                }));
                settings.add(new BooleanSetting("Iniciar de Frente", () -> WorthClient.PerspectiveStartFront, () -> {
                    WorthClient.PerspectiveStartFront = !WorthClient.PerspectiveStartFront;
                }));
            }
        });
        allModules.add(new ModCard("Zoom", "Zoom Infinito", "Z", Category.WORLD) {
            @Override
            public boolean isMenuOnly() {
                return true;
            }

            @Override
            public void initSettings() {
                settings.add(new KeybindSetting("Tecla", () -> WorthClient.KeyZoom, (val) -> {
                    WorthClient.KeyZoom = val;
                    com.vitorxp.WorthClient.keybinds.Keybinds.updateZoomKey(val);
                }));
                settings.add(new BooleanSetting("Modo Toggle", () -> WorthClient.enableToggleZoom, () -> WorthClient.enableToggleZoom = !WorthClient.enableToggleZoom));
            }
        });

        allModules.add(new ModCard("Chat", "Opções de Chat", "chat", Category.MISC) {
            @Override
            public boolean isMenuOnly() {
                return true;
            }

            public void initSettings() {
                settings.add(new BooleanSetting("Desativar mensagem de pet maxímo", () -> WorthClient.petOverlay, () -> WorthClient.petOverlay = !WorthClient.petOverlay));
                settings.add(new BooleanSetting("Desativar mensagem de inventário cheio", () -> WorthClient.blockInventoryMessages, () -> WorthClient.blockInventoryMessages = !WorthClient.blockInventoryMessages));
                settings.add(new BooleanSetting("Desativar aviso ao quebrar bloco (fora da ilha)", () -> WorthClient.MsgBlockDestroyBlock, () -> WorthClient.MsgBlockDestroyBlock = !WorthClient.MsgBlockDestroyBlock));
                settings.add(new BooleanSetting("Botão para copiar mensagem", () -> WorthClient.enableCopy, () -> WorthClient.enableCopy = !WorthClient.enableCopy));
                settings.add(new BooleanSetting("Mostrar data de envio", () -> WorthClient.showTime, () -> WorthClient.showTime = !WorthClient.showTime));
            }
        });
        allModules.add(new ModCard("Animations", "Animações 1.7", "anim", Category.MISC) {
            @Override
            public boolean isEnabled() {
                return com.vitorxp.WorthClient.config.AnimationsConfig.enabled;
            }

            @Override
            public void toggle() {
                com.vitorxp.WorthClient.config.AnimationsConfig.enabled = !com.vitorxp.WorthClient.config.AnimationsConfig.enabled;
            }

            @Override
            public void initSettings() {
                settings.add(new BooleanSetting("Partículas dos Portais Realçadas", () -> WorthClient.animationPortal, () -> WorthClient.animationPortal = !WorthClient.animationPortal));
                settings.add(new BooleanSetting("BlockHit 1.7 (Osu)", () -> com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17, () -> com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17 = !com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17));

                settings.add(new BooleanSetting("Vara de Pescar 1.7", () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldRod, () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldRod = !com.vitorxp.WorthClient.config.AnimationsConfig.oldRod));

                settings.add(new BooleanSetting("Arco 1.7", () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldBow, () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldBow = !com.vitorxp.WorthClient.config.AnimationsConfig.oldBow));

                settings.add(new BooleanSetting("Agachar 1.7 (Suave)", () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak, () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak = !com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak));

                settings.add(new BooleanSetting("Dano na Câmera", () -> com.vitorxp.WorthClient.config.AnimationsConfig.damageShake, () -> com.vitorxp.WorthClient.config.AnimationsConfig.damageShake = !com.vitorxp.WorthClient.config.AnimationsConfig.damageShake));

                settings.add(new BooleanSetting("Sempre Bater (Always Swing)", () -> com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing, () -> com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing = !com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing));

                settings.add(new SliderSetting("Posição X", -1.0f, 1.0f, () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX, (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX = val));

                settings.add(new SliderSetting("Posição Y", -1.0f, 1.0f, () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY, (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY = val));

                settings.add(new SliderSetting("Posição Z", -1.0f, 1.0f, () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ, (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ = val));

                settings.add(new SliderSetting("Tamanho do Item", 0.5f, 2.0f, () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemScale, (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemScale = val));

                settings.add(new ActionSetting("Resetar Posição", () -> {
                    com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX = 0.0f;
                    com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY = 0.0f;
                    com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ = 0.0f;
                    com.vitorxp.WorthClient.config.AnimationsConfig.itemScale = 1.0f;
                    NotificationRenderer.send(com.vitorxp.WorthClient.gui.utils.NotificationRenderer.Type.SUCCESS, "Posição Resetada!");
                }));
            }
        });

        if (isStaff(Minecraft.getMinecraft().thePlayer)) {
            allModules.add(new ModCard("Admin", "Painel Staff", "admin", Category.MISC) {
                @Override
                public boolean isMenuOnly() {
                    return true;
                }

                public void initSettings() {
                    settings.add(new BooleanSetting("Bloquear Build Ilhas", () -> WorthClient.blockIsBuild, () -> WorthClient.blockIsBuild = !WorthClient.blockIsBuild));
                }
            });
        }
    }

    public enum Category {ALL, HUD, PLAYER, WORLD, MISC}

    abstract class ModCard {
        String name, description, iconName;
        Category category;
        List<Setting> settings = new ArrayList<>();
        float hoverScale = 1.0f;

        public ModCard(String n, String d, String i, Category c) {
            name = n;
            description = d;
            iconName = i;
            category = c;
            initSettings();
        }

        public void initSettings() {
        }

        public boolean isEnabled() {
            return false;
        }

        public boolean isBlocked() {
            return false;
        }

        public boolean isMenuOnly() {
            return false;
        }

        public void toggle() {
        }

        public void drawCard(Minecraft mc, int x, int y, int w, int h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            float targetScale = hovered ? 1.02f : 1.0f;
            hoverScale = lerp(hoverScale, targetScale, 0.2f);

            float centerX = x + w / 2.0f;
            float centerY = y + h / 2.0f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(centerX, centerY, 0);
            GlStateManager.scale(hoverScale, hoverScale, 1f);
            GlStateManager.translate(-centerX, -centerY, 0);

            if (isEnabled() || hovered) {
                drawRoundedRect(x - 1, y - 1, w + 2, h + 2, 9, hovered ? 0x60FFAA00 : 0x40FFAA00);
            }

            drawGradientRoundedRect(x, y, w, h, 8, 0xE61A1A1A, 0xE6222222);
            drawRoundedOutline(x, y, w, h, 8, 1.0f, hovered ? 0xFFFFAA00 : 0x30FFFFFF);

            drawCircleSector(x + 20, y + 20, 12, 0, 360);
            GlStateManager.color(0.2f, 0.2f, 0.2f); // Dark circle bg
            drawCircleSector(x + 20, y + 20, 10, 0, 360);
            drawCenteredString(mc.fontRendererObj, name.substring(0, 1).toUpperCase(), x + 20, y + 16, themeColor.getRGB());

            fontRendererObj.drawString(name, x + 40, y + 15, -1);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.7, 0.7, 1);
            fontRendererObj.drawString(description, (int) ((x + 10) / 0.7), (int) ((y + 40) / 0.7), 0xFFAAAAAA);
            GlStateManager.popMatrix();

            int btnH = 18;
            int padding = 5;
            int statusY = y + h - btnH - padding;
            int optY = statusY - btnH - 3;

            boolean hasSettings = !settings.isEmpty() || isMenuOnly();

            if (hasSettings) {
                boolean hovOpt = hovered && (mouseY >= optY && mouseY <= optY + btnH);
                drawRoundedRect(x + padding, optY, w - (padding * 2), btnH, 4, hovOpt ? btnSettingsHover : btnSettingsNormal);
                drawCenteredString(mc.fontRendererObj, "OPÇÕES", x + w / 2, optY + 5, 0xFFCCCCCC);
            }

            if (!isMenuOnly()) {
                boolean active = isEnabled();
                boolean blocked = isBlocked();
                int colTop = blocked ? btnDisabledTop : (active ? btnEnabledTop : btnDisabledTop);
                int colBot = blocked ? btnDisabledBottom : (active ? btnEnabledBottom : btnDisabledBottom);
                String txt = blocked ? "BLOQUEADO" : (active ? "ATIVADO" : "DESATIVADO");

                drawGradientRoundedRect(x + padding, statusY, w - (padding * 2), btnH, 4, colTop, colBot);
                drawCenteredString(mc.fontRendererObj, txt, x + w / 2, statusY + 5, 0xFFFFFFFF);
            } else if (hasSettings && !settings.isEmpty()) {
                drawCenteredString(mc.fontRendererObj, "§f", x + w / 2, statusY + 5, 0xFF555555);
            }

            GlStateManager.popMatrix();
        }
    }

    abstract class Setting {
        String name;

        public Setting(String n) {
            name = n;
        }

        abstract void draw(Minecraft mc, int x, int y, int mx, int my);

        abstract boolean mouseClicked(int x, int y, int mx, int my, int mb);

        public int getHeight() {
            return 32;
        }
    }

    class ActionSetting extends Setting {
        Runnable action;

        public ActionSetting(String name, Runnable action) {
            super(name);
            this.action = action;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            boolean hov = mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32;
            drawRoundedRect(x, y, settingWidth, 32, 6, hov ? 0xFF555555 : 0xFF333333);
            drawCenteredString(mc.fontRendererObj, name, x + settingWidth / 2, y + 12, -1);
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                if (action != null) action.run();
                return true;
            }
            return false;
        }
    }

    class BooleanSetting extends Setting {
        boolean value;
        Supplier<Boolean> getter;
        Runnable toggler;
        boolean isDynamic;
        float anim = 0f;

        public BooleanSetting(String name, Supplier<Boolean> g, Runnable t) {
            super(name);
            getter = g;
            toggler = t;
            isDynamic = true;
            anim = g.get() ? 1f : 0f;
        }

        public boolean isOn() {
            return isDynamic ? getter.get() : value;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            anim = lerp(anim, isOn() ? 1f : 0f, 0.2f);
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 12, -1);
            int swX = x + settingWidth - 45;
            int swY = y + 6;
            int col = interpolateColor(0xFF555555, 0xFF2ECC71, anim);
            drawRoundedRect(swX, swY, 30, 16, 8, col);
            drawCircleSector(swX + 8 + (14 * anim), swY + 8, 6, 0, 360);
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                toggler.run();
                markUnsaved();
                return true;
            }
            return false;
        }
    }

    class SliderSetting extends Setting {
        Supplier<Float> getter;
        java.util.function.Consumer<Float> setter;
        float min, max;
        boolean dragging;

        public SliderSetting(String n, float min, float max, Supplier<Float> g, java.util.function.Consumer<Float> s) {
            super(n);
            this.min = min;
            this.max = max;
            getter = g;
            setter = s;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 5, 0xFFAAAAAA);
            if (dragging) {
                float val = (float) (mx - (x + 15)) / (float) (settingWidth - 30);
                val = Math.max(0, Math.min(1, val));
                setter.accept(min + (val * (max - min)));
            }
            float val = getter.get();
            float fill = (val - min) / (max - min);
            drawRoundedRect(x + 15, y + 20, settingWidth - 30, 4, 2, 0xFF404040);
            if (fill > 0) drawRoundedRect(x + 15, y + 20, (settingWidth - 30) * fill, 4, 2, themeColor.getRGB());
            String s = String.format("%.1f", val);
            mc.fontRendererObj.drawString(s, x + settingWidth - 15 - mc.fontRendererObj.getStringWidth(s), y + 5, -1);
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                dragging = true;
                return true;
            }
            return false;
        }
    }

    class CategorySetting extends Setting {
        boolean expanded = false;
        List<Setting> children = new ArrayList<>();

        public CategorySetting(String name) {
            super(name);
        }

        public void add(Setting s) {
            children.add(s);
        }

        public int getHeight() {
            return expanded ? 32 + children.stream().mapToInt(s -> s.getHeight() + 2).sum() : 32;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF333333);
            mc.fontRendererObj.drawString(name, x + 15, y + 12, -1);
            mc.fontRendererObj.drawString(expanded ? "v" : ">", x + settingWidth - 20, y + 12, 0xFFAAAAAA);
            if (expanded) {
                int cy = y + 34;
                for (Setting s : children) {
                    s.draw(mc, x + 10, cy, mx, my);
                    cy += s.getHeight() + 2;
                }
            }
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                expanded = !expanded;
                return true;
            }
            if (expanded) {
                int cy = y + 34;
                for (Setting s : children) {
                    if (s.mouseClicked(x + 10, cy, mx, my, mb)) return true;
                    cy += s.getHeight() + 2;
                }
            }
            return false;
        }
    }

    class ColorSetting extends Setting {
        Supplier<Color> getter;
        java.util.function.Consumer<Color> setter;

        public ColorSetting(String n, Supplier<Color> g, java.util.function.Consumer<Color> s) {
            super(n);
            getter = g;
            setter = s;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 12, 0xFFAAAAAA);
            int pX = x + settingWidth - 30;
            int pY = y + 6;
            drawRoundedRect(pX, pY, 20, 20, 4, getter.get().getRGB() | 0xFF000000);
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                mc.displayGuiScreen(new GuiColorPicker(GuiModMenu.this, name, getter.get(), setter::accept));
                markUnsaved();
                return true;
            }
            return false;
        }
    }

    class ModeSetting extends Setting {
        String currentValue; // NOME CORRIGIDO AQUI
        List<String> modes;
        int idx;

        public ModeSetting(String n, String c, List<String> m) {
            super(n);
            currentValue = c;
            modes = m;
            idx = m.indexOf(c);
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 12, 0xFFAAAAAA);
            drawCenteredString(mc.fontRendererObj, modes.get(idx), x + settingWidth - 50, y + 12, themeColor.getRGB());
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                idx = (idx + 1) % modes.size();
                currentValue = modes.get(idx);
                markUnsaved();
                return true;
            }
            return false;
        }
    }

    class KeybindSetting extends Setting {
        Supplier<Integer> getter;
        java.util.function.Consumer<Integer> setter;
        boolean isBinding;

        public KeybindSetting(String n, Supplier<Integer> g, java.util.function.Consumer<Integer> s) {
            super(n);
            getter = g;
            setter = s;
        }

        void draw(Minecraft mc, int x, int y, int mx, int my) {
            drawRoundedRect(x, y, settingWidth, 32, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 12, 0xFFAAAAAA);
            String k = isBinding ? "..." : org.lwjgl.input.Keyboard.getKeyName(getter.get());
            drawCenteredString(mc.fontRendererObj, "[" + k + "]", x + settingWidth - 40, y + 12, isBinding ? 0xFF55FF55 : -1);
        }

        boolean mouseClicked(int x, int y, int mx, int my, int mb) {
            if (mx >= x && mx <= x + settingWidth && my >= y && my <= y + 32) {
                isBinding = !isBinding;
                return true;
            }
            isBinding = false;
            return false;
        }

        public void onKeyTyped(int k) {
            if (isBinding) {
                if (k != 1) setter.accept(k);
                isBinding = false;
            }
        }
    }

    // --- GL UTILS ---
    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    private int interpolateColor(int c1, int c2, float f) {
        int r1 = (c1 >> 16) & 255;
        int g1 = (c1 >> 8) & 255;
        int b1 = c1 & 255;
        int a1 = (c1 >> 24) & 255;
        int r2 = (c2 >> 16) & 255;
        int g2 = (c2 >> 8) & 255;
        int b2 = c2 & 255;
        int a2 = (c2 >> 24) & 255;
        return ((int) (a1 + (a2 - a1) * f) << 24) | ((int) (r1 + (r2 - r1) * f) << 16) | ((int) (g1 + (g2 - g1) * f) << 8) | (int) (b1 + (b2 - b1) * f);
    }

    private void glScissor(int x, int y, int w, int h) {
        int s = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(x * s, mc.displayHeight - (y + h) * s, w * s, h * s);
    }

    public static void drawGradientRoundedRect(float x, float y, float w, float h, float r, int c1, int c2) {
        drawRoundedRect(x, y, w, h, r, c1);
    }

    public static void drawRoundedOutline(float x, float y, float w, float h, float r, float t, int c) {
        float x2 = x + w, y2 = y + h;
        float a = (c >> 24 & 255) / 255f, red = (c >> 16 & 255) / 255f, g = (c >> 8 & 255) / 255f, b = (c & 255) / 255f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, g, b, a);
        GL11.glLineWidth(t);
        Tessellator tes = Tessellator.getInstance();
        WorldRenderer wr = tes.getWorldRenderer();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 270; i >= 180; i -= 10)
            wr.pos(x + r + Math.sin(Math.toRadians(i)) * r, y + r + Math.cos(Math.toRadians(i)) * r, 0).endVertex();
        for (int i = 180; i >= 90; i -= 10)
            wr.pos(x2 - r + Math.sin(Math.toRadians(i)) * r, y + r + Math.cos(Math.toRadians(i)) * r, 0).endVertex();
        for (int i = 90; i >= 0; i -= 10)
            wr.pos(x2 - r + Math.sin(Math.toRadians(i)) * r, y2 - r + Math.cos(Math.toRadians(i)) * r, 0).endVertex();
        for (int i = 0; i >= -90; i -= 10)
            wr.pos(x + r + Math.sin(Math.toRadians(i)) * r, y2 - r + Math.cos(Math.toRadians(i)) * r, 0).endVertex();
        tes.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedRect(float x, float y, float w, float h, float r, int c) {
        float x2 = x + w, y2 = y + h;
        float f = (c >> 24 & 255) / 255f, f1 = (c >> 16 & 255) / 255f, f2 = (c >> 8 & 255) / 255f, f3 = (c & 255) / 255f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        Tessellator tes = Tessellator.getInstance();
        WorldRenderer wr = tes.getWorldRenderer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        double angle;
        for (int i = 0; i <= 90; i += 10) {
            angle = Math.toRadians(i);
            wr.pos(x2 - r + Math.sin(angle) * r, y2 - r + Math.cos(angle) * r, 0).endVertex();
        }
        for (int i = 90; i <= 180; i += 10) {
            angle = Math.toRadians(i);
            wr.pos(x2 - r + Math.sin(angle) * r, y + r + Math.cos(angle) * r, 0).endVertex();
        }
        for (int i = 180; i <= 270; i += 10) {
            angle = Math.toRadians(i);
            wr.pos(x + r + Math.sin(angle) * r, y + r + Math.cos(angle) * r, 0).endVertex();
        }
        for (int i = 270; i <= 360; i += 10) {
            angle = Math.toRadians(i);
            wr.pos(x + r + Math.sin(angle) * r, y2 - r + Math.cos(angle) * r, 0).endVertex();
        }
        tes.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircleSector(float cx, float cy, float r, int sa, int ea) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        w.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        w.pos(cx, cy, 0).endVertex();
        for (int i = sa; i <= ea; i += 10)
            w.pos(cx + Math.sin(Math.toRadians(i)) * r, cy + Math.cos(Math.toRadians(i)) * r, 0).endVertex();
        t.draw();
    }
}