package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.math.Mth;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MathHelper.class)
public class MixinMathHelper {

    /**
     * @author VitorXP
     * @reason Fast Math: Sin (Otimização de Renderização)
     */
    @Overwrite
    public static float sin(float value) {
        return Mth.sin(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Cos (Otimização de Renderização)
     */
    @Overwrite
    public static float cos(float value) {
        return Mth.cos(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Sqrt (Direto para hardware)
     */
    @Overwrite
    public static float sqrt_float(float value) {
        return Mth.sqrt(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Sqrt (Direto para hardware)
     */
    @Overwrite
    public static float sqrt_double(double value) {
        return Mth.sqrt((float)value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Floor (Acelera cálculo de colisão e chunks)
     */
    @Overwrite
    public static int floor_float(float value) {
        return Mth.floor(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Floor (Acelera cálculo de colisão e chunks)
     */
    @Overwrite
    public static int floor_double(double value) {
        return Mth.floor(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Ceiling
     */
    @Overwrite
    public static int ceiling_float_int(float value) {
        return Mth.ceil(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Ceiling
     */
    @Overwrite
    public static int ceiling_double_int(double value) {
        return Mth.ceil(value);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Clamp Int (Acelera iluminação)
     */
    @Overwrite
    public static int clamp_int(int num, int min, int max) {
        return Mth.clamp_int(num, min, max);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Clamp Float (Acelera ângulos)
     */
    @Overwrite
    public static float clamp_float(float num, float min, float max) {
        return Mth.clamp_float(num, min, max);
    }

    /**
     * @author VitorXP
     * @reason Fast Math: Atan2 (Acelera rotação e IA)
     */
    @Overwrite
    public static double atan2(double y, double x) {
        return Mth.atan2_double(y, x);
    }
}