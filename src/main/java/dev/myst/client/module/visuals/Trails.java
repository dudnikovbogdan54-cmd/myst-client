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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Smooth, fading player ribbon. It is intentionally hidden in first person. */
public final class Trails {
    private static final Deque<Vec3d> HISTORY = new ArrayDeque<>();
    private static final int MAX_POINTS = 26;
    private static boolean initialized; private static boolean enabled;
    public static void setEnabled(boolean value) { enabled = value; if (!value) HISTORY.clear(); }

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
        List<Vec3d> points = new ArrayList<>(HISTORY);
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        VertexConsumer quads = context.consumers().getBuffer(RenderLayers.debugQuads());
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d a = points.get(i);
            Vec3d b = points.get(i + 1);
            Vec3d direction = b.subtract(a);
            Vec3d side = new Vec3d(-direction.z, 0, direction.x);
            if (side.lengthSquared() < 1.0E-6) continue;
            side = side.normalize().multiply(0.075);
            float t0 = i / (float)(points.size() - 1);
            float t1 = (i + 1) / (float)(points.size() - 1);
            int c0 = MathUtil.multiplyAlpha(MathUtil.gradient(t0, 0xFFB58AFF, 0xFF55E6FF), 0.08f + t0 * 0.62f);
            int c1 = MathUtil.multiplyAlpha(MathUtil.gradient(t1, 0xFFB58AFF, 0xFF55E6FF), 0.08f + t1 * 0.62f);
            Render3D.drawRibbon(quads, context.matrices().peek(),
                    a.add(side).subtract(camera), a.subtract(side).subtract(camera),
                    b.add(side).subtract(camera), b.subtract(side).subtract(camera), c0, c1);
        }
    }
}
