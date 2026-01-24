package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    @Shadow
    public int deltaX;
    @Shadow
    public int deltaY;

    private float smoothCamFilterX;
    private float smoothCamFilterY;

    /**
     * @author vitorxp
     * @reason Move a câmera do Perspective
     */
    @Inject(method = "mouseXYChange", at = @At("RETURN"))
    public void onMouseXYChange(CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled) {
            Minecraft mc = Minecraft.getMinecraft();

            float sensitivity = mc.gameSettings.mouseSensitivity * 0.6F;
            float multiplier = sensitivity * sensitivity * sensitivity * 8.0F;

            float moveX = (float) this.deltaX * multiplier;
            float moveY = (float) this.deltaY * multiplier;

            if (mc.gameSettings.smoothCamera) {
                float smoothFactor = multiplier * 0.5F;
                float smoothX = (float) this.deltaX * smoothFactor;
                float smoothY = (float) this.deltaY * smoothFactor;
                this.smoothCamFilterX += smoothX;
                this.smoothCamFilterY += smoothY;
                float dampX = this.smoothCamFilterX * 0.5F;
                float dampY = this.smoothCamFilterY * 0.5F;
                this.smoothCamFilterX -= dampX;
                this.smoothCamFilterY -= dampY;
                moveX = dampX;
                moveY = dampY;
            } else {
                this.smoothCamFilterX = 0.0F;
                this.smoothCamFilterY = 0.0F;
            }

            if (mc.gameSettings.invertMouse) {
                moveY = -moveY;
            }

            PerspectiveMod.cameraYaw += moveX;
            PerspectiveMod.cameraPitch += moveY;
            if (PerspectiveMod.cameraPitch > 90.0F) PerspectiveMod.cameraPitch = 90.0F;
            if (PerspectiveMod.cameraPitch < -90.0F) PerspectiveMod.cameraPitch = -90.0F;
            this.deltaX = 0;
            this.deltaY = 0;
        }
    }
}