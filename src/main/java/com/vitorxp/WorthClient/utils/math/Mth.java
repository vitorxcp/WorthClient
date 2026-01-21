package com.vitorxp.WorthClient.utils.math;

public class Mth {
    private static final int SIN_BITS = 14;
    private static final int SIN_MASK = ~(-1 << SIN_BITS);
    private static final int SIN_COUNT = SIN_MASK + 1;
    private static final float radFull = (float) (Math.PI * 2.0);
    private static final float degFull = 360.0f;
    private static final float radToIndex = SIN_COUNT / radFull;
    private static final float degToIndex = SIN_COUNT / degFull;
    private static final float[] table = new float[SIN_COUNT];

    static {
        for (int i = 0; i < SIN_COUNT; i++) {
            table[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
        }
        for (int i = 0; i < 360; i += 90) {
            table[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
        }
    }

    public static float sin(float radians) {
        return table[(int) (radians * radToIndex) & SIN_MASK];
    }

    public static float cos(float radians) {
        return table[(int) ((radians + ((float) Math.PI / 2)) * radToIndex) & SIN_MASK];
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqrt(double value) {
        return (float) Math.sqrt(value);
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > (double) i ? i + 1 : i;
    }

    public static int clamp_int(int num, int min, int max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    public static float clamp_float(float num, float min, float max) {
        return (num < min) ? min : (num > max) ? max : num;
    }

    public static float atan2(float y, float x) {
        float n1 = x;
        float n2 = y;
        if (n1 == 0.0f) return (n2 > 0.0f ? (float) Math.PI / 2 : (n2 == 0.0f ? 0.0f : (float) -Math.PI / 2));
        float ax = Math.abs(n1), ay = Math.abs(n2);
        float a = Math.min(ax, ay) / Math.max(ax, ay);
        float s = a * a;
        float r = ((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a;
        if (ay > ax) r = (float) Math.PI / 2 - r;
        if (n1 < 0.0f) r = (float) Math.PI - r;
        return (n2 < 0.0f ? -r : r);
    }

    public static double atan2(double y, double x) {
        return atan2((float) y, (float) x);
    }

    public static double atan2_double(double y, double x) {
        return atan2((float) y, (float) x);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}