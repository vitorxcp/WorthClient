package com.vitorxp.WorthClient.hud;
import net.minecraft.item.ItemStack;
import com.vitorxp.WorthClient.WorthClient;

public class LeggingsHUD extends SingleItemHUD {
    public LeggingsHUD() { super("LeggingsHUD", 10, 46); }
    @Override protected ItemStack getItemToRender() { return mc.thePlayer.getCurrentArmor(1); }
    @Override protected boolean isEnabled() { return WorthClient.leggingsHUDOverlay; }
    @Override protected String getStyle() { return WorthClient.leggingsStyle; }
}