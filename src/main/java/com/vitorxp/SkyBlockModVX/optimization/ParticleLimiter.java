package com.vitorxp.SkyBlockModVX.optimization;

import com.vitorxp.SkyBlockModVX.config.PerfConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.util.List;

public class ParticleLimiter {

    private static int spawnedThisTick = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            spawnedThisTick = 0;
        }
    }

    public static void trySwapEffectRenderer() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (!(mc.effectRenderer instanceof LimitedEffectRenderer)) {
                mc.effectRenderer = new LimitedEffectRenderer(mc, mc.effectRenderer);
            }
        } catch (Throwable ignored) {}
    }

    public static class LimitedEffectRenderer extends EffectRenderer {

        private final EffectRenderer delegate;
        private final Minecraft mc = Minecraft.getMinecraft();

        public LimitedEffectRenderer(Minecraft mc, EffectRenderer delegate) {
            super(mc.theWorld, mc.getTextureManager());
            this.delegate = delegate;
        }

        @Override
        public void addEffect(EntityFX effect) {
            try {
                if (effect instanceof EntityDiggingFX) {
                    // Sempre permitir partículas de bloco
                    delegate.addEffect(effect);
                    return;
                }

                String name = effect.getClass().getSimpleName();
                if (name.contains("Critical") || name.contains("Hit")) {
                    delegate.addEffect(effect);
                    return;
                }

                int alive = countAlive(delegate);
                if (alive >= PerfConfig.particleCap) return;
                if (spawnedThisTick >= PerfConfig.particleBurstCap) return;

                spawnedThisTick++;
                delegate.addEffect(effect);
            } catch (Throwable t) {
                delegate.addEffect(effect);
            }
        }

        @Override
        public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {
            try {
                state.getBlock().addDestroyEffects(mc.theWorld, pos, delegate);
            } catch (Throwable ignored) {}
        }

        @Override
        public void addBlockHitEffects(BlockPos pos, EnumFacing side) {
            try {
                delegate.addBlockHitEffects(pos, side);
            } catch (Throwable ignored) {}
        }

        @SuppressWarnings("unchecked")
        private int countAlive(EffectRenderer er) throws Exception {
            int total = 0;
            for (Field field : EffectRenderer.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object list = field.get(er);
                    if (list instanceof List) total += ((List<?>) list).size();
                }
            }
            return total;
        }
    }
}
