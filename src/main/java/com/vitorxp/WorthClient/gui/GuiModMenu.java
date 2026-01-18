package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.hud.ScoreboardHUD;
import com.vitorxp.WorthClient.manager.ActivationManager;
import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import com.vitorxp.WorthClient.config.KeystrokesColors;
import javax.swing.JColorChooser;
import java.awt.Color;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import static com.vitorxp.WorthClient.utils.RankUtils.isStaff;

public class GuiModMenu extends GuiScreen {

    private final Color themeColor = new Color(158, 96, 32);
    private final int colBackgroundTop = new Color(20, 20, 20, 240).getRGB();
    private final int colBackgroundBottom = new Color(35, 15, 5, 240).getRGB();
    private final int btnEnabledTop = 0xFF2ECC71;
    private final int btnEnabledBottom = 0xFF27AE60;
    private final int btnDisabledTop = 0xFFE74C3C;
    private final int btnDisabledBottom = 0xFFC0392B;
    private enum ScreenState { GRID, CONFIG }
    private ScreenState currentState = ScreenState.GRID;
    private final List<ModCard> allModules = new ArrayList<>();
    private final List<ModCard> visibleModules = new ArrayList<>();
    private ModCard selectedMod = null;
    private Category currentCategory = Category.HUD;
    private int guiWidth = 660;
    private int guiHeight = 410;
    private int guiLeft;
    private int guiTop;
    private int settingWidth = 540;
    private int settingHeight = 32;
    private float scrollOffset = 0;
    private float maxScroll = 0;
    private float currentScale = 0.0f;
    private boolean closing = false;
    private float fitScale = 1.0f;
    public static boolean toggleArmor = true;
    public static boolean toggleTimeChanger = false;
    public GuiModMenu() {}

