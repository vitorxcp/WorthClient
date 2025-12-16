package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    @Shadow public int deltaX;
    @Shadow public int deltaY;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void mouseXYChange() {
        this.deltaX = Mouse.getDX();
        this.deltaY = Mouse.getDY();

        if (PerspectiveMod.perspectiveToggled) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen != null) return;

            GameSettings settings = mc.gameSettings;
            float f = settings.mouseSensitivity * 0.6F + 0.2F;
            float sensitivity = f * f * f * 8.0F;

            float dx = (float)this.deltaX * sensitivity;
            float dy = (float)this.deltaY * sensitivity;

            if (settings.invertMouse) dy = -dy;

            PerspectiveMod.cameraYaw += dx * 0.15F;
            PerspectiveMod.cameraPitch -= dy * 0.15F;

            if (PerspectiveMod.cameraPitch > 90.0F) PerspectiveMod.cameraPitch = 90.0F;
            if (PerspectiveMod.cameraPitch < -90.0F) PerspectiveMod.cameraPitch = -90.0F;

            this.deltaX = 0;
            this.deltaY = 0;
        }
    }
}