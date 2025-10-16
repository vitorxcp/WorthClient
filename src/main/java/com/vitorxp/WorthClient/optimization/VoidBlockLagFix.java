package com.vitorxp.WorthClient.optimization;

import com.vitorxp.WorthClient.config.VoidLagFixConfig;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;

public class VoidBlockLagFix {

    private static final Field notifiedSidesField = ReflectionHelper.findField(BlockEvent.NeighborNotifyEvent.class, "notifiedSides");

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {

        if (!VoidLagFixConfig.enabled || event.world.isRemote || event.pos.getY() < VoidLagFixConfig.activationHeight) {
            return;
        }

        final int currentDimensionId = event.world.provider.getDimensionId();
        boolean isDimensionWhitelisted = Arrays.stream(VoidLagFixConfig.dimensionWhitelist)
                .anyMatch(id -> id == currentDimensionId);
        if (!isDimensionWhitelisted) {
            return;
        }

        if (event.world.isAirBlock(event.pos.down())) {
            try {
                EnumSet<EnumFacing> notifiedSides = (EnumSet<EnumFacing>) notifiedSidesField.get(event);
                notifiedSides.remove(EnumFacing.DOWN);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}