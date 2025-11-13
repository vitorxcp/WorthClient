package com.vitorxp.WorthClient.hud;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static com.vitorxp.WorthClient.utils.RenderUtil.drawRect;

public class LookAtHUD extends HudElement {

    private final Minecraft mc = Minecraft.getMinecraft();

    public static String name = "§7...";
    public static String status = "";
    public static String modSource = "";
    private ItemStack previewStack = null;

    public LookAtHUD() {
        super("LookAtHUD", 10, 70);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null) return;

        name = "§7...";
        status = "";
        modSource = "";
        previewStack = null;

        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = mop.getBlockPos();
            IBlockState state = mc.theWorld.getBlockState(pos);
            Block block = state.getBlock();

            if (block != null) {
                name = "§f" + block.getLocalizedName();
                modSource = "§9" + getModFromBlock(block);
                previewStack = new ItemStack(block);

                TileEntity te = mc.theWorld.getTileEntity(pos);
                if (te != null) {
                    String teName = te.getClass().getSimpleName();
                    if (teName.toLowerCase().contains("energy") || teName.toLowerCase().contains("power")) {
                        status = "§c40kFE §7/ §c40kFE";
                    } else {
                        status = "§7Ativo";
                    }
                }
            }
        } else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Entity e = mop.entityHit;
            if (e != null) {
                if (e instanceof EntityArmorStand) {
                    name = "§bArmor Stand";
                    if (e.hasCustomName()) name = "§e" + e.getCustomNameTag();
                    previewStack = new ItemStack(net.minecraft.init.Blocks.stone_slab);
                    modSource = "§7Vanilla";
                } else if (e instanceof EntityPlayer) {
                    name = "§a" + e.getName();
                    status = "§7Jogador";
                    modSource = "§7SkyBlock";
                } else {
                    name = "§eEntidade";
                    if (e.hasCustomName()) name = "§e" + e.getCustomNameTag();
                    status = "§7Tipo: " + e.getClass().getSimpleName();
                    modSource = "§7Entidade";
                }
            }
        } else return;

        drawHudBox();
    }

    private void drawHudBox() {
        ScaledResolution sr = new ScaledResolution(mc);

        int boxWidth = 140;
        int boxHeight = 45;

        drawRect(this.x, this.y, this.x + boxWidth, this.y + boxHeight, 0x90000000);
        drawRect(this.x, this.y, this.x + boxWidth, this.y + 1, 0xFF00A8FF);

        if (previewStack != null) {
            renderItem(previewStack, this.x + 5, this.y + 5);
        }

        int textX = this.x + 28;
        int textY = this.y + 6;
        fontRenderer.drawStringWithShadow(name, textX, textY, 0xFFFFFF);

        if (!status.isEmpty())
            fontRenderer.drawStringWithShadow(status, textX, textY + 10, 0xCCCCCC);

        if (!modSource.isEmpty())
            fontRenderer.drawStringWithShadow(modSource, textX, textY + 22, 0x4BA3FF);
    }

    private void renderItem(ItemStack stack, int x, int y) {
        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

    private String getModFromBlock(Block block) {
        try {
            String registry = Block.blockRegistry.getNameForObject(block).toString();
            if (registry.contains(":")) {
                String modid = registry.split(":")[0];
                if (modid.equalsIgnoreCase("minecraft")) return "Minecraft";
                return capitalize(modid);
            }
        } catch (Exception ignored) {}
        return "Desconhecido";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 45;
    }
}
