package com.vitorxp.WorthClient.hud;
import net.minecraft.item.ItemStack;
import com.vitorxp.WorthClient.WorthClient;

public class HelmetHUD extends SingleItemHUD {
    public HelmetHUD() { super("HelmetHUD", 10, 10); }
    @Override protected ItemStack getItemToRender() { return mc.thePlayer.getCurrentArmor(3); }
    @Override protected boolean isEnabled() { return WorthClient.helmetHUDOverlay; }
    @Override protected String getStyle() { return WorthClient.helmetStyle; }
}