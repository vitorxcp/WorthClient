package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(
            method = {"updateCameraAndRender", "func_78480_b"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDX()I"),
            require = 0
    )
    public int getDX() {
        return Mouse.getDX();
    }

    @Redirect(
            method = {"updateCameraAndRender", "func_78480_b"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDY()I"),
            require = 0
    )
    public int getDY() {
        return Mouse.getDY();
    }
}