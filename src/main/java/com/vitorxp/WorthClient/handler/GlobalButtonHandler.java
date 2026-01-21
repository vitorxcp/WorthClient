package com.vitorxp.WorthClient.handler;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GlobalButtonHandler {

    private static Field buttonListField = null;
    private final GuiModernButton wrapperButton = new GuiModernButton(0, 0, 0, 0, 0, "");

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.buttonList == null) return;

        List<GuiButton> newButtons = new ArrayList<>();
        boolean changed = false;

        for (GuiButton button : event.buttonList) {
            if (button instanceof GuiModernButton) {
                newButtons.add(button);
                continue;
            }

            if (button.getClass() == GuiButton.class) {
                GuiModernButton modernBtn = new GuiModernButton(
                        button.id,
                        button.xPosition,
                        button.yPosition,
                        button.width,
                        button.height,
                        button.displayString
                );
                modernBtn.enabled = button.enabled;
                modernBtn.visible = button.visible;
                newButtons.add(modernBtn);
                changed = true;
            } else {
                newButtons.add(button);
            }
        }

        if (changed) {
            event.buttonList.clear();
            event.buttonList.addAll(newButtons);
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui == null) return;

        List<GuiButton> buttons = getButtonList(event.gui);
        if (buttons == null) return;

        for (GuiButton button : buttons) {
            if (!(button instanceof GuiModernButton) && button.visible) {

                wrapperButton.xPosition = button.xPosition;
                wrapperButton.yPosition = button.yPosition;
                wrapperButton.width = button.width;
                wrapperButton.height = button.height;
                wrapperButton.displayString = button.displayString;
                wrapperButton.enabled = button.enabled;
                wrapperButton.drawButton(Minecraft.getMinecraft(), event.mouseX, event.mouseY);
            }
        }
    }

    // Método seguro para acessar a lista protegida 'buttonList'
    @SuppressWarnings("unchecked")
    private List<GuiButton> getButtonList(GuiScreen gui) {
        try {
            if (buttonListField == null) {
                try {
                    buttonListField = GuiScreen.class.getDeclaredField("buttonList");
                } catch (NoSuchFieldException e) {
                    buttonListField = GuiScreen.class.getDeclaredField("field_146292_n");
                }
                buttonListField.setAccessible(true);
            }
            return (List<GuiButton>) buttonListField.get(gui);
        } catch (Exception e) {
            // Se falhar, falha silencisamente para não spammar o log
            return null;
        }
    }
}