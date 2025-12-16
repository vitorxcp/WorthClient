package com.vitorxp.WorthClient.mixin;

import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MathHelper.class)
public class MixinMathHelper {

    /**
     * @author vitorxp
     * @reason Substitui a tabela lenta do Minecraft por instruções nativas da CPU.
     * Melhora a performance matemática sem quebrar a direção (WASD).
     */
    @Overwrite
    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    @Overwrite
    public static float cos(float value) {
        return (float) Math.cos(value);
    }
}