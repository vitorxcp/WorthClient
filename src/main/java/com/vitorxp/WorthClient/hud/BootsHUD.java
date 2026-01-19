package com.vitorxp.WorthClient.hud;
import net.minecraft.item.ItemStack;
import com.vitorxp.WorthClient.WorthClient;

public class BootsHUD extends SingleItemHUD {
    public BootsHUD() { super("BootsHUD", 10, 64); }
    @Override protected ItemStack getItemToRender() { return mc.thePlayer.getCurrentArmor(0); }
    @Override protected boolean isEnabled() { return WorthClient.bootsHUDOverlay; }
    @Override protected String getStyle() { return WorthClient.bootsStyle; }
}