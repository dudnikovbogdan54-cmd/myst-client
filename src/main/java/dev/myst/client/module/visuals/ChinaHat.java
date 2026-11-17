package dev.myst.client.module.visuals;

import dev.myst.client.render.MathUtil;
import dev.myst.client.render.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

/** Floating neon halo and wireframe cone above the local player in third person. */
public final class ChinaHat {
    private static boolean initialized;
    private static boolean enabled;
    private static long ticks;

    public static void setEnabled(boolean value) { enabled = value; }
    private ChinaHat() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        WorldRenderEvents.AFTER_ENTITIES.register(ChinaHat::render);
    }

    private static void render(WorldRenderContext context) {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || client.options.getPerspective().isFirstPerson()) return;

        var camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        float hx = (float)(client.player.getX() - cameraPos.x);
        float hy = (float)(client.player.getY() + client.player.getHeight() + 0.22 - cameraPos.y);
        float hz = (float)(client.player.getZ() - cameraPos.z);
        if (!Render3D.isVisible((float)client.player.getX(),
                (float)(client.player.getY() + client.player.getHeight() + 0.22),
                (float)client.player.getZ(), cameraPos.x, cameraPos.y, cameraPos.z,
                camera.getYaw(), camera.getPitch(), 48.0)) return;

        ticks++;
        VertexConsumer quads = context.consumers().getBuffer(RenderLayers.debugQuads());
        var matrix = context.matrices().peek();
        float pulse = 0.85f + (float)Math.sin(ticks * 0.08) * 0.15f;
        int neon = MathUtil.withAlpha(0xFFB56CFF, pulse);
        int cyan = MathUtil.withAlpha(0xFF55E7FF, 0.58f * pulse);
        float radius = 0.68f;
        Render3D.drawRing(quads, matrix, hx, hy, hz, radius, 0, neon, 32);
        Render3D.drawRing(quads, matrix, hx, hy, hz, radius * 0.78f, 0.035f, cyan, 32);

        float apexY = hy + 0.42f;
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2.0 * i / 32.0;
            float ex = hx + (float)Math.cos(angle) * radius;
            float ez = hz + (float)Math.sin(angle) * radius;
            Render3D.drawLine(quads, matrix, ex, hy + 0.015f, ez,
                    hx, apexY, hz, MathUtil.withAlpha(neon, 0.76f));
        }
    }
}
