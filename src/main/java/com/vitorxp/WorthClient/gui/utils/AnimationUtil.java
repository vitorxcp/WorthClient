package com.vitorxp.WorthClient.gui.utils;

public class AnimationUtil {

    public static float easeOutCubic(float t) {
        return (float) (1.0 - Math.pow(1.0 - t, 3.0));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static float easeOutBack(float t) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1.0f;
        return (float) (1.0f + c3 * Math.pow(t - 1.0f, 3.0f) + c1 * Math.pow(t - 1.0f, 2.0f));
    }

}