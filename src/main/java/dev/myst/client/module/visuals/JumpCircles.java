package dev.myst.client.module.visuals;

import dev.myst.client.render.MathUtil;
import dev.myst.client.render.Render3D;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Double neon ground rings that expand and fade after each player jump. */
public final class JumpCircles {
    private record Pulse(Vec3d position, int age) {}
    private static final List<Pulse> PULSES = new ArrayList<>();
    private static boolean initialized;
    private static boolean enabled;
    private static boolean wasGrounded;

    public static void setEnabled(boolean value) { enabled = value; }
    private JumpCircles() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                wasGrounded = false;
                PULSES.clear();
                return;
            }
            boolean grounded = client.player.isOnGround();
            if (wasGrounded && !grounded && client.player.getVelocity().y > 0.0) {
                PULSES.add(new Pulse(new Vec3d(client.player.getX(),
                        client.player.getY() + 0.025, client.player.getZ()), 0));
            }
            wasGrounded = grounded;
            for (int i = 0; i < PULSES.size(); i++) {
                Pulse pulse = PULSES.get(i);
                PULSES.set(i, new Pulse(pulse.position(), pulse.age() + 1));
            }
            PULSES.removeIf(pulse -> pulse.age() > 28);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(JumpCircles::render);
    }

    private static void render(WorldRenderContext context) {
        if (!enabled || PULSES.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        var camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        VertexConsumer quads = context.consumers().getBuffer(
                net.minecraft.client.render.RenderLayers.debugQuads());
        var matrix = context.matrices().peek();

        for (Pulse pulse : PULSES) {
            Vec3d position = pulse.position();
            if (!Render3D.isVisible((float)position.x, (float)position.y, (float)position.z,
                    cameraPos.x, cameraPos.y, cameraPos.z,
                    camera.getYaw(), camera.getPitch(), 48.0)) continue;

            float t = pulse.age() / 28.0f;
            float radius = 0.22f + MathUtil.easeOutCubic(t) * 1.05f;
            float alpha = 1.0f - t;
            float x = (float)(position.x - cameraPos.x);
            float y = (float)(position.y - cameraPos.y);
            float z = (float)(position.z - cameraPos.z);
            int outer = MathUtil.multiplyAlpha(
                    MathUtil.gradient(t, 0xFF59E8FF, 0xFF9B69FF), alpha);
            int inner = MathUtil.multiplyAlpha(
                    MathUtil.gradient(t, 0xFFB58AFF, 0xFF58F2D2), alpha);
            Render3D.drawRing(quads, matrix, x, y, z, radius, 0.006f, outer, 32);
            Render3D.drawRing(quads, matrix, x, y, z, radius * 0.68f, 0.012f, inner, 32);
        }
    }
}
