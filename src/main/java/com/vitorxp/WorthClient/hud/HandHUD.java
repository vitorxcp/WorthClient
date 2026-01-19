package com.vitorxp.WorthClient.hud;
import net.minecraft.item.ItemStack;
import com.vitorxp.WorthClient.WorthClient;

public class HandHUD extends SingleItemHUD {
    public HandHUD() { super("HandHUD", 10, 82); }
    @Override protected ItemStack getItemToRender() { return mc.thePlayer.getHeldItem(); }
    @Override protected boolean isEnabled() { return WorthClient.mainHandHUDOverlay; }
    @Override protected String getStyle() { return WorthClient.mainHandStyle; }
}