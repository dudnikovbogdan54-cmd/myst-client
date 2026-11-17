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
    private static long ticks;
    private ChinaHat() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        WorldRenderEvents.AFTER_ENTITIES.register(ChinaHat::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || client.options.getPerspective().isFirstPerson()) return;
        ticks++;
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        Vec3d head = new Vec3d(client.player.getX(),
                client.player.getY() + client.player.getHeight() + 0.22, client.player.getZ()).subtract(camera);
        VertexConsumer lines = context.consumers().getBuffer(RenderLayers.lines());
        float pulse = 0.85f + (float)Math.sin(ticks * 0.08) * 0.15f;
        int neon = MathUtil.withAlpha(0xFFB56CFF, pulse);
        int cyan = MathUtil.withAlpha(0xFF55E7FF, 0.58f * pulse);
        float radius = 0.68f;
        Render3D.drawRing(lines, context.matrices().peek(), head, radius, 0, neon, 64);
        Render3D.drawRing(lines, context.matrices().peek(), head, radius * 0.78f, 0.035f, cyan, 48);
        Vec3d apex = head.add(0, 0.42, 0);
        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2 * i / 32.0;
            Vec3d edge = head.add(Math.cos(angle) * radius, 0.015, Math.sin(angle) * radius);
            Render3D.drawLine(lines, context.matrices().peek(), edge, apex,
                    MathUtil.withAlpha(neon, 0.76f));
        }
    }
}
