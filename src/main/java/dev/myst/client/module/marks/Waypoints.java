package dev.myst.client.module.marks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.myst.client.render.MathUtil;
import dev.myst.client.render.Render2D;
import dev.myst.client.render.Render3D;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Persistent 3D waypoints with a depth-aware screen-space label renderer. */
public final class Waypoints {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mystclient_waypoints.json");
    private static final List<Waypoint> POINTS = new ArrayList<>();
    private static boolean initialized;
    private static long animationClock;

    private Waypoints() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("waypoint")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            MinecraftClient client = MinecraftClient.getInstance();
                                            if (client.player == null || client.world == null) {
                                                context.getSource().sendError(Text.literal("Войди в мир, чтобы создать метку."));
                                                return 0;
                                            }
                                            String name = StringArgumentType.getString(context, "name").trim();
                                            if (name.isEmpty()) {
                                                context.getSource().sendError(Text.literal("Укажи название метки."));
                                                return 0;
                                            }
                                            Waypoint point = new Waypoint(name, client.player.getX(), client.player.getY(),
                                                    client.player.getZ(),
                                                    client.world.getRegistryKey().getValue().toString(),
                                                    0xFFB58AFF, true);
                                            POINTS.removeIf(p -> p.name.equalsIgnoreCase(name)
                                                    && safe(p.dimension).equals(point.dimension));
                                            POINTS.add(point);
                                            save();
                                            context.getSource().sendFeedback(Text.literal("Метка «" + name + "» создана."));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name").trim();
                                            boolean removed = POINTS.removeIf(p -> p.name.equalsIgnoreCase(name));
                                            if (removed) {
                                                save();
                                                context.getSource().sendFeedback(Text.literal("Метка «" + name + "» удалена."));
                                                return 1;
                                            }
                                            context.getSource().sendError(Text.literal("Метка «" + name + "» не найдена."));
                                            return 0;
                                        })))));
        WorldRenderEvents.AFTER_ENTITIES.register(Waypoints::renderWorld);
        HudRenderCallback.EVENT.register(Waypoints::renderLabels);
    }

    public static List<Waypoint> getAll() { return Collections.unmodifiableList(POINTS); }

    public static void add(Waypoint waypoint) {
        if (waypoint == null || waypoint.name == null || waypoint.name.isBlank()) return;
        POINTS.removeIf(p -> p.name.equalsIgnoreCase(waypoint.name)
                && safe(p.dimension).equals(safe(waypoint.dimension)));
        POINTS.add(waypoint);
        save();
    }

    public static boolean remove(String name) {
        boolean changed = POINTS.removeIf(p -> p.name.equalsIgnoreCase(name));
        if (changed) save();
        return changed;
    }

    public static void setEnabled(String name, boolean enabled) {
        for (Waypoint point : POINTS) if (point.name.equalsIgnoreCase(name)) point.enabled = enabled;
        save();
    }

    private static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try (var reader = Files.newBufferedReader(FILE)) {
            Waypoint[] loaded = GSON.fromJson(reader, Waypoint[].class);
            POINTS.clear();
            if (loaded != null) for (Waypoint point : loaded) {
                if (point != null && point.name != null && Double.isFinite(point.x)
                        && Double.isFinite(point.y) && Double.isFinite(point.z)) {
                    if (point.dimension == null) point.dimension = "minecraft:overworld";
                    POINTS.add(point);
                }
            }
        } catch (IOException | JsonParseException exception) {
            System.err.println("[Myst Visuals] Не удалось загрузить метки: " + exception.getMessage());
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(POINTS));
        } catch (IOException exception) {
            System.err.println("[Myst Visuals] Не удалось сохранить метки: " + exception.getMessage());
        }
    }

    private static void renderWorld(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.options.hudHidden) return;
        String dimension = client.world.getRegistryKey().getValue().toString();
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayers.lines());
        VertexConsumer quads = context.consumers().getBuffer(RenderLayers.debugQuads());
        animationClock++;
        float rotation = (animationClock % 3600L) * 0.035f;

        for (Waypoint point : POINTS) {
            if (!point.enabled || !safe(point.dimension).equals(dimension)) continue;
            Vec3d base = new Vec3d(point.x + 0.5, point.y, point.z + 0.5);
            if (base.squaredDistanceTo(camera) > 160.0 * 160.0) continue;
            Vec3d p = base.subtract(camera);
            float spin = rotation + (float)(point.x * 0.13 + point.z * 0.17);
            int color = point.color;

            float beamHeight = 8.0f;
            Vec3d a = p.add(-0.065, 0.10, 0), b = p.add(0.065, 0.10, 0);
            Vec3d c = p.add(0.065, beamHeight, 0), d = p.add(-0.065, beamHeight, 0);
            Render3D.drawRibbon(quads, context.matrices().peek(), a, b, d, c,
                    MathUtil.withAlpha(color, 0.045f), MathUtil.withAlpha(color, 0.0f));
            Vec3d e = p.add(0, 0.10, -0.065), f = p.add(0, 0.10, 0.065);
            Vec3d g = p.add(0, beamHeight, 0.065), h = p.add(0, beamHeight, -0.065);
            Render3D.drawRibbon(quads, context.matrices().peek(), e, f, h, g,
                    MathUtil.withAlpha(color, 0.045f), MathUtil.withAlpha(color, 0.0f));

            Vec3d center = p.add(0, 1.45, 0);
            double sin = Math.sin(spin), cos = Math.cos(spin);
            Vec3d top = center.add(0, 0.48, 0), bottom = center.add(0, -0.48, 0);
            Vec3d[] sides = {
                    center.add(cos * 0.34, 0, sin * 0.34),
                    center.add(-sin * 0.34, 0, cos * 0.34),
                    center.add(-cos * 0.34, 0, -sin * 0.34),
                    center.add(sin * 0.34, 0, -cos * 0.34)
            };
            for (Vec3d side : sides) {
                Render3D.drawLine(lines, context.matrices().peek(), top, side, color);
                Render3D.drawLine(lines, context.matrices().peek(), bottom, side, color);
            }
            for (int i = 0; i < 4; i++) {
                Render3D.drawLine(lines, context.matrices().peek(), sides[i], sides[(i + 1) % 4], color);
            }
            Render3D.drawRing(lines, context.matrices().peek(), center, 0.53f, 0,
                    MathUtil.withAlpha(color, 0.82f), 36);
            Render3D.drawRing(lines, context.matrices().peek(), p, 0.42f, 0.035f,
                    MathUtil.withAlpha(color, 0.45f), 28);
        }
    }

    private static void renderLabels(net.minecraft.client.gui.DrawContext context,
                                     net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        String dimension = client.world.getRegistryKey().getValue().toString();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        int width = context.getScaledWindowWidth(), height = context.getScaledWindowHeight();
        double fov = Math.toRadians(client.options.getFov().getValue());
        double focal = (height * 0.5) / Math.tan(fov * 0.5);
        double yaw = Math.toRadians(camera.getYaw()), pitch = Math.toRadians(camera.getPitch());
        Vec3d right = new Vec3d(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3d up = new Vec3d(-Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch),
                Math.cos(yaw) * Math.sin(pitch));
        Vec3d forward = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch));

        for (Waypoint point : POINTS) {
            if (!point.enabled || !safe(point.dimension).equals(dimension)) continue;
            Vec3d target = new Vec3d(point.x + 0.5, point.y + 1.65, point.z + 0.5);
            Vec3d delta = target.subtract(cameraPos);
            double depth = delta.dotProduct(forward);
            if (depth <= 0.15) continue;
            double distance = Math.sqrt(delta.lengthSquared());
            if (distance < 5.0 || distance > 160.0) continue;
            double sx = width * 0.5 + delta.dotProduct(right) * focal / depth;
            double sy = height * 0.5 - delta.dotProduct(up) * focal / depth;
            if (sx < -100 || sx > width + 100 || sy < -50 || sy > height + 50) continue;

            float alpha = MathUtil.clamp((float)((distance - 5.0) / 3.0), 0, 1);
            String name = point.name;
            String meters = String.format(Locale.ROOT, "%.0f m", distance);
            int nameWidth = client.textRenderer.getWidth(name);
            int metersWidth = client.textRenderer.getWidth(meters);
            int panelWidth = nameWidth + metersWidth + 29, panelHeight = 22;
            int x = Math.max(3, Math.min(width - panelWidth - 3, (int)Math.round(sx - panelWidth * 0.5)));
            int y = Math.max(3, Math.min(height - panelHeight - 3, (int)Math.round(sy - panelHeight - 8)));

            var matrices = context.getMatrices();
            var consumers = context.getVertexConsumers();
            VertexConsumer fan = consumers.getBuffer(RenderLayers.debugTriangleFan());
            Render2D.drawShadow(fan, matrices.peek(), x, y, panelWidth, panelHeight, 7,
                    MathUtil.withAlpha(0xE8100E18, alpha), 4, 3.0f);
            Render2D.drawRoundedRect(fan, matrices.peek(), x, y, panelWidth, panelHeight, 7,
                    MathUtil.withAlpha(0xE91A1724, alpha));
            Render2D.drawRoundedRect(fan, matrices.peek(), x + 7, y + 7, 6, 8, 3,
                    MathUtil.withAlpha(point.color, alpha));
            consumers.draw(RenderLayers.debugTriangleFan());
            context.drawText(client.textRenderer, name, x + 19, y + 7,
                    MathUtil.withAlpha(0xFFF5F1FF, alpha), false);
            context.drawText(client.textRenderer, meters, x + panelWidth - metersWidth - 8, y + 7,
                    MathUtil.withAlpha(0xFFBEB5CC, alpha), false);
        }
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
