 package com.vitorxp.WorthClient.utils.math;
 
 public class Vector4f
 {
   private float x;
   private float y;
   private float z;
   private float w;

   public boolean equals(Object object) {
     if (this == object)
       return true;
     if (object == null || getClass() != object.getClass())
       return false;
     Vector4f vector4f = (Vector4f)object;
     if (Float.compare(vector4f.x, this.x) != 0)
       return false;
     if (Float.compare(vector4f.y, this.y) != 0)
       return false;
     if (Float.compare(vector4f.z, this.z) != 0)
       return false;
     return (Float.compare(vector4f.w, this.w) == 0);
   }
   
   public int hashCode() {
     int i = Float.floatToIntBits(this.x);
     i = 31 * i + Float.floatToIntBits(this.y);
     i = 31 * i + Float.floatToIntBits(this.z);
     i = 31 * i + Float.floatToIntBits(this.w);
     return i;
   }
   
   public float x() {
     return this.x;
   }
   public float y() {
     return this.y;
   }
   public float z() {
     return this.z;
   }
   public float w() {
     return this.w;
   }

   public void set(float f, float g, float h, float i) {
     this.x = f;
     this.y = g;
     this.z = h;
     this.w = i;
   }
   
   public void add(float f, float g, float h, float i) {
     this.x += f;
     this.y += g;
     this.z += h;
     this.w += i;
   }

   public String toString() {
     return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
   }
 }