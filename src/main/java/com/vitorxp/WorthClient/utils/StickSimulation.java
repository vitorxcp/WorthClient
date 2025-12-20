package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.utils.math.Mth;
import java.util.ArrayList;
import java.util.List;

public class StickSimulation {
    public List<Point> points = new ArrayList<>();
    public List<Stick> sticks = new ArrayList<>();
    public float gravity = 25.0F;
    public int numIterations = 20;
    private float maxBend = 5.0F;
    public void simulate() {
        if (points.isEmpty()) return;
        if (sticks.isEmpty() && points.size() > 1) {
            for (int i = 0; i < points.size() - 1; i++) {
                sticks.add(new Stick(points.get(i), points.get(i + 1), 1.0F / 16.0F));
            }
        }
        float deltaTime = 0.05F;
        Vector3 down = new Vector3(0.0F, -this.gravity * deltaTime, 0.0F);
        Vector3 tmp = new Vector3(0,0,0);
        for (Point p : this.points) {
            if (!p.locked) {
                tmp.copy(p.position);
                float velX = (p.position.x - p.prevPosition.x) * 0.99F;
                float velY = (p.position.y - p.prevPosition.y) * 0.99F;
                float velZ = (p.position.z - p.prevPosition.z) * 0.99F;
                p.prevPosition.copy(tmp);
                p.position.x += velX;
                p.position.y += velY + down.y;
                p.position.z += velZ;
            }
        }
        for (int k = 0; k < 1; k++) {
            for (int i = points.size() - 2; i >= 1; i--) {
                Point current = points.get(i);
                Point prev = points.get(i - 1);
                Point next = points.get(i + 1);
                double angle = getAngle(current.position, prev.position, next.position);
                angle *= 57.2958D;
                if (angle > 360.0D) angle -= 360.0D;
                if (angle < -360.0D) angle += 360.0D;
                double absAngle = Math.abs(angle);
                if (absAngle < (180.0F - this.maxBend)) {
                    applyReplacement(current, prev, next, angle, (180.0F - this.maxBend + 1.0F));
                } else if (absAngle > (180.0F + this.maxBend)) {
                    applyReplacement(current, prev, next, angle, (180.0F + this.maxBend - 1.0F));
                }
            }
        }
        for (int i = 0; i < this.numIterations; i++) {
            for (Stick stick : this.sticks) {
                Vector3 center = stick.pointA.position.clone().add(stick.pointB.position).div(2.0F);
                Vector3 dir = stick.pointA.position.clone().subtract(stick.pointB.position).normalize();
                if (Float.isNaN(dir.x)) dir = new Vector3(0, -1, 0);
                if (!stick.pointA.locked) {
                    stick.pointA.position = center.clone().add(dir.clone().mul(stick.length / 2.0F));
                }
                if (!stick.pointB.locked) {
                    stick.pointB.position = center.clone().subtract(dir.clone().mul(stick.length / 2.0F));
                }
            }
        }
    }
    private void applyReplacement(Point middle, Point prev, Point next, double angle, double target) {
        double theta = target / 57.2958D;
        float z = prev.position.z - middle.position.z;
        float y = prev.position.y - middle.position.y;
        if (angle < 0.0D) theta *= -1.0D;
        double cs = Math.cos(theta);
        double sn = Math.sin(theta);
        next.position.z = (float)(z * cs - y * sn + middle.position.z);
        next.position.y = (float)(z * sn + y * cs + middle.position.y);
    }
    private double getAngle(Vector3 middle, Vector3 prev, Vector3 next) {
        return Math.atan2((next.y - middle.y), (next.z - middle.z)) -
                Math.atan2((prev.y - middle.y), (prev.z - middle.z));
    }
    public static class Point {
        public Vector3 position = new Vector3(0,0,0);
        public Vector3 prevPosition = new Vector3(0,0,0);
        public boolean locked;
        public float getLerpX(float delta) { return Mth.lerp(delta, this.prevPosition.x, this.position.x); }
        public float getLerpY(float delta) { return Mth.lerp(delta, this.prevPosition.y, this.position.y); }
        public float getLerpZ(float delta) { return Mth.lerp(delta, this.prevPosition.z, this.position.z); }
    }
    public static class Stick {
        public Point pointA, pointB;
        public float length;
        public Stick(Point pointA, Point pointB, float length) {
            this.pointA = pointA; this.pointB = pointB; this.length = length;
        }
    }
    public static class Vector3 {
        public float x, y, z;
        public Vector3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
        public Vector3 clone() { return new Vector3(x, y, z); }
        public void copy(Vector3 v) { this.x = v.x; this.y = v.y; this.z = v.z; }
        public Vector3 subtract(Vector3 v) { x -= v.x; y -= v.y; z -= v.z; return this; }
        public Vector3 add(Vector3 v) { x += v.x; y += v.y; z += v.z; return this; }
        public Vector3 div(float v) { x /= v; y /= v; z /= v; return this; }
        public Vector3 mul(float v) { x *= v; y *= v; z *= v; return this; }
        public Vector3 normalize() {
            float f = (float)Math.sqrt(x*x + y*y + z*z);
            if (f < 1E-4F) return new Vector3(0,0,0);
            x/=f; y/=f; z/=f; return this;
        }
    }
}