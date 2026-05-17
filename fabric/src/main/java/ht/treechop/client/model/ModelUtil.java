package ht.treechop.client.model;

import ht.tuber.math.Vector3;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

public class ModelUtil {

    /**
     * Trims a quad's vertex positions to be within [mins, maxes] and adjusts UVs accordingly.
     * Operates in-place on the emitter (after fromBakedQuad has been called).
     */
    public static void trimQuadInEmitter(QuadEmitter emitter, Vector3 corner1, Vector3 corner2) {
        Vector3 mins = new Vector3(
                Math.min(corner1.x(), corner2.x()),
                Math.min(corner1.y(), corner2.y()),
                Math.min(corner1.z(), corner2.z())
        );
        Vector3 maxes = new Vector3(
                Math.max(corner1.x(), corner2.x()),
                Math.max(corner1.y(), corner2.y()),
                Math.max(corner1.z(), corner2.z())
        );

        // Read all 4 vertices first
        float[] xs = new float[4], ys = new float[4], zs = new float[4];
        float[] us = new float[4], vs = new float[4];
        for (int i = 0; i < 4; i++) {
            xs[i] = emitter.x(i) * 16f;
            ys[i] = emitter.y(i) * 16f;
            zs[i] = emitter.z(i) * 16f;
            us[i] = emitter.u(i);
            vs[i] = emitter.v(i);
        }

        // For each vertex, clamp pos and lerp UVs using the triangle (0,1,2)
        for (int i = 0; i < 4; i++) {
            float newX = (float) clamp(xs[i], mins.x(), maxes.x());
            float newY = (float) clamp(ys[i], mins.y(), maxes.y());
            float newZ = (float) clamp(zs[i], mins.z(), maxes.z());

            Vector3 newPos = new Vector3(newX, newY, newZ);
            Vector3 v0 = new Vector3(xs[0], ys[0], zs[0]);
            Vector3 v1 = new Vector3(xs[1], ys[1], zs[1]);
            Vector3 v2 = new Vector3(xs[2], ys[2], zs[2]);

            Vector3 weights = getLerpWeightsInSimplex(newPos, v0, v1, v2);
            float newU = (float) (weights.x() * us[0] + weights.y() * us[1] + weights.z() * us[2]);
            float newV = (float) (weights.x() * vs[0] + weights.y() * vs[1] + weights.z() * vs[2]);

            emitter.pos(i, newX / 16f, newY / 16f, newZ / 16f);
            emitter.uv(i, newU, newV);
        }
    }

    /**
     * Translates all 4 vertex positions of the quad in the emitter by (dx, dy, dz) in block space.
     * Operates in-place on the emitter (after fromBakedQuad has been called).
     */
    public static void translateQuadInEmitter(QuadEmitter emitter, float dx, float dy, float dz) {
        for (int i = 0; i < 4; i++) {
            emitter.pos(i, emitter.x(i) + dx, emitter.y(i) + dy, emitter.z(i) + dz);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Vector3 getLerpWeightsInSimplex(Vector3 pos, Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 side1 = v2.subtract(v1);
        Vector3 side2 = v3.subtract(v1);
        Vector3 normal = side2.cross(side1).normalize();

        double offset = pos.subtract(v1).dot(normal);
        Vector3 p = pos.subtract(normal.scale(offset));

        Vector3 f1 = v1.subtract(p);
        Vector3 f2 = v2.subtract(p);
        Vector3 f3 = v3.subtract(p);

        Vector3 va = side1.cross(side2);
        Vector3 va1 = f2.cross(f3);
        Vector3 va2 = f3.cross(f1);
        Vector3 va3 = f1.cross(f2);

        double a = va.length();
        if (a == 0) return new Vector3(1.0 / 3, 1.0 / 3, 1.0 / 3);
        double w1 = va1.length() / a * Math.signum(va.dot(va1));
        double w2 = va2.length() / a * Math.signum(va.dot(va2));
        double w3 = va3.length() / a * Math.signum(va.dot(va3));

        return new Vector3(w1, w2, w3);
    }
}

