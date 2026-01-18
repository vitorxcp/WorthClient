package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.config.AnimationsConfig;
import com.vitorxp.WorthClient.utils.CapeSimulationHolder;
import com.vitorxp.WorthClient.utils.StickSimulation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    private float currentEyeHeight = 1.62F;

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

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onUpdateSneak(CallbackInfo ci) {
        if (AnimationsConfig.enabled && AnimationsConfig.oldSneak && this.worldObj.isRemote) {

            float targetHeight = this.isSneaking() ? 1.54F : 1.62F;

            if (this.isPlayerSleeping()) {
                targetHeight = 0.2F;
            }

            if (this.currentEyeHeight != targetHeight) {
                this.currentEyeHeight += (targetHeight - this.currentEyeHeight) * 0.5F;

                if (Math.abs(this.currentEyeHeight - targetHeight) < 0.001F) {
                    this.currentEyeHeight = targetHeight;
                }
            }
        } else {
            this.currentEyeHeight = this.isSneaking() ? 1.54F : 1.62F;
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    public void getSmoothEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (AnimationsConfig.enabled && AnimationsConfig.oldSneak && this.worldObj.isRemote) {
            cir.setReturnValue(this.currentEyeHeight);
        }
    }
}