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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Double neon ground rings that expand and fade after each player jump. */
public final class JumpCircles {
    private record Pulse(Vec3d position, int age) {}
    private static final List<Pulse> PULSES = new ArrayList<>();
    private static boolean initialized; private static boolean enabled;
    public static void setEnabled(boolean value) { enabled = value; }
    private static boolean wasGrounded;

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
                PULSES.add(new Pulse(new Vec3d(client.player.getX(), client.player.getY() + 0.025,
                        client.player.getZ()), 0));
            }
            wasGrounded = grounded;
            for (int i = 0; i < PULSES.size(); i++) {
                Pulse p = PULSES.get(i);
                PULSES.set(i, new Pulse(p.position(), p.age() + 1));
            }
            PULSES.removeIf(p -> p.age() > 28);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(JumpCircles::render);
    }

    private static void render(WorldRenderContext context) {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || PULSES.isEmpty()) return;
        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayers.lines());
        for (Pulse pulse : PULSES) {
            float t = pulse.age() / 28.0f;
            float radius = 0.22f + MathUtil.easeOutCubic(t) * 1.05f;
            float alpha = 1.0f - t;
            Vec3d center = pulse.position().subtract(camera);
            int outer = MathUtil.multiplyAlpha(MathUtil.gradient(t, 0xFF59E8FF, 0xFF9B69FF), alpha);
            int inner = MathUtil.multiplyAlpha(MathUtil.gradient(t, 0xFFB58AFF, 0xFF58F2D2), alpha);
            Render3D.drawRing(lines, context.matrices().peek(), center, radius, 0.006f, outer, 48);
            Render3D.drawRing(lines, context.matrices().peek(), center, radius * 0.68f, 0.012f, inner, 40);
        }
    }
}
