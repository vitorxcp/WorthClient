package com.vitorxp.WorthClient.handlers;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.config.AnimationsConfig;
import com.vitorxp.WorthClient.particles.EntityAlivePortalFX;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;
import java.util.Random;

public class PortalEffectHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random rand = new Random();
    private List[][] fxLayers = null;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(!AnimationsConfig.enabled || !WorthClient.animationPortal) return;
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) return;

        removeVanillaParticles();

        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        int range = 16;
        int samples = 100;

        for (int i = 0; i < samples; i++) {
            int dx = rand.nextInt(range * 2 + 1) - range;
            int dy = rand.nextInt(range * 2 + 1) - range;
            int dz = rand.nextInt(range * 2 + 1) - range;

            BlockPos pos = playerPos.add(dx, dy, dz);
            IBlockState state = mc.theWorld.getBlockState(pos);

            if (state.getBlock() == Blocks.portal) {
                spawnRealistcPoralParticles(pos, 0);
            } else if (state.getBlock() == Blocks.end_portal) {
                spawnRealistcPoralParticles(pos, 1);
            }
        }
    }

    private void removeVanillaParticles() {
        try {
            if (fxLayers == null) {
                fxLayers = ReflectionHelper.getPrivateValue(net.minecraft.client.particle.EffectRenderer.class, mc.effectRenderer, "field_78876_b", "fxLayers");
            }
            if (fxLayers != null && fxLayers.length > 1) {
                List[] layer1 = fxLayers[1];
                if (layer1 != null) {
                    for (List<EntityFX> list : layer1) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            if (list.get(i) instanceof EntityPortalFX) {
                                list.get(i).setDead();
                                list.remove(i);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void spawnRealistcPoralParticles(BlockPos pos, int type) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        double startX = x + (rand.nextGaussian() * 0.4);
        double startY = y + (rand.nextGaussian() * 0.4);
        double startZ = z + (rand.nextGaussian() * 0.4);

        float r = (type == 0) ? 0.6f : 0.05f;
        float g = (type == 0) ? 0.0f : 0.05f;
        float b = (type == 0) ? 0.8f : 0.1f;

        EntityAlivePortalFX particle = new EntityAlivePortalFX(
                mc.theWorld,
                startX, startY, startZ,
                x, y, z,
                r, g, b,
                type
        );

        mc.effectRenderer.addEffect(particle);
    }
}