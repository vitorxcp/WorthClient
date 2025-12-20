package com.vitorxp.WorthClient.utils.math;

import net.minecraft.util.MathHelper;

public final class Vector3f {
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);

    private float x;
    private float y;
    private float z;

    public Vector3f() {}

    public Vector3f(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Vector3f vector3f = (Vector3f) object;
        if (Float.compare(vector3f.x, this.x) != 0) return false;
        if (Float.compare(vector3f.y, this.y) != 0) return false;
        return (Float.compare(vector3f.z, this.z) == 0);
    }

    public int hashCode() {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        i = 31 * i + Float.floatToIntBits(this.z);
        return i;
    }

    public float x() { return this.x; }
    public float y() { return this.y; }
    public float z() { return this.z; }

    public void set(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public void load(Vector3f vector3f) {
        this.x = vector3f.x;
        this.y = vector3f.y;
        this.z = vector3f.z;
    }

    public void add(float f, float g, float h) {
        this.x += f;
        this.y += g;
        this.z += h;
    }

    public void add(Vector3f vector3f) {
        this.x += vector3f.x;
        this.y += vector3f.y;
        this.z += vector3f.z;
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}