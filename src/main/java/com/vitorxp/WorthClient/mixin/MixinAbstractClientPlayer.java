package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.CapeSimulationHolder;
import com.vitorxp.WorthClient.utils.StickSimulation;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer implements CapeSimulationHolder {
    public MixinAbstractClientPlayer(World worldIn, com.mojang.authlib.GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }
    @Unique
    private final StickSimulation simulation = new StickSimulation();
    @Override
    public StickSimulation getSimulation() {
        return this.simulation;
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(World worldIn, com.mojang.authlib.GameProfile playerProfile, CallbackInfo ci) {
        if (this.simulation.points.isEmpty()) {
            for (int i = 0; i < 16; i++) {
                this.simulation.points.add(new StickSimulation.Point());
            }
        }
    }
}