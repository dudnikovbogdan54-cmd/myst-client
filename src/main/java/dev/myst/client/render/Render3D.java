package dev.myst.client.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Mobile-friendly world geometry. All primitives are emitted as POSITION_COLOR
 * quads, avoiding line vertex formats (Normal/LineWidth) unsupported by some GLES paths.
 */
public final class Render3D {
    private static final float LINE_HALF_WIDTH = 0.006f;

    private Render3D() {}

    private static void vertex(VertexConsumer out, MatrixStack.Entry matrix,
                               float x, float y, float z, int color) {
        out.vertex(matrix, x, y, z).color(
                (color >>> 16) & 255,
                (color >>> 8) & 255,
                color & 255,
                (color >>> 24) & 255
        );
    }

    public static void drawRibbon(VertexConsumer out, MatrixStack.Entry matrix,
                                  float ax, float ay, float az,
                                  float bx, float by, float bz,
                                  float cx, float cy, float cz,
                                  float dx, float dy, float dz,
                                  int startColor, int endColor) {
        if (out == null || matrix == null) return;
        vertex(out, matrix, ax, ay, az, startColor);
        vertex(out, matrix, bx, by, bz, startColor);
        vertex(out, matrix, cx, cy, cz, endColor);
        vertex(out, matrix, dx, dy, dz, endColor);
    }

    public static void drawRibbon(VertexConsumer out, MatrixStack.Entry matrix,
                                  Vec3d a, Vec3d b, Vec3d c, Vec3d d,
                                  int startColor, int endColor) {
        drawRibbon(out, matrix,
                (float)a.x, (float)a.y, (float)a.z,
                (float)b.x, (float)b.y, (float)b.z,
                (float)c.x, (float)c.y, (float)c.z,
                (float)d.x, (float)d.y, (float)d.z,
                startColor, endColor);
    }

    /** A thin camera-independent flat ribbon around a line segment. */
    public static void drawLine(VertexConsumer out, MatrixStack.Entry matrix,
                                float ax, float ay, float az,
                                float bx, float by, float bz, int color) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float sx = dz, sy = 0.0f, sz = -dx;
        float lengthSq = sx * sx + sz * sz;
        if (lengthSq < 1.0E-8f) {
            sx = 1.0f; sz = 0.0f; lengthSq = 1.0f;
        }
        float scale = LINE_HALF_WIDTH / (float)Math.sqrt(lengthSq);
        sx *= scale; sz *= scale;
        drawRibbon(out, matrix,
                ax + sx, ay + sy, az + sz,
                ax - sx, ay - sy, az - sz,
                bx - sx, by - sy, bz - sz,
                bx + sx, by + sy, bz + sz, color, color);
    }

    public static void drawLine(VertexConsumer out, MatrixStack.Entry matrix,
                                Vec3d a, Vec3d b, int color) {
        drawLine(out, matrix, (float)a.x, (float)a.y, (float)a.z,
                (float)b.x, (float)b.y, (float)b.z, color);
    }

    /** Thin annular quads; no per-segment objects are allocated. */
    public static void drawRing(VertexConsumer out, MatrixStack.Entry matrix,
                                float x, float y, float z, float radius,
                                float yOffset, int color, int segments) {
        if (out == null || matrix == null || radius <= 0.0f) return;
        int count = Math.max(8, segments);
        float halfWidth = 0.006f;
        float outer = radius + halfWidth;
        float inner = Math.max(0.0f, radius - halfWidth);
        for (int i = 0; i < count; i++) {
            double a0 = Math.PI * 2.0 * i / count;
            double a1 = Math.PI * 2.0 * (i + 1) / count;
            float ox0 = x + (float)Math.cos(a0) * outer;
            float oz0 = z + (float)Math.sin(a0) * outer;
            float ix0 = x + (float)Math.cos(a0) * inner;
            float iz0 = z + (float)Math.sin(a0) * inner;
            float ox1 = x + (float)Math.cos(a1) * outer;
            float oz1 = z + (float)Math.sin(a1) * outer;
            float ix1 = x + (float)Math.cos(a1) * inner;
            float iz1 = z + (float)Math.sin(a1) * inner;
            float yy = y + yOffset;
            vertex(out, matrix, ox0, yy, oz0, color);
            vertex(out, matrix, ix0, yy, iz0, color);
            vertex(out, matrix, ix1, yy, iz1, color);
            vertex(out, matrix, ox1, yy, oz1, color);
        }
    }

    public static void drawRing(VertexConsumer out, MatrixStack.Entry matrix,
                                Vec3d center, float radius, float yOffset,
                                int color, int segments) {
        drawRing(out, matrix, (float)center.x, (float)center.y, (float)center.z,
                radius, yOffset, color, segments);
    }

    public static void drawDiamond(VertexConsumer out, MatrixStack.Entry matrix,
                                   float x, float y, float z,
                                   float width, float height, int color) {
        float halfW = width * 0.5f, halfH = height * 0.5f;
        drawLine(out, matrix, x, y + halfH, z, x + halfW, y, z, color);
        drawLine(out, matrix, x + halfW, y, z, x, y - halfH, z, color);
        drawLine(out, matrix, x, y - halfH, z, x - halfW, y, z, color);
        drawLine(out, matrix, x - halfW, y, z, x, y + halfH, z, color);
    }

    /** Reject distant points and points behind the camera without temporary vectors. */
    public static boolean isVisible(float x, float y, float z,
                                    double cameraX, double cameraY, double cameraZ,
                                    float yawDegrees, float pitchDegrees, double maxDistance) {
        double dx = x - cameraX, dy = y - cameraY, dz = z - cameraZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        if (distanceSq > maxDistance * maxDistance) return false;
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);
        return dx * fx + dy * fy + dz * fz > 0.15;
    }
}
