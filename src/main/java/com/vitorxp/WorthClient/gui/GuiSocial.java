package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.manager.SkinManager;
import com.vitorxp.WorthClient.socket.ClientSocket;
import com.vitorxp.WorthClient.social.SocialManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GuiSocial extends GuiScreen {

    private GuiTextField inputField;
    private GuiTextField ticketSubjectField;

    private int sidebarWidth = 180;
    private final int TOP_BAR_HEIGHT = 50;

    private boolean showingTickets = false;
    private boolean showStatusMenu = false;
    private boolean showCreateTicketModal = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private final int C_BG_DARK     = 0xFF121212;
    private final int C_BG_SIDEBAR  = 0xFF18181B;
    private final int C_ACCENT      = 0xFFEAB308;
    private final int C_BORDER      = 0xFF27272A;
    private final int C_HOVER       = 0xFF27272A;
    private final int C_SELECTED    = 0x40EAB308;
    private final int C_TEXT_MAIN   = 0xFFF4F4F5;
    private final int C_TEXT_SEC    = 0xFFA1A1AA;
    private final int C_RED         = 0xFFEF4444;
    private final int C_GREEN       = 0xFF10B981;

    public GuiSocial() {
        super();
    }

    public GuiSocial(String targetNick) {
        super();
        this.selectChat(targetNick, false);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        if (this.width < 400) sidebarWidth = 120;
        else sidebarWidth = 180;

        int inputX = sidebarWidth + 20;
        int inputWidth = this.width - sidebarWidth - 40;

        this.inputField = new GuiTextField(0, this.fontRendererObj, inputX, this.height - 35, inputWidth, 18);
        this.inputField.setMaxStringLength(256);
        this.inputField.setFocused(true);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setTextColor(0xFFFFFF);

        int modalX = this.width / 2 - 100;
        int modalY = this.height / 2 - 20;
        this.ticketSubjectField = new GuiTextField(1, this.fontRendererObj, modalX + 10, modalY + 20, 180, 20);
        this.ticketSubjectField.setMaxStringLength(50);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        this.inputField.updateCursorCounter();
        this.ticketSubjectField.updateCursorCounter();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (showCreateTicketModal) return;

        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            scrollOffset += (dWheel > 0 ? 35 : -35);
            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (showCreateTicketModal) showCreateTicketModal = false;
            else if (showStatusMenu) showStatusMenu = false;
            else super.keyTyped(typedChar, keyCode);
            return;
        }

        if (showCreateTicketModal) {
            if (keyCode == Keyboard.KEY_RETURN) createTicket();
            this.ticketSubjectField.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            if (this.inputField.isFocused()) sendMessage();
            return;
        }

        this.inputField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showCreateTicketModal) {
            this.ticketSubjectField.mouseClicked(mouseX, mouseY, mouseButton);
            int modalX = this.width / 2 - 100;
            int modalY = this.height / 2 - 50;
            if (isHover(mouseX, mouseY, modalX + 110, modalY + 80, 80, 20)) createTicket();
            if (isHover(mouseX, mouseY, modalX + 10, modalY + 80, 80, 20)) showCreateTicketModal = false;
            return;
        }

        if (showStatusMenu) {
            int menuX = 5, menuY = this.height - 110;
            if (isHover(mouseX, mouseY, menuX, menuY, 100, 20)) setStatus("online");
            else if (isHover(mouseX, mouseY, menuX, menuY + 20, 100, 20)) setStatus("ocupado");
            else if (isHover(mouseX, mouseY, menuX, menuY + 40, 100, 20)) setStatus("ausente");
            else if (isHover(mouseX, mouseY, menuX, menuY + 60, 100, 20)) setStatus("offline");
            else showStatusMenu = false;
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX < sidebarWidth) {
            if (mouseY < 40) {
                boolean clickFriends = mouseX < sidebarWidth / 2;
                if (showingTickets == clickFriends) {
                    showingTickets = !clickFriends;
                    SocialManager.currentChatTarget = null;
                    scrollOffset = 0;
                    if (showingTickets) ClientSocket.socket.emit("ticket:list");
                }
                return;
            }

            if (showingTickets && isHover(mouseX, mouseY, 10, 50, sidebarWidth - 20, 20)) {
                showCreateTicketModal = true;
                ticketSubjectField.setFocused(true);
                ticketSubjectField.setText("");
                return;
            }

            int listY = showingTickets ? 80 : 50;

            if (!showingTickets) {
                for (SocialManager.Friend f : SocialManager.friends) {
                    if (isHover(mouseX, mouseY, 0, listY, sidebarWidth, 35)) {
                        selectChat(f.nick, false);
                        break;
                    }
                    listY += 36;
                }
            } else {
                for (SocialManager.Ticket t : SocialManager.tickets) {
                    if (isHover(mouseX, mouseY, 0, listY, sidebarWidth, 35)) {
                        selectChat(t.id, true);
                        break;
                    }
                    listY += 36;
                }
            }

            if (mouseY > this.height - 40) showStatusMenu = true;
        }

        if (SocialManager.currentChatTarget != null && SocialManager.isTicketChat) {
            if (isHover(mouseX, mouseY, this.width - 100, 15, 90, 20)) closeTicket();
        }
    }

    private void selectChat(String target, boolean isTicket) {
        SocialManager.currentChatTarget = target;
        SocialManager.isTicketChat = isTicket;
        scrollOffset = 0;

        if (!isTicket) {
            for (SocialManager.Friend f : SocialManager.friends) {
                if (f.nick.equals(target)) {
                    f.hasUnread = false;
                    break;
                }
            }
            if (ClientSocket.socket != null) {
                ClientSocket.socket.emit("chat:select", target);
                ClientSocket.socket.emit("chat:mark_read", target);
            }
        } else {
            if (ClientSocket.socket != null) {
                ClientSocket.socket.emit("ticket:join", target);
                ClientSocket.socket.emit("ticket:mark_read", target);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, C_BG_DARK);
        drawRect(0, 0, sidebarWidth, this.height, C_BG_SIDEBAR);
        drawRect(sidebarWidth, 0, sidebarWidth + 1, this.height, C_BORDER);

        drawRect(0, 0, sidebarWidth, 40, C_BG_SIDEBAR);
        drawRect(0, 39, sidebarWidth, 40, C_BORDER);
        int tabWidth = sidebarWidth / 2;

        drawRect(showingTickets ? tabWidth : 0, 38, showingTickets ? sidebarWidth : tabWidth, 40, C_ACCENT);

        drawCenteredString(fontRendererObj, "Amigos", tabWidth / 2, 16, !showingTickets ? C_TEXT_MAIN : C_TEXT_SEC);
        drawCenteredString(fontRendererObj, "Suporte", tabWidth + (tabWidth / 2), 16, showingTickets ? C_TEXT_MAIN : C_TEXT_SEC);

        int listY = 50;

        if (showingTickets) {
            boolean hoverCreate = isHover(mouseX, mouseY, 10, listY, sidebarWidth - 20, 20);
            drawRect(10, listY, sidebarWidth - 10, listY + 20, hoverCreate ? 0xFF3F3F46 : 0xFF27272A);
            drawCenteredString(fontRendererObj, "+ Novo Ticket", sidebarWidth / 2, listY + 6, C_ACCENT);
            listY += 30;

            for (SocialManager.Ticket t : SocialManager.tickets) {
                boolean selected = t.id.equals(SocialManager.currentChatTarget);
                boolean hover = isHover(mouseX, mouseY, 0, listY, sidebarWidth, 35);

                if (selected) drawRect(2, listY, sidebarWidth - 2, listY + 34, 0xFF2A2A2E);
                else if (hover) drawRect(2, listY, sidebarWidth - 2, listY + 34, C_HOVER);

                int statusCol = t.status.equals("open") ? C_GREEN : C_RED;

                drawRect(10, listY + 10, 14, listY + 24, statusCol);

                fontRendererObj.drawString("Ticket #" + t.id, 20, listY + 6, C_TEXT_MAIN);
                fontRendererObj.drawString(limitString(t.subject, 18), 20, listY + 18, C_TEXT_SEC);
                listY += 36;
            }
        } else {
            for (SocialManager.Friend f : SocialManager.friends) {
                boolean selected = f.nick.equals(SocialManager.currentChatTarget);
                boolean hover = isHover(mouseX, mouseY, 0, listY, sidebarWidth, 35);

                if (selected) {
                    drawRect(5, listY, sidebarWidth - 5, listY + 34, 0xFF27272A);
                    drawRect(sidebarWidth - 3, listY + 10, sidebarWidth, listY + 24, C_ACCENT);
                } else if (hover) {
                    drawRect(5, listY, sidebarWidth - 5, listY + 34, 0x1AFFFFFF);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                ResourceLocation skin = SkinManager.getSkin(f.nick);
                Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
                Gui.drawScaledCustomSizeModalRect(10, listY + 3, 8.0F, 8.0F, 8, 8, 28, 28, 64.0F, 64.0F);

                int statCol = getStatusColor(f.status);
                drawRect(32, listY + 24, 38, listY + 30, 0xFF18181B);
                drawRect(33, listY + 25, 37, listY + 29, statCol);

                fontRendererObj.drawString(limitString(f.nick, 12), 45, listY + 6, selected ? C_ACCENT : C_TEXT_MAIN);
                fontRendererObj.drawString(f.status, 45, listY + 18, C_TEXT_SEC);

                if (f.hasUnread) {
                    drawRect(sidebarWidth - 14, listY + 12, sidebarWidth - 6, listY + 20, C_ACCENT);
                }

                listY += 36;
            }
        }

        int footerY = this.height - 40;
        drawRect(0, footerY, sidebarWidth, footerY + 1, C_BORDER);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation mySkin = SkinManager.getSkin(Minecraft.getMinecraft().getSession().getUsername());
        Minecraft.getMinecraft().getTextureManager().bindTexture(mySkin);
        Gui.drawScaledCustomSizeModalRect(10, footerY + 8, 8, 8, 8, 8, 24, 24, 64, 64);

        fontRendererObj.drawString(limitString(Minecraft.getMinecraft().getSession().getUsername(), 12), 40, footerY + 8, C_TEXT_MAIN);
        fontRendererObj.drawString(SocialManager.myStatus, 40, footerY + 20, getStatusColor(SocialManager.myStatus));

        if (showStatusMenu) {
            int mx = 5, my = footerY - 85;
            drawRect(mx, my, mx + 100, footerY - 5, C_BG_SIDEBAR);
            drawRect(mx, my, mx+1, footerY-5, C_BORDER);
            drawRect(mx+99, my, mx+100, footerY-5, C_BORDER);
            drawRect(mx, my, mx+100, my+1, C_BORDER);

            drawStatusOption(mx, my, "Online", C_GREEN, mouseX, mouseY);
            drawStatusOption(mx, my + 20, "Ocupado", C_RED, mouseX, mouseY);
            drawStatusOption(mx, my + 40, "Ausente", 0xFFFFAA00, mouseX, mouseY);
            drawStatusOption(mx, my + 60, "Offline", 0xFFAAAAAA, mouseX, mouseY);
        }

        if (SocialManager.currentChatTarget != null) {
            drawRect(sidebarWidth, 0, this.width, TOP_BAR_HEIGHT, C_BG_DARK);
            drawRect(sidebarWidth, TOP_BAR_HEIGHT - 1, this.width, TOP_BAR_HEIGHT, C_BORDER);

            String title = SocialManager.isTicketChat ? "Ticket #" + SocialManager.currentChatTarget : SocialManager.currentChatTarget;
            String subTitle = SocialManager.isTicketChat ? "Suporte WorthClient" : "Amigo";

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ResourceLocation headerSkin = SocialManager.isTicketChat ? new ResourceLocation("textures/items/book_writable.png") : SkinManager.getSkin(SocialManager.currentChatTarget);
            Minecraft.getMinecraft().getTextureManager().bindTexture(headerSkin);

            if (SocialManager.isTicketChat) {
                Gui.drawModalRectWithCustomSizedTexture(sidebarWidth + 20, 10, 0, 0, 30, 30, 30, 30);

                boolean hoverClose = isHover(mouseX, mouseY, this.width - 100, 15, 90, 20);
                drawRect(this.width - 100, 15, this.width - 10, 35, hoverClose ? 0x40EF4444 : 0x20EF4444);
                drawCenteredString(fontRendererObj, "Fechar Ticket", this.width - 55, 21, C_RED);
            } else {
                Gui.drawScaledCustomSizeModalRect(sidebarWidth + 20, 10, 8, 8, 8, 8, 30, 30, 64, 64);
            }

            fontRendererObj.drawStringWithShadow(title, sidebarWidth + 60, 14, C_TEXT_MAIN);
            fontRendererObj.drawString(subTitle, sidebarWidth + 60, 26, C_TEXT_SEC);

            drawRect(sidebarWidth, this.height - 50, this.width, this.height, C_BG_DARK);
            drawRect(sidebarWidth, this.height - 51, this.width, this.height - 50, C_BORDER);

            int boxX = sidebarWidth + 15;
            int boxY = this.height - 40;
            int boxW = this.width - boxX - 15;
            drawRect(boxX, boxY, boxX + boxW, this.height - 10, 0xFF27272A);

            this.inputField.drawTextBox();

            renderMessages();
        } else {
            drawCenteredString(fontRendererObj, "Selecione uma conversa para começar", sidebarWidth + (width - sidebarWidth) / 2, height / 2, C_TEXT_SEC);
        }

        if (showCreateTicketModal) {
            drawRect(0, 0, width, height, 0xCC000000);

            int mx = width / 2 - 100;
            int my = height / 2 - 50;

            drawRect(mx, my, mx + 200, my + 110, C_BG_SIDEBAR);
            drawRect(mx, my, mx + 200, my + 1, C_ACCENT);
            drawCenteredString(fontRendererObj, "Novo Ticket", width / 2, my + 10, C_ACCENT);
            fontRendererObj.drawString("Assunto:", mx + 10, my + 30, C_TEXT_SEC);

            this.ticketSubjectField.drawTextBox();

            boolean hoverCancel = isHover(mouseX, mouseY, mx + 10, my + 80, 80, 20);
            boolean hoverCreate = isHover(mouseX, mouseY, mx + 110, my + 80, 80, 20);

            drawRect(mx + 10, my + 80, mx + 90, my + 100, hoverCancel ? 0xFF3F3F46 : 0xFF27272A);
            drawCenteredString(fontRendererObj, "Cancelar", mx + 50, my + 86, C_TEXT_MAIN);
            drawRect(mx + 110, my + 80, mx + 190, my + 100, hoverCreate ? 0xFFCA8A04 : C_ACCENT);
            drawCenteredString(fontRendererObj, "Criar", mx + 150, my + 86, 0xFF000000); // Texto preto no amarelo
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderMessages() {
        List<SocialManager.ChatMessage> msgs = SocialManager.getMessages(SocialManager.currentChatTarget);

        int startY = this.height - 70 + scrollOffset;
        int totalHeightCalc = 0;

        GL11.glPushMatrix();
        for (int i = msgs.size() - 1; i >= 0; i--) {
            SocialManager.ChatMessage msg = msgs.get(i);

            int msgHeight = 35;
            totalHeightCalc += msgHeight;

            if (startY < TOP_BAR_HEIGHT) {
                startY -= msgHeight;
                continue;
            }
            if (startY > this.height - 60) {
                startY -= msgHeight;
                continue;
            }

            boolean isMe = msg.sender.equals(Minecraft.getMinecraft().getSession().getUsername());
            String text = msg.text;
            int txtW = fontRendererObj.getStringWidth(text);

            int bubblePaddingX = 8;
            int bubblePaddingY = 6;
            int bubbleW = txtW + (bubblePaddingX * 2);

            int bubbleX;
            if (isMe) {
                bubbleX = this.width - bubbleW - 20;
            } else {
                bubbleX = sidebarWidth + 20;
            }

            int bubbleColor = isMe ? 0xFF2F2F20 : 0xFF27272A;
            int bubbleBorder = isMe ? 0x40EAB308 : 0xFF3F3F46;

            drawRect(bubbleX, startY - bubblePaddingY, bubbleX + bubbleW, startY + 8 + bubblePaddingY, bubbleColor);
            drawRect(bubbleX, startY - bubblePaddingY, bubbleX + bubbleW, startY - bubblePaddingY + 1, bubbleBorder);
            drawRect(bubbleX, startY + 8 + bubblePaddingY - 1, bubbleX + bubbleW, startY + 8 + bubblePaddingY, bubbleBorder);
            drawRect(bubbleX, startY - bubblePaddingY, bubbleX + 1, startY + 8 + bubblePaddingY, bubbleBorder);
            drawRect(bubbleX + bubbleW - 1, startY - bubblePaddingY, bubbleX + bubbleW, startY + 8 + bubblePaddingY, bubbleBorder);

            if (!isMe) {
                // fontRendererObj.drawString(msg.sender, bubbleX, startY - 15, C_ACCENT);
            }

            fontRendererObj.drawString(text, bubbleX + bubblePaddingX, startY, isMe ? C_TEXT_MAIN : C_TEXT_MAIN);

            GlStateManager.pushMatrix();
            float scale = 0.6f;
            GlStateManager.scale(scale, scale, scale);

            String time = new SimpleDateFormat("HH:mm").format(new Date(msg.timestamp));
            String statusIcon = (isMe && msg.read) ? "Lida" : "";

            int metaX = (int) ((bubbleX + bubbleW - (fontRendererObj.getStringWidth(time) * scale) - 5) / scale);
            int metaY = (int) ((startY + 10) / scale);

            fontRendererObj.drawString(time, metaX, metaY, 0xAAFFFFFF);

            if (isMe) {
                int statusColor = msg.read ? 0xFF60A5FA : 0xFFAAAAAA;
                fontRendererObj.drawString(msg.read ? "✔✔" : "✔", metaX - 10, metaY, statusColor);
            }

            GlStateManager.popMatrix();

            startY -= msgHeight;
        }
        GL11.glPopMatrix();

        maxScroll = Math.max(0, totalHeightCalc - (this.height - 130));
    }

    private void setStatus(String s) {
        SocialManager.myStatus = s;
        showStatusMenu = false;
        if(ClientSocket.socket != null) ClientSocket.socket.emit("status:change", s);
    }

    private void createTicket() {
        String subj = ticketSubjectField.getText().trim();
        if(subj.isEmpty()) return;
        try {
            JSONObject obj = new JSONObject();
            obj.put("subject", subj);
            ClientSocket.socket.emit("ticket:create", obj);
        } catch(Exception e) { e.printStackTrace(); }
        showCreateTicketModal = false;
    }

    private void closeTicket() {
        if (ClientSocket.socket != null) ClientSocket.socket.emit("ticket:close", SocialManager.currentChatTarget);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if(text.isEmpty()) return;

        String myNick = Minecraft.getMinecraft().getSession().getUsername();
        SocialManager.addMessage(SocialManager.currentChatTarget, new SocialManager.ChatMessage(myNick, text, System.currentTimeMillis(), true));

        try {
            JSONObject obj = new JSONObject();
            if (SocialManager.isTicketChat) {
                obj.put("ticketId", SocialManager.currentChatTarget);
                obj.put("text", text);
                ClientSocket.socket.emit("ticket:send", obj);
            } else {
                obj.put("targetNick", SocialManager.currentChatTarget);
                obj.put("text", text);
                ClientSocket.socket.emit("chat:send", obj);
            }
        } catch(Exception e) { e.printStackTrace(); }

        inputField.setText("");
        scrollOffset = 0;
    }

    private void drawStatusOption(int x, int y, String text, int color, int mx, int my) {
        boolean hover = isHover(mx, my, x, y, 100, 20);
        drawRect(x, y, x + 100, y + 20, hover ? 0xFF27272A : C_BG_SIDEBAR);

        drawRect(x + 8, y + 7, x + 14, y + 13, color);
        fontRendererObj.drawString(text, x + 20, y + 6, C_TEXT_MAIN);
    }

    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int getStatusColor(String s) {
        if(s == null) return 0xFFAAAAAA;
        switch(s.toLowerCase()) {
            case "online": return C_GREEN;
            case "ocupado": return C_RED;
            case "ausente": return 0xFFFFAA00;
            default: return 0xFFAAAAAA;
        }
    }

    public void drawCenteredString(net.minecraft.client.gui.FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
    }

    private String limitString(String s, int len) {
        if (s == null) return "";
        if (s.length() > len) return s.substring(0, len) + "...";
        return s;
    }
}