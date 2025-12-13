package com.vitorxp.WorthClient.mixin;

import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MathHelper.class)
public class MixinMathHelper {

    private static final float[] SIN_TABLE_FAST = new float[65536];

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE_FAST[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }
    }

    @Overwrite
    public static float sin(float value) {
        return SIN_TABLE_FAST[(int) (value * 10430.378F) & 65535];
    }

    @Overwrite
    public static float cos(float value) {
        return SIN_TABLE_FAST[(int) (value * 10430.378F + 16384.0F) & 65535];
    }
}