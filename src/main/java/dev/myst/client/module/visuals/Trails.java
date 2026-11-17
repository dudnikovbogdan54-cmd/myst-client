package dev.myst.client.module.visuals;

import dev.myst.client.render.MathUtil;
import dev.myst.client.render.Render3D;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/** Smooth, fading player ribbon. It is intentionally hidden in first person. */
public final class Trails {
    private static final Deque<Vec3d> HISTORY = new ArrayDeque<>();
    private static final int MAX_POINTS = 26;
    private static boolean initialized;
    private static boolean enabled;

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) HISTORY.clear();
    }

    private Trails() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                HISTORY.clear();
                return;
            }
            Vec3d point = new Vec3d(client.player.getX(), client.player.getY() + 0.08,
                    client.player.getZ());
            if (HISTORY.isEmpty() || HISTORY.peekLast().squaredDistanceTo(point) > 0.0025) {
                HISTORY.addLast(point);
                while (HISTORY.size() > MAX_POINTS) HISTORY.removeFirst();
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(Trails::render);
    }

    public static void clear() {
        HISTORY.clear();
    }

    private static void render(WorldRenderContext context) {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || HISTORY.size() < 2
                || client.options.getPerspective().isFirstPerson()) return;

        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();
        VertexConsumer quads = context.consumers().getBuffer(RenderLayers.debugQuads());
        var matrix = context.matrices().peek();
        Iterator<Vec3d> iterator = HISTORY.iterator();
        Vec3d previous = iterator.next();
        int index = 0;
        int segmentCount = HISTORY.size() - 1;

        while (iterator.hasNext()) {
            Vec3d current = iterator.next();
            double dx = current.x - previous.x;
            double dz = current.z - previous.z;
            double sideLengthSq = dx * dx + dz * dz;
            if (sideLengthSq >= 1.0E-6) {
                double scale = 0.075 / Math.sqrt(sideLengthSq);
                float sx = (float)(-dz * scale);
                float sz = (float)(dx * scale);
                float t0 = index / (float)segmentCount;
                float t1 = (index + 1) / (float)segmentCount;
                int c0 = MathUtil.multiplyAlpha(
                        MathUtil.gradient(t0, 0xFFB58AFF, 0xFF55E6FF), 0.08f + t0 * 0.62f);
                int c1 = MathUtil.multiplyAlpha(
                        MathUtil.gradient(t1, 0xFFB58AFF, 0xFF55E6FF), 0.08f + t1 * 0.62f);

                float ax = (float)(previous.x + sx - camera.x);
                float ay = (float)(previous.y - camera.y);
                float az = (float)(previous.z + sz - camera.z);
                float bx = (float)(previous.x - sx - camera.x);
                float bz = (float)(previous.z - sz - camera.z);
                float cx = (float)(current.x - sx - camera.x);
                float cy = (float)(current.y - camera.y);
                float cz = (float)(current.z - sz - camera.z);
                float ex = (float)(current.x + sx - camera.x);
                float ez = (float)(current.z + sz - camera.z);
                Render3D.drawRibbon(quads, matrix, ax, ay, az, bx, ay, bz,
                        cx, cy, cz, ex, cy, ez, c0, c1);
            }
            previous = current;
            index++;
        }
    }
}
