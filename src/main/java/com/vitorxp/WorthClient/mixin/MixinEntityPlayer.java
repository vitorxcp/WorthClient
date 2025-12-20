package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.CapeSimulationHolder;
import com.vitorxp.WorthClient.utils.StickSimulation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }
    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onUpdate(CallbackInfo ci) {
        if (this instanceof CapeSimulationHolder) {
            CapeSimulationHolder holder = (CapeSimulationHolder) this;
            StickSimulation simulation = holder.getSimulation();
            if (simulation != null && !simulation.points.isEmpty()) {
                double radYaw = Math.toRadians(this.renderYawOffset);
                double xOffset = -Math.sin(radYaw) * 0.15;
                double zOffset = Math.cos(radYaw) * 0.15;
                StickSimulation.Point anchor = simulation.points.get(0);
                anchor.position.x = (float) (this.posX + xOffset);
                anchor.position.y = (float) (this.posY + (this.isSneaking() ? 1.25 : 1.55));
                anchor.position.z = (float) (this.posZ + zOffset);

                simulation.simulate();
            }
        }
    }
}