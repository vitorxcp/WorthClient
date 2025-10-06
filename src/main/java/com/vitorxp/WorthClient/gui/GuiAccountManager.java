package com.vitorxp.WorthClient.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.vitorxp.WorthClient.account.Account;
import com.vitorxp.WorthClient.account.AccountManager;
import com.vitorxp.WorthClient.account.SessionManager;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiAccountManager extends GuiScreen implements GuiYesNoCallback {

    private final GuiScreen parentScreen;
    private GuiAccountList accountList;
    private Account selectedAccount;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation ICON_MICROSOFT = new ResourceLocation("worthclient", "textures/icons/microsoft_icon.png");
    private static final ResourceLocation ICON_CRACKED = new ResourceLocation("worthclient", "textures/icons/offline_icon.png");
    private static final ResourceLocation ICON_DELETE = new ResourceLocation("worthclient", "textures/icons/delete_icon.png");
    private static final ResourceLocation ICON_CHECK = new ResourceLocation("worthclient", "textures/icons/check_icon.png");

    public GuiAccountManager(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        AccountManager.loadAccounts();

        this.accountList = new GuiAccountList(this, mc, this.width, this.height, 40, this.height - 90, 58);

        this.buttonList.clear();
        int btnWidth = 160;
        int btnSpacing = 10;
        int centerX = this.width / 2;

        this.buttonList.add(new GuiModernButton(0, centerX - btnWidth - btnSpacing / 2, this.height - 48, btnWidth, 22, "Adicionar Conta", 0L));
        this.buttonList.add(new GuiModernButton(1, centerX + btnSpacing / 2, this.height - 48, btnWidth, 22, "Login Rápido", 0L));
        this.buttonList.add(new GuiModernButton(2, this.width / 2 - (btnWidth / 2), this.height - 24, btnWidth, 20, "Voltar", 0L));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.accountList != null) this.accountList.handleMouseInput();
    }

    public void selectAccount(int index) {
        List<Account> accounts = AccountManager.getAccounts();
        this.selectedAccount = (index >= 0 && index < accounts.size()) ? accounts.get(index) : null;
    }

    public boolean isAccountSelected(int index) {
        List<Account> accounts = AccountManager.getAccounts();
        return index >= 0 && index < accounts.size() && accounts.get(index).equals(this.selectedAccount);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(new GuiAddAccount(this));
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiCustomPrompt(this, "§eDigite seu nick:", new GuiCustomPrompt.Callback() {
                    @Override
                    public void accept(String nick) {
                        if (nick != null && !nick.trim().isEmpty()) {
                            SessionManager.loginCracked(nick);
                        }
                        mc.displayGuiScreen(GuiAccountManager.this);
                    }
                }));
                break;
            case 2:
                this.mc.displayGuiScreen(this.parentScreen);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 160).getRGB());

        String title = "Gerenciador de Contas";
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2 + 1, 13, new Color(0, 0, 0, 150).getRGB());
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, 12, 0xFFD700); // Dourado

        if (this.accountList != null) this.accountList.drawScreen(mouseX, mouseY, partialTicks);

        String prefix = "Logado como: ";
        String username = (mc.getSession() != null) ? mc.getSession().getUsername() : "Ninguém";
        int footerY = this.height - 80;
        int prefixWidth = this.fontRendererObj.getStringWidth(prefix);
        int totalWidth = prefixWidth + this.fontRendererObj.getStringWidth(username);
        int startX = (this.width - totalWidth) / 2;
        this.fontRendererObj.drawString(prefix, startX, footerY, 0xCCCCCC);
        this.fontRendererObj.drawStringWithShadow(username, startX + prefixWidth, footerY, 0xFFD700);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void confirmClicked(boolean result, int slotIndexToDelete) {
        if (result) {
            List<Account> accounts = AccountManager.getAccounts();
            if (slotIndexToDelete >= 0 && slotIndexToDelete < accounts.size()) {
                AccountManager.removeAccount(accounts.get(slotIndexToDelete));
                if (selectedAccount != null && selectedAccount.equals(accounts.get(slotIndexToDelete))) {
                    selectedAccount = null;
                }
            }
        }
        this.mc.displayGuiScreen(this);
    }

    static class GuiAccountList extends GuiSlot {
        private final GuiAccountManager parent;
        private final Map<String, ResourceLocation> skinCache = new HashMap<>();

        public GuiAccountList(GuiAccountManager parent, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
            super(mc, width, height, top, bottom, slotHeight);
            this.parent = parent;
            this.setHasListHeader(false, 0);

            int listWidth = 340;
            this.left = (width - listWidth) / 2;
            this.right = this.left + listWidth;
        }

        @Override
        protected int getSize() {
            return AccountManager.getAccounts().size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            this.parent.selectAccount(slotIndex);
            if (slotIndex < 0 || slotIndex >= AccountManager.getAccounts().size()) return;
            Account account = AccountManager.getAccounts().get(slotIndex);

            int cardWidth = 320;
            int cardX = this.left + (this.right - this.left - cardWidth) / 2;
            int cardY = this.top + slotIndex * this.slotHeight - (int) this.amountScrolled + 4;
            int cardHeight = this.slotHeight - 8;

            int iconAreaX = cardX + cardWidth - 24;
            int deleteY = cardY + cardHeight - 22;

            boolean clickedOnCard = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;

            if (mouseX >= iconAreaX && mouseX <= iconAreaX + 12 && mouseY >= deleteY && mouseY <= deleteY + 12) {
                String question = "Deletar a conta '" + account.username + "'?";
                String detail = "Essa ação não pode ser desfeita.";
                mc.displayGuiScreen(new GuiYesNo(this.parent, question, detail, "Deletar", "Cancelar", slotIndex));
            } else if (isDoubleClick && clickedOnCard) {
                SessionManager.switchAccount(account);
                mc.displayGuiScreen(this.parent.parentScreen);
            }
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return this.parent.isAccountSelected(slotIndex);
        }

        @Override
        protected void drawBackground() { /* Fundo principal já desenhado */ }

        @Override
        protected void drawSlot(int slotIndex, int x, int y, int slotHeightParam, int mouseX, int mouseY) {
            Account account = AccountManager.getAccounts().get(slotIndex);
            if (account == null) return;

            boolean isSelected = isSelected(slotIndex);
            boolean isCurrent = mc.getSession() != null && account.username.equalsIgnoreCase(mc.getSession().getUsername());

            int cardWidth = 320;
            int cardX = this.left + (this.right - this.left - cardWidth) / 2;
            int cardY = y + 4;
            int cardHeight = slotHeightParam - 8;

            boolean isHovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;

            if (isCurrent) {
                RenderUtil.drawRoundedRect(cardX - 1, cardY - 1, cardWidth + 2, cardHeight + 2, 7f, new Color(255, 215, 0).getRGB());
            } else if (isSelected) {
                RenderUtil.drawRoundedRect(cardX - 1, cardY - 1, cardWidth + 2, cardHeight + 2, 7f, new Color(255, 255, 255, 80).getRGB());
            }

            int baseColor = isHovered ? new Color(60, 60, 60, 230).getRGB() : new Color(45, 45, 45, 230).getRGB();
            RenderUtil.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 6f, baseColor);

            GlStateManager.color(1f, 1f, 1f, 1f);
            ResourceLocation skin = getSkin(account);
            this.mc.getTextureManager().bindTexture(skin);
            int skinY = cardY + (cardHeight - 32) / 2;
            Gui.drawScaledCustomSizeModalRect(cardX + 10, skinY, 8, 8, 8, 8, 32, 32, 64, 64);  // Rosto
            Gui.drawScaledCustomSizeModalRect(cardX + 10, skinY, 40, 8, 8, 8, 32, 32, 64, 64); // Chapéu

            int infoX = cardX + 52;
            int nameColor = isCurrent ? 0xFFD700 : 0xFFFFFF;
            this.mc.fontRendererObj.drawStringWithShadow(account.username, infoX, cardY + 12, nameColor);

            ResourceLocation iconType = account.isCracked() ? ICON_CRACKED : ICON_MICROSOFT;
            mc.getTextureManager().bindTexture(iconType);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
            drawModalRectWithCustomSizedTexture(infoX, cardY + 28, 0, 0, 12, 12, 12, 12);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            String typeText = account.isCracked() ? "Offline" : "Microsoft";
            this.mc.fontRendererObj.drawString(typeText, infoX + 16, cardY + 30, 0xAAAAAA);

            int iconAreaX = cardX + cardWidth - 24;

            if (isCurrent) {
                mc.getTextureManager().bindTexture(ICON_CHECK);
                drawModalRectWithCustomSizedTexture(iconAreaX, cardY + 10, 0, 0, 12, 12, 12, 12);
            }

            if (isHovered) {
                int deleteY = cardY + cardHeight - 22;
                boolean isHoveringDelete = mouseX >= iconAreaX && mouseX <= iconAreaX + 12 && mouseY >= deleteY && mouseY <= deleteY + 12;

                mc.getTextureManager().bindTexture(ICON_DELETE);
                if (isHoveringDelete) {
                    GlStateManager.color(1.0f, 0.4f, 0.4f, 1.0f);
                } else {
                    GlStateManager.color(0.6f, 0.6f, 0.6f, 1.0f);
                }
                drawModalRectWithCustomSizedTexture(iconAreaX, deleteY, 0, 0, 12, 12, 12, 12);
                GlStateManager.color(1f, 1f, 1f, 1f);
            }
        }

        private ResourceLocation getSkin(Account account) {
            String username = account.username;
            if (skinCache.containsKey(username)) {
                return skinCache.get(username);
            }

            UUID playerUUID;
            if (account.isCracked() || account.uuid == null || account.uuid.isEmpty()) {
                playerUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
            } else {
                try {
                    playerUUID = UUID.fromString(account.uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                } catch (IllegalArgumentException e) {
                    playerUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
                }
            }
            GameProfile profile = new GameProfile(playerUUID, username);

            net.minecraft.client.resources.SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinManager.loadSkinFromCache(profile);

            ResourceLocation location = AbstractClientPlayer.getLocationSkin(username);

            skinCache.put(username, location);
            return location;
        }
    }
}