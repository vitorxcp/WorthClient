package com.vitorxp.WorthClient.hud;
import net.minecraft.item.ItemStack;
import com.vitorxp.WorthClient.WorthClient;

public class ChestplateHUD extends SingleItemHUD {
    public ChestplateHUD() { super("ChestplateHUD", 10, 28); }
    @Override protected ItemStack getItemToRender() { return mc.thePlayer.getCurrentArmor(2); }
    @Override protected boolean isEnabled() { return WorthClient.chestplateHUDOverlay; }
    @Override protected String getStyle() { return WorthClient.chestplateStyle; }
}