    @Override
    public void initGui() {
        this.currentState = ScreenState.GRID;
        this.selectedMod = null;
        this.closing = false;
        this.currentScale = 0.0f;
        this.scrollOffset = 0;
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;
        this.settingWidth = guiWidth - 120;

        setupModules();
        filterModules();
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
        if (currentState == ScreenState.CONFIG) {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                if (dWheel > 0) {
                    scrollOffset += 35;
                } else {
                    scrollOffset -= 35;
                }
                clampScroll();
            }
        }
    }

    private void clampScroll() {
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
    }

    private void setupModules() {
        allModules.clear();

        allModules.add(new ModCard("FPS", "Exibe o framerate", "fps", Category.HUD) {
            @Override public boolean isEnabled() { return WorthClient.fpsOverlay; }
            @Override public void toggle() { WorthClient.fpsOverlay = !WorthClient.fpsOverlay; }
        });

        allModules.add(new ModCard("Ping", "Latência do servidor", "ping", Category.HUD) {
            @Override public boolean isEnabled() { return WorthClient.pingOverlay; }
            @Override public void toggle() { WorthClient.pingOverlay = !WorthClient.pingOverlay; }
        });

        allModules.add(new ModCard("Scoreboard", "Customiza a tabela lateral", "scoreboard", Category.HUD) {
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
                settings.add(new BooleanSetting("Mostrar Números",
                        () -> ScoreboardHUD.showNumbers,
                        () -> ScoreboardHUD.showNumbers = !ScoreboardHUD.showNumbers
                ));

                settings.add(new BooleanSetting("Fundo",
                        () -> ScoreboardHUD.background,
                        () -> ScoreboardHUD.background = !ScoreboardHUD.background
                ));

                settings.add(new BooleanSetting("Borda",
                        () -> ScoreboardHUD.border,
                        () -> ScoreboardHUD.border = !ScoreboardHUD.border
                ));

                settings.add(new ModeSetting("Tamanho", "Normal", Arrays.asList("Pequeno", "Normal", "Grande", "Gigante")) {
                    @Override
                    boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
                        if (super.mouseClicked(x, y, mouseX, mouseY, mouseButton)) {
                            switch (this.currentValue) {
                                case "Pequeno": ScoreboardHUD.scale = 0.75f; break;
                                case "Normal":  ScoreboardHUD.scale = 1.0f; break;
                                case "Grande":  ScoreboardHUD.scale = 1.25f; break;
                                case "Gigante": ScoreboardHUD.scale = 1.5f; break;
                            }
                            return true;
                        }
                        return false;
                    }
                });

                allModules.add(new ModCard("Animations", "Animações 1.7 e Física", "anim", Category.MISC) {
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
                        settings.add(new BooleanSetting("BlockHit 1.7 (Osu)",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17 = !com.vitorxp.WorthClient.config.AnimationsConfig.blockHit17
                        ));

                        settings.add(new BooleanSetting("Vara de Pescar 1.7",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldRod,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldRod = !com.vitorxp.WorthClient.config.AnimationsConfig.oldRod
                        ));

                        settings.add(new BooleanSetting("Arco 1.7",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldBow,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldBow = !com.vitorxp.WorthClient.config.AnimationsConfig.oldBow
                        ));

                        settings.add(new BooleanSetting("Agachar 1.7 (Suave)",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak = !com.vitorxp.WorthClient.config.AnimationsConfig.oldSneak
                        ));

                        settings.add(new BooleanSetting("Dano na Câmera",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.damageShake,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.damageShake = !com.vitorxp.WorthClient.config.AnimationsConfig.damageShake
                        ));

                        settings.add(new BooleanSetting("Sempre Bater (Always Swing)",
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing = !com.vitorxp.WorthClient.config.AnimationsConfig.alwaysSwing
                        ));

                        settings.add(new SliderSetting("Posição X", -1.0f, 1.0f,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX,
                                (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX = val
                        ));

                        settings.add(new SliderSetting("Posição Y", -1.0f, 1.0f,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY,
                                (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY = val
                        ));

                        settings.add(new SliderSetting("Posição Z", -1.0f, 1.0f,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ,
                                (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ = val
                        ));

                        settings.add(new SliderSetting("Tamanho do Item", 0.5f, 2.0f,
                                () -> com.vitorxp.WorthClient.config.AnimationsConfig.itemScale,
                                (val) -> com.vitorxp.WorthClient.config.AnimationsConfig.itemScale = val
                        ));

                        settings.add(new ActionSetting("Resetar Posição", () -> {
                            com.vitorxp.WorthClient.config.AnimationsConfig.itemPosX = 0.0f;
                            com.vitorxp.WorthClient.config.AnimationsConfig.itemPosY = 0.0f;
                            com.vitorxp.WorthClient.config.AnimationsConfig.itemPosZ = 0.0f;
                            com.vitorxp.WorthClient.config.AnimationsConfig.itemScale = 1.0f;
                            NotificationRenderer.send(com.vitorxp.WorthClient.gui.utils.NotificationRenderer.Type.SUCCESS, "Posição Resetada!");
                        }));
                    }
                });

                settings.add(new ColorSetting("Cor do Fundo",
                        () -> new java.awt.Color(ScoreboardHUD.backgroundColor, true),
                        (c) -> ScoreboardHUD.backgroundColor = c.getRGB()
                ));

                settings.add(new ColorSetting("Cor da Borda",
                        () -> new java.awt.Color(ScoreboardHUD.borderColor, true),
                        (c) -> ScoreboardHUD.borderColor = c.getRGB()
                ));

                settings.add(new ActionSetting("Resetar (Padrão Vanilla)", () -> {
                    ScoreboardHUD.backgroundColor = 0x50000000;
                    ScoreboardHUD.borderColor = 0xFF000000;
                    ScoreboardHUD.background = true;
                    ScoreboardHUD.showNumbers = true;
                    ScoreboardHUD.border = false;
                    ScoreboardHUD.scale = 1.0f;
                    NotificationRenderer.send(com.vitorxp.WorthClient.gui.utils.NotificationRenderer.Type.SUCCESS, "Scoreboard Resetada!");
                }));
            }
        });

        allModules.add(new ModCard("Keystrokes", "Mostra teclas", "keys", Category.HUD) {
            @Override public boolean isEnabled() { return WorthClient.keystrokesOverlay; }
            @Override public void toggle() { WorthClient.keystrokesOverlay = !WorthClient.keystrokesOverlay; }
            @Override public void initSettings() {
                settings.add(new ColorSetting("Cor: Fundo Padrão",
                        () -> KeystrokesColors.backgroundDefault, KeystrokesColors::setBackgroundDefault
                ));

                settings.add(new ColorSetting("Cor: Fundo Press",
                        () -> KeystrokesColors.backgroundPressed, KeystrokesColors::setBackgroundPressed
                ));

                settings.add(new ColorSetting("Cor: Borda",
                        () -> KeystrokesColors.border, KeystrokesColors::setBorder
                ));

                settings.add(new ColorSetting("Cor: Texto",
                        () -> KeystrokesColors.text, KeystrokesColors::setText
                ));

                settings.add(new ColorSetting("Cor: Texto CPS",
                        () -> KeystrokesColors.cpsText, KeystrokesColors::setCpsText
                ));

                settings.add(new BooleanSetting("Rainbow Fundo", KeystrokesColors.chromaBackground) {
                    @Override boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
                        if (super.mouseClicked(x, y, mouseX, mouseY, mouseButton)) {
                            KeystrokesColors.chromaBackground = this.value;
                            return true;
                        }
                        return false;
                    }
                });

                settings.add(new BooleanSetting("Rainbow Borda", KeystrokesColors.chromaBorder) {
                    @Override boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
                        if (super.mouseClicked(x, y, mouseX, mouseY, mouseButton)) {
                            KeystrokesColors.chromaBorder = this.value;
                            return true;
                        }
                        return false;
                    }
                });

                settings.add(new ActionSetting("Resetar Padrão", () -> {
                    KeystrokesColors.resetToDefault();
                    NotificationRenderer.send(NotificationRenderer.Type.WARNING, "Cores resetadas!");
                }));

                settings.add(new ActionSetting("Salvar Cores", () -> {
                    try {
                        KeystrokesColors.saveColors();
                        NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Cores salvas!");
                    } catch (Exception e) {
                        NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Erro ao salvar.");
                        e.printStackTrace();
                    }
                }));
            }
        });

        allModules.add(new ModCard("ArmorStatus", "Estado da Armadura", "armor", Category.HUD) {
            @Override public boolean isEnabled() { return WorthClient.ArmorsOverlays; }
            @Override public void toggle() { WorthClient.ArmorsOverlays = !WorthClient.ArmorsOverlays; }
            @Override public void initSettings() {
                settings.add(new BooleanSetting("Mostrar Item na Mão",
                        () -> WorthClient.mainHandHUDOverlay,
                        () -> WorthClient.mainHandHUDOverlay = !WorthClient.mainHandHUDOverlay
                ));
                settings.add(new BooleanSetting("Mostrar Capacete",
                        () -> WorthClient.helmetHUDOverlay,
                        () -> WorthClient.helmetHUDOverlay = !WorthClient.helmetHUDOverlay
                ));
                settings.add(new BooleanSetting("Mostrar Peitoral",
                        () -> WorthClient.chestplateHUDOverlay,
                        () -> WorthClient.chestplateHUDOverlay = !WorthClient.chestplateHUDOverlay
                ));
                settings.add(new BooleanSetting("Mostrar Calças",
                        () -> WorthClient.leggingsHUDOverlay,
                        () -> WorthClient.leggingsHUDOverlay = !WorthClient.leggingsHUDOverlay
                ));
                settings.add(new BooleanSetting("Mostrar Botas",
                        () -> WorthClient.bootsHUDOverlay,
                        () -> WorthClient.bootsHUDOverlay = !WorthClient.bootsHUDOverlay
                ));
            }
        });

        allModules.add(new ModCard("AutoLogin", "Login Automático (Pirata)", "key", Category.PLAYER) {
            @Override public boolean isEnabled() { return WorthClient.AutoLoginEnabled; }
            @Override public void toggle() { WorthClient.AutoLoginEnabled = !WorthClient.AutoLoginEnabled; }
            @Override public void initSettings() {
                settings.add(new BooleanSetting("Habilitar AutoLogin",
                        () -> WorthClient.AutoLoginEnabled,
                        () -> {
                            WorthClient.AutoLoginEnabled = !WorthClient.AutoLoginEnabled;
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));

                settings.add(new ActionSetting("Abrir Editor de Configurações", () -> {
                    Minecraft.getMinecraft().displayGuiScreen(
                            new GuiAutoLoginServers(Minecraft.getMinecraft().currentScreen)
                    );
                }));
            }
        });

        allModules.add(new ModCard("AutoText", "Macros de Texto", "chat", Category.PLAYER) {
            @Override public boolean isMenuOnly() { return true; }
            @Override public void initSettings() {
                settings.add(new ActionSetting("Abrir Editor de Macros", () -> {
                    Minecraft.getMinecraft().displayGuiScreen(
                            new GuiAutoText(Minecraft.getMinecraft().currentScreen)
                    );
                }));
            }
        });

        allModules.add(new ModCard("Waila", "Mostrar informações do bloco focado", "waila", Category.HUD) {
            @Override public boolean isEnabled() { return WorthClient.WailaMod; }
            @Override public void toggle() { WorthClient.WailaMod = !WorthClient.WailaMod; }
        });

        allModules.add(new ModCard("TimeChanger", "Controla o horário do mundo", "time", Category.WORLD) {
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
                settings.add(new SliderSetting("Horário", 0.0f, 24000.0f,
                        () -> WorthClient.clientTime,
                        (val) -> WorthClient.clientTime = val
                ));
            }
        });

        allModules.add(new ModCard("Perspective", "Visão 360", "360", Category.WORLD) {
            @Override public boolean isMenuOnly() { return true; }
            @Override public void initSettings() {
                settings.add(new KeybindSetting("Tecla do Perspective",
                        () -> WorthClient.KeyPerspective,
                        (val) -> {
                            WorthClient.KeyPerspective = val;
                            com.vitorxp.WorthClient.keybinds.Keybinds.updatePerspectiveKey(val);
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
                settings.add(new BooleanSetting("Modo Toggle (Ativar/Desativar)",
                        () -> com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle,
                        () -> {
                            WorthClient.PerspectiveModToggle = !WorthClient.PerspectiveModToggle;
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
                settings.add(new BooleanSetting("Iniciar de Frente",
                        () -> WorthClient.PerspectiveStartFront,
                        () -> {
                            WorthClient.PerspectiveStartFront = !WorthClient.PerspectiveStartFront;
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
            }
        });

        allModules.add(new ModCard("Zoom", "Amplifique com o Zoom", "Z", Category.WORLD) {
            @Override public boolean isMenuOnly() { return true; }
            @Override public void initSettings() {
                settings.add(new KeybindSetting("Tecla do Zoom",
                        () -> WorthClient.KeyZoom,
                        (val) -> {
                            WorthClient.KeyZoom = val;
                            com.vitorxp.WorthClient.keybinds.Keybinds.updateZoomKey(val);
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
                settings.add(new BooleanSetting("Modo Toggle (Ativar/Desativar)",
                        () -> WorthClient.enableToggleZoom,
                        () -> {
                            WorthClient.enableToggleZoom = !WorthClient.enableToggleZoom;
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
            }
        });

        allModules.add(new ModCard("Mutante", "Alerta Zealot", "mutant", Category.MISC) {
            @Override public boolean isEnabled() { return com.vitorxp.WorthClient.WorthClient.announceZealot; }
            @Override public void toggle() { if(ActivationManager.isActivated) com.vitorxp.WorthClient.WorthClient.announceZealot = !com.vitorxp.WorthClient.WorthClient.announceZealot; }
            @Override public boolean isBlocked() { return !ActivationManager.isActivated; }
        });

        allModules.add(new ModCard("Chat", "Configurar Chat", "chat", Category.MISC) {
            @Override public boolean isMenuOnly() { return true; }
            public void initSettings() {
                settings.add(new BooleanSetting("Desativar mensagem de pet maxímo",
                        () -> WorthClient.petOverlay,
                        () -> WorthClient.petOverlay = !WorthClient.petOverlay
                ));
                settings.add(new BooleanSetting("Desativar mensagem de inventário cheio",
                        () -> WorthClient.blockInventoryMessages,
                        () -> WorthClient.blockInventoryMessages = !WorthClient.blockInventoryMessages
                ));
                settings.add(new BooleanSetting("Desativar aviso ao quebrar bloco (fora da ilha)",
                        () -> WorthClient.MsgBlockDestroyBlock,
                        () -> WorthClient.MsgBlockDestroyBlock = !WorthClient.MsgBlockDestroyBlock
                ));
                settings.add(new BooleanSetting("Botão para copiar mensagem",
                        () -> WorthClient.enableCopy,
                        () -> WorthClient.enableCopy = !WorthClient.enableCopy
                ));
                settings.add(new BooleanSetting("Mostrar data de envio",
                        () -> WorthClient.showTime,
                        () -> WorthClient.showTime = !WorthClient.showTime
                ));
            }
        });

        if (isStaff(Minecraft.getMinecraft().thePlayer)) {
            allModules.add(new ModCard("Admin", "Painel Staff", "admin", Category.MISC) { @Override public boolean isMenuOnly() { return true; } });
        }

        allModules.add(new ModCard("Skin 3D", "Adiciona relevo à skin", "skin", Category.PLAYER) {
            @Override
            public boolean isEnabled() {
                return com.vitorxp.WorthClient.WorthClient.skin3D;
            }

            @Override
            public void toggle() {
                com.vitorxp.WorthClient.WorthClient.skin3D = !com.vitorxp.WorthClient.WorthClient.skin3D;
            }

            @Override
            public void initSettings() {
                settings.add(new SliderSetting("Espessura (Pixels)", 0.1f, 5.0f,
                        () -> WorthClient.pixelsThickness,
                        (val) -> {
                            WorthClient.pixelsThickness = val;
                            com.vitorxp.WorthClient.manager.ConfigManager.save();
                        }
                ));
            }
        });
    }

    class SliderSetting extends Setting {
        Supplier<Float> getter;
        java.util.function.Consumer<Float> setter;
        float min;
        float max;
        boolean dragging = false;

        public SliderSetting(String name, float min, float max, Supplier<Float> getter, java.util.function.Consumer<Float> setter) {
            super(name);
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            drawRoundedRect(x, y, settingWidth, settingHeight, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x + 15, y + 5, 0xFFAAAAAA);

            if (dragging) {
                float val = (float)(mouseX - (x + 15)) / (float)(settingWidth - 30);
                val = Math.max(0, Math.min(1, val));
                float newVal = min + (val * (max - min));

                newVal = Math.round(newVal * 10.0f) / 10.0f;
                setter.accept(newVal);
            }

            float currentVal = getter.get();
            float sliderFill = (currentVal - min) / (max - min);

            int barX = x + 15;
            int barY = y + settingHeight - 12;
            int barW = settingWidth - 30;
            int barH = 4;

            drawRoundedRect(barX, barY, barW, barH, 2, 0xFF404040);

            int fillW = (int)(barW * sliderFill);
            if (fillW > 0) {
                drawGradientRoundedRect(barX, barY, fillW, barH, 2, 0xFFFFAA00, 0xFFFF5500);
            }

            float knobX = barX + fillW;
            float knobY = barY + 2;
            boolean hover = mouseX >= barX && mouseX <= barX + barW && mouseY >= barY - 5 && mouseY <= barY + 10;

            drawCircleSector(knobX, knobY, hover || dragging ? 5 : 4, 0, 360);

            String valStr = String.format("%.1f", currentVal);
            mc.fontRendererObj.drawString(valStr, x + settingWidth - 15 - mc.fontRendererObj.getStringWidth(valStr), y + 5, 0xFFFFFFFF);
        }

        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseButton == 0 && mouseX >= x && mouseX <= x + settingWidth && mouseY >= y && mouseY <= y + settingHeight) {
                dragging = true;
                return true;
            }
            return false;
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            dragging = false;
        }
    }

    class ColorSetting extends Setting {
        Supplier<Color> getter;
        java.util.function.Consumer<Color> setter;

        public ColorSetting(String name, Supplier<Color> getter, java.util.function.Consumer<Color> setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            boolean hover = mouseX >= x && mouseX <= x + settingWidth && mouseY >= y && mouseY <= y + settingHeight;
            drawRoundedRect(x, y, settingWidth, settingHeight, 6, 0xFF222222);

            mc.fontRendererObj.drawString(name, x + 15, y + (settingHeight / 2) - 4, 0xFFAAAAAA);

            int previewSize = 20;
            int previewX = x + settingWidth - previewSize - 10;
            int previewY = y + (settingHeight - previewSize) / 2;
            Color c = getter.get();

            drawRoundedRect(previewX, previewY, previewSize, previewSize, 4, c.getRGB() | 0xFF000000);
            drawRoundedOutline(previewX, previewY, previewSize, previewSize, 4, 1.0f, hover ? 0xFFFFFFFF : 0xFF888888);
        }

        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x + settingWidth && mouseY >= y && mouseY <= y + settingHeight) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                mc.displayGuiScreen(new GuiColorPicker(
                        GuiModMenu.this,
                        name,
                        getter.get(),
                        (newColor) -> setter.accept(newColor)
                ));
                return true;
            }
            return false;
        }
    }

    public static void drawGradientRoundedRectVertical(float x, float y, float width, float height, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x + width, y, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(x, y + height, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(x + width, y + height, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void filterModules() {
        visibleModules.clear();
        for (ModCard mod : allModules) {
            if (mod.category == currentCategory) visibleModules.add(mod);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (selectedMod != null && currentState == ScreenState.CONFIG) {
            for (Setting s : selectedMod.settings) {
                if (s instanceof SliderSetting) {
                    ((SliderSetting) s).mouseReleased(mouseX, mouseY, state);
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateFitScale();

        int adjMouseX = getAdjustedMouseX(mouseX);
        int adjMouseY = getAdjustedMouseY(mouseY);

        drawRect(0, 0, width, height, 0x50000000);

        if (closing) {
            currentScale = lerp(currentScale, 0f, 0.5f);
            if (currentScale < 0.1f) {
                ConfigManager.save();
                mc.displayGuiScreen(null);
                return;
            }
        } else {
            currentScale = lerp(currentScale, 1f, 0.4f);
        }

        GlStateManager.pushMatrix();

        float finalScale = currentScale * fitScale;

        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(finalScale, finalScale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);

        drawGradientRoundedRect(guiLeft, guiTop, guiWidth, guiHeight, 15, colBackgroundTop, colBackgroundBottom);
        drawRoundedOutline(guiLeft, guiTop, guiWidth, guiHeight, 15, 2.0f, themeColor.getRGB());

        if (currentState == ScreenState.GRID) {
            drawGridScreen(adjMouseX, adjMouseY);
        } else if (currentState == ScreenState.CONFIG) {
            drawConfigScreen(adjMouseX, adjMouseY);
        }

        NotificationRenderer.render(mc);
        GlStateManager.popMatrix();
    }

    private void drawGridScreen(int mouseX, int mouseY) {
        drawCenteredString(fontRendererObj, "WorthClient", guiLeft + 70, guiTop + 20, themeColor.getRGB());

        int catStartX = guiLeft + 150;
        int catY = guiTop + 18;
        int catGap = 80;

        int i = 0;
        for (Category cat : Category.values()) {
            int x = catStartX + (i * catGap);
            boolean selected = (cat == currentCategory);
            int color = selected ? 0xFFFFFFFF : 0xFFAAAAAA;

            if (mouseX >= x && mouseX <= x + 50 && mouseY >= catY && mouseY <= catY + 15) {
                color = 0xFFDDDDDD;
            }

            fontRendererObj.drawStringWithShadow(cat.name(), x, catY, color);

            if (selected) {
                drawRect(x, catY + 12, x + fontRendererObj.getStringWidth(cat.name()), catY + 14, themeColor.getRGB());
                drawGradientRect(x, catY + 14, x + fontRendererObj.getStringWidth(cat.name()), catY + 20, 0x609E6020, 0x00000000);
            }
            i++;
        }

        drawRect(guiLeft + 20, guiTop + 45, guiLeft + guiWidth - 20, guiTop + 46, 0x30FFFFFF);

        int startX = guiLeft + 30;
        int startY = guiTop + 60;
        int cardWidth = 135;
        int cardHeight = 150;
        int gapX = 18;
        int gapY = 18;
        int columns = 4;

        for (int j = 0; j < visibleModules.size(); j++) {
            ModCard mod = visibleModules.get(j);
            int col = j % columns;
            int row = j / columns;
            int x = startX + (col * (cardWidth + gapX));
            int y = startY + (row * (cardHeight + gapY));

            mod.drawPremiumCard(mc, x, y, cardWidth, cardHeight, mouseX, mouseY);
        }
    }

    private void drawConfigScreen(int mouseX, int mouseY) {
        if (selectedMod == null) {
            currentState = ScreenState.GRID;
            return;
        }

        boolean hoverBack = mouseX >= guiLeft + 20 && mouseX <= guiLeft + 70 && mouseY >= guiTop + 20 && mouseY <= guiTop + 40;
        drawRoundedRect(guiLeft + 20, guiTop + 20, 50, 20, 5, hoverBack ? 0xFF555555 : 0xFF333333);
        drawCenteredString(fontRendererObj, "< Voltar", guiLeft + 45, guiTop + 26, 0xFFFFFFFF);

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 1);
        fontRendererObj.drawStringWithShadow(selectedMod.name, (guiLeft + 100) / 2.0f, (guiTop + 23) / 2.0f, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        drawRect(guiLeft + 20, guiTop + 55, guiLeft + guiWidth - 20, guiTop + 56, 0x40FFFFFF);

        int totalSettingsHeight = selectedMod.settings.size() * (settingHeight + 5);
        int viewHeight = guiHeight - 80;

        this.maxScroll = Math.max(0, totalSettingsHeight - viewHeight + 20);
        clampScroll();

        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();

        int scissorX = (guiLeft + 20) * scaleFactor;
        int scissorY = (mc.displayHeight - (guiTop + guiHeight - 10) * scaleFactor);
        int scissorW = (guiWidth - 40) * scaleFactor;
        int scissorH = (guiHeight - 70) * scaleFactor;

        if (fitScale >= 1.0f) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        }

        int setX = guiLeft + 60;
        int setY = (int) (guiTop + 80 + scrollOffset);

        if (selectedMod.settings.isEmpty()) {
            fontRendererObj.drawString("Nenhuma configuração disponível.", setX, setY, 0xFFAAAAAA);
        } else {
            for (Setting s : selectedMod.settings) {
                int currentHeight = settingHeight + 5;

                if (setY + currentHeight > guiTop + 50 && setY < guiTop + guiHeight) {
                    s.draw(mc, setX, setY, mouseX, mouseY);
                }
                setY += currentHeight;
            }
        }

        if (fitScale >= 1.0f) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        if (maxScroll > 0) {
            int scrollBarH = (int) ((float) viewHeight / totalSettingsHeight * viewHeight);
            if (scrollBarH < 30) scrollBarH = 30;
            int scrollBarY = guiTop + 80 + (int)((-scrollOffset / maxScroll) * (viewHeight - scrollBarH));
            drawRoundedRect(guiLeft + guiWidth - 15, scrollBarY, 5, scrollBarH, 2, 0x80FFFFFF);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int adjMouseX = getAdjustedMouseX(mouseX);
        int adjMouseY = getAdjustedMouseY(mouseY);

        if (currentState == ScreenState.GRID) {
            int startX = guiLeft + 30;
            int startY = guiTop + 60;
            int cardWidth = 135;
            int cardHeight = 150;
            int gapX = 18;
            int gapY = 18;
            int columns = 4;

            int catStartX = guiLeft + 150;
            int catGap = 80;
            int i = 0;
            for (Category cat : Category.values()) {
                int x = catStartX + (i * catGap);
                if (adjMouseX >= x && adjMouseX <= x + 50 && adjMouseY >= guiTop + 15 && adjMouseY <= guiTop + 30) {
                    currentCategory = cat;
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    filterModules();
                    return;
                }
                i++;
            }

            for (int j = 0; j < visibleModules.size(); j++) {
                ModCard mod = visibleModules.get(j);
                int col = j % columns;
                int row = j / columns;
                int x = startX + (col * (cardWidth + gapX));
                int y = startY + (row * (cardHeight + gapY));

                if (adjMouseX >= x && adjMouseX <= x + cardWidth && adjMouseY >= y && adjMouseY <= y + cardHeight) {
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                    if (mod.isBlocked()) return;

                    int btnH = 22;
                    int statusY = y + cardHeight - btnH - 8;
                    int optY = statusY - btnH - 4;

                    boolean clickOpt = (adjMouseX >= x+5 && adjMouseX <= x+cardWidth-5) && (adjMouseY >= optY && adjMouseY <= optY+btnH);
                    boolean clickTog = (adjMouseX >= x+5 && adjMouseX <= x+cardWidth-5) && (adjMouseY >= statusY && adjMouseY <= statusY+btnH);

                    if (mod.isMenuOnly() || (clickOpt && !mod.settings.isEmpty())) {
                        selectedMod = mod;
                        currentState = ScreenState.CONFIG;
                        scrollOffset = 0;
                    } else if (clickTog) {
                        mod.toggle();
                        ConfigManager.save();
                        NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Configurações salvas!");
                    } else {
                        mod.toggle();
                        ConfigManager.save();
                        NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Configurações salvas!");
                    }
                    return;
                }
            }
        } else if (currentState == ScreenState.CONFIG) {
            if (adjMouseX >= guiLeft + 20 && adjMouseX <= guiLeft + 70 && adjMouseY >= guiTop + 20 && adjMouseY <= guiTop + 40) {
                currentState = ScreenState.GRID;
                selectedMod = null;
                return;
            }
            if (selectedMod != null) {
                int setX = guiLeft + 60;
                int setY = (int) (guiTop + 80 + scrollOffset);

                if (adjMouseY > guiTop + 55 && adjMouseY < guiTop + guiHeight - 10) {
                    for (Setting s : selectedMod.settings) {
                        int currentHeight = settingHeight + 5;

                        if (s.mouseClicked(adjMouseX, setY, adjMouseX, adjMouseY, mouseButton)) {
                            if (!(s instanceof ColorSetting)) {
                                ConfigManager.save();
                                KeystrokesColors.saveColors();
                                NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Configurações salvas!");
                            }
                            return;
                        }
                        setY += currentHeight;
                    }
                }
            }
        }
        if (currentState == ScreenState.GRID && (adjMouseX < guiLeft || adjMouseX > guiLeft + guiWidth || adjMouseY < guiTop || adjMouseY > guiTop + guiHeight)) {
            closing = true;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (currentState == ScreenState.CONFIG && selectedMod != null) {
            for (Setting s : selectedMod.settings) {
                if (s instanceof KeybindSetting) {
                    KeybindSetting ks = (KeybindSetting) s;
                    if (ks.isBinding) {
                        ks.onKeyTyped(keyCode);
                        return;
                    }
                }
            }
        }
        if (keyCode == 1) {
            if (currentState == ScreenState.CONFIG) {
                currentState = ScreenState.GRID;
                selectedMod = null;
            } else {
                closing = true;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);
        int a = (int) (a1 + (a2 - a1) * fraction);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public enum Category {
        HUD, PLAYER, WORLD, MISC
    }

    abstract class ModCard {
        String name;
        String description;
        String iconName;
        Category category;
        List<Setting> settings = new ArrayList<>();
        float hoverScale = 1.0f;

        public ModCard(String name, String description, String iconName, Category category) {
            this.name = name;
            this.description = description;
            this.iconName = iconName;
            this.category = category;
            this.initSettings();
        }

        public void initSettings() {}
        public boolean isEnabled() { return false; }
        public boolean isBlocked() { return false; }
        public boolean isMenuOnly() { return false; }
        public void toggle() {}

        public void drawPremiumCard(Minecraft mc, int x, int y, int w, int h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            float targetScale = hovered ? 1.05f : 1.0f;
            hoverScale = lerp(hoverScale, targetScale, 0.2f);
            float centerX = x + w / 2.0f;
            float centerY = y + h / 2.0f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(centerX, centerY, 0);
            GlStateManager.scale(hoverScale, hoverScale, 1f);
            GlStateManager.translate(-centerX, -centerY, 0);

            if (isEnabled() || hovered) {
                drawRoundedRect(x - 2, y - 2, w + 4, h + 4, 10, hovered ? 0x60FFAA00 : 0x40FFAA00);
            }

            drawGradientRoundedRect(x, y, w, h, 8, 0xE6151515, 0xE6252525);

            int borderColor = hovered ? 0xFFFFAA00 : 0x40FFFFFF;
            drawRoundedOutline(x, y, w, h, 8, 1.0f, borderColor);

            int iconY = y + 25;

            drawCircleSector(x + w/2, iconY + 16, 20, 0, 360);
            GlStateManager.color(0.2f, 0.2f, 0.2f);
            drawCircleSector(x + w/2, iconY + 16, 18, 0, 360);

            GlStateManager.pushMatrix();
            GlStateManager.scale(2, 2, 1);
            drawCenteredString(mc.fontRendererObj, name.substring(0, 1), (int)((x + w/2)/2), (int)((iconY + 12)/2), 0xFFFFAA00);
            GlStateManager.popMatrix();

            drawCenteredString(mc.fontRendererObj, name, x + w/2, y + 70, 0xFFFFFFFF);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.65, 0.65, 1);
            drawCenteredString(mc.fontRendererObj, description, (int)((x + w/2)/0.65), (int)((y + 85)/0.65), 0xFFAAAAAA);
            GlStateManager.popMatrix();

            int btnH = 22;
            int padding = 6;
            int statusY = y + h - btnH - padding;
            int optY = statusY - btnH - 4;

            if (!settings.isEmpty() || isMenuOnly()) {
                boolean hovOpt = hovered && (mouseY >= optY && mouseY <= optY+btnH);
                drawRoundedRect(x+padding, optY, w-(padding*2), btnH, 5, hovOpt ? 0xFF555555 : 0xFF333333);
                drawCenteredString(mc.fontRendererObj, "OPÇÕES", x+w/2, optY+7, 0xFFCCCCCC);
            }
            if (!isMenuOnly()) {
                boolean active = isEnabled();
                boolean blocked = isBlocked();
                int colTop = blocked ? btnDisabledTop : (active ? btnEnabledTop : btnDisabledTop);
                int colBot = blocked ? btnDisabledBottom : (active ? btnEnabledBottom : btnDisabledBottom);
                String txt = blocked ? "BLOQUEADO" : (active ? "ATIVADO" : "DESATIVADO");
                drawGradientRoundedRect(x+padding, statusY, w-(padding*2), btnH, 5, colTop, colBot);
                drawCenteredString(mc.fontRendererObj, txt, x+w/2, statusY+7, 0xFFFFFFFF);
            } else {
                drawGradientRoundedRect(x+padding, statusY, w-(padding*2), btnH, 5, btnEnabledTop, btnEnabledBottom);
                drawCenteredString(mc.fontRendererObj, "ABRIR MENU", x+w/2, statusY+7, 0xFFFFFFFF);
            }

            GlStateManager.popMatrix();
        }
    }

    abstract class Setting {
        String name;
        public Setting(String name) { this.name = name; }
        abstract void draw(Minecraft mc, int x, int y, int mouseX, int mouseY);
        abstract boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton);
    }

    class ActionSetting extends Setting {
        Runnable action;
        public ActionSetting(String name, Runnable action) {
            super(name);
            this.action = action;
        }
        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            boolean hov = mouseX >= x && mouseX <= x+settingWidth && mouseY >= y && mouseY <= y+settingHeight;
            drawRoundedRect(x, y, settingWidth, settingHeight, 6, hov ? 0xFF555555 : 0xFF333333);
            drawCenteredString(mc.fontRendererObj, name, x + (settingWidth/2), y + (settingHeight/2) - 4, 0xFFFFFFFF);
        }
        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x+settingWidth && mouseY >= y && mouseY <= y+settingHeight) {
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
        boolean isDynamic = false;
        float animProgress = 0f;

        public BooleanSetting(String name, boolean defaultValue) {
            super(name);
            this.value = defaultValue;
            this.isDynamic = false;
            this.animProgress = defaultValue ? 1f : 0f;
        }

        public BooleanSetting(String name, Supplier<Boolean> getter, Runnable toggler) {
            super(name);
            this.getter = getter;
            this.toggler = toggler;
            this.isDynamic = true;
            this.animProgress = getter.get() ? 1f : 0f;
        }

        public boolean isOn() {
            return isDynamic ? getter.get() : value;
        }

        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            boolean active = isOn();
            float target = active ? 1.0f : 0.0f;
            animProgress = lerp(animProgress, target, 0.2f);

            drawRoundedRect(x, y, settingWidth, settingHeight, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x+15, y + (settingHeight/2) - 4, 0xFFFFFFFF);

            int switchW = 40;
            int switchH = 20;
            int switchX = x + settingWidth - switchW - 15;
            int switchY = y + (settingHeight - switchH) / 2;
            int bgColor = interpolateColor(0xFF555555, 0xFF2ECC71, animProgress);

            drawRoundedRect(switchX, switchY, switchW, switchH, 10, bgColor);
            float knobX = switchX + 2 + ((switchW - 20) * animProgress);
            drawCircleSector(knobX + 8, switchY + 10, 8, 0, 360);
        }

        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x+settingWidth && mouseY >= y && mouseY <= y+settingHeight) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                if (isDynamic) toggler.run();
                else value = !value;
                return true;
            }
            return false;
        }
    }

    class ModeSetting extends Setting {
        String currentValue;
        List<String> modes;
        int index = 0;

        public ModeSetting(String name, String current, List<String> modes) {
            super(name);
            this.currentValue = current;
            this.modes = modes;
            this.index = modes.indexOf(current);
        }

        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            drawRoundedRect(x, y, settingWidth, settingHeight, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x+15, y + (settingHeight/2) - 4, 0xFFAAAAAA);
            drawCenteredString(mc.fontRendererObj, modes.get(index), x + settingWidth - 60, y + (settingHeight/2) - 4, 0xFFFFAA00);
        }

        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x+settingWidth && mouseY >= y && mouseY <= y+settingHeight) {
                index++;
                if (index >= modes.size()) index = 0;
                currentValue = modes.get(index);
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                return true;
            }
            return false;
        }
    }

    class KeybindSetting extends Setting {
        Supplier<Integer> getter;
        java.util.function.Consumer<Integer> setter;
        boolean isBinding = false;

        public KeybindSetting(String name, Supplier<Integer> getter, java.util.function.Consumer<Integer> setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
            drawRoundedRect(x, y, settingWidth, settingHeight, 6, 0xFF222222);
            mc.fontRendererObj.drawString(name, x+15, y + (settingHeight/2) - 4, 0xFFAAAAAA);
            String keyName = isBinding ? "..." : org.lwjgl.input.Keyboard.getKeyName(getter.get());
            int color = isBinding ? 0xFF55FF55 : 0xFFFFFFFF;
            drawCenteredString(mc.fontRendererObj, "[" + keyName + "]", x + settingWidth - 40, y + (settingHeight/2) - 4, color);
        }

        @Override
        boolean mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x+settingWidth && mouseY >= y && mouseY <= y+settingHeight) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                isBinding = !isBinding;
                return true;
            }
            if(isBinding) isBinding=false;
            return false;
        }

        public void onKeyTyped(int keyCode) {
            if (isBinding) {
                if (keyCode == 1) {
                    isBinding = false;
                } else {
                    setter.accept(keyCode);
                    isBinding = false;
                }
            }
        }
    }

    public static void drawGradientRoundedRect(float x, float y, float width, float height, float radius, int colorTop, int colorBottom) {
        drawRoundedRect(x, y, width, height, radius, colorTop);
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float x1 = x;
        float y1 = y;
        float x2 = x + width;
        float y2 = y + height;
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 270; i >= 180; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 180; i >= 90; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 90; i >= 0; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 0; i >= -90; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x;
        float y1 = y;
        float x2 = x + width;
        float y2 = y + height;
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        drawCircleSector(x1 + radius, y1 + radius, radius, 180, 270);
        drawCircleSector(x2 - radius, y1 + radius, radius, 90, 180);
        drawCircleSector(x2 - radius, y2 - radius, radius, 0, 90);
        drawCircleSector(x1 + radius, y2 - radius, radius, 270, 360);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x1 + radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x1, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1 + radius, 0.0D).endVertex();
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
        for (int i = startAngle; i <= endAngle; i += 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }
}