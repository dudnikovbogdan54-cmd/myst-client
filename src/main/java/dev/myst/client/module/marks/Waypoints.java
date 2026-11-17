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
    private static boolean initialized; private static boolean enabled = true;
    public static void setEnabled(boolean value) { enabled = value; }
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
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.options.hudHidden) return;

        String dimension = client.world.getRegistryKey().getValue().toString();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        double cx = cameraPos.x, cy = cameraPos.y, cz = cameraPos.z;
        VertexConsumer quads = context.consumers().getBuffer(RenderLayers.debugQuads());
        var matrix = context.matrices().peek();
        animationClock++;
        float rotation = (animationClock % 3600L) * 0.035f;

        for (Waypoint point : POINTS) {
            if (!point.enabled || !safe(point.dimension).equals(dimension)) continue;
            float wx = (float)(point.x + 0.5);
            float wy = (float)point.y;
            float wz = (float)(point.z + 0.5);
            if (!Render3D.isVisible(wx, wy + 1.4f, wz, cx, cy, cz,
                    camera.getYaw(), camera.getPitch(), 96.0)) continue;

            float x = (float)(wx - cx), y = (float)(wy - cy), z = (float)(wz - cz);
            float spin = rotation + (float)(point.x * 0.13 + point.z * 0.17);
            int color = point.color;
            float beamHeight = 8.0f;
            int beamStart = MathUtil.withAlpha(color, 0.045f);
            int beamEnd = MathUtil.withAlpha(color, 0.0f);

            Render3D.drawRibbon(quads, matrix,
                    x - 0.065f, y + 0.10f, z, x + 0.065f, y + 0.10f, z,
                    x - 0.065f, y + beamHeight, z, x + 0.065f, y + beamHeight, z,
                    beamStart, beamEnd);
            Render3D.drawRibbon(quads, matrix,
                    x, y + 0.10f, z - 0.065f, x, y + 0.10f, z + 0.065f,
                    x, y + beamHeight, z - 0.065f, x, y + beamHeight, z + 0.065f,
                    beamStart, beamEnd);

            float centerY = y + 1.45f;
            float topY = centerY + 0.48f;
            float bottomY = centerY - 0.48f;
            float c = (float)Math.cos(spin), s = (float)Math.sin(spin);
            float r = 0.34f;
            float s0x = x + c * r, s0z = z + s * r;
            float s1x = x - s * r, s1z = z + c * r;
            float s2x = x - c * r, s2z = z - s * r;
            float s3x = x + s * r, s3z = z - c * r;
            Render3D.drawLine(quads, matrix, x, topY, z, s0x, centerY, s0z, color);
            Render3D.drawLine(quads, matrix, x, bottomY, z, s0x, centerY, s0z, color);
            Render3D.drawLine(quads, matrix, x, topY, z, s1x, centerY, s1z, color);
            Render3D.drawLine(quads, matrix, x, bottomY, z, s1x, centerY, s1z, color);
            Render3D.drawLine(quads, matrix, x, topY, z, s2x, centerY, s2z, color);
            Render3D.drawLine(quads, matrix, x, bottomY, z, s2x, centerY, s2z, color);
            Render3D.drawLine(quads, matrix, x, topY, z, s3x, centerY, s3z, color);
            Render3D.drawLine(quads, matrix, x, bottomY, z, s3x, centerY, s3z, color);
            Render3D.drawLine(quads, matrix, s0x, centerY, s0z, s1x, centerY, s1z, color);
            Render3D.drawLine(quads, matrix, s1x, centerY, s1z, s2x, centerY, s2z, color);
            Render3D.drawLine(quads, matrix, s2x, centerY, s2z, s3x, centerY, s3z, color);
            Render3D.drawLine(quads, matrix, s3x, centerY, s3z, s0x, centerY, s0z, color);
            Render3D.drawRing(quads, matrix, x, centerY, z, 0.53f, 0,
                    MathUtil.withAlpha(color, 0.82f), 32);
            Render3D.drawRing(quads, matrix, x, y, z, 0.42f, 0.035f,
                    MathUtil.withAlpha(color, 0.45f), 32);
        }
    }

    private static void renderLabels(net.minecraft.client.gui.DrawContext context,
                                     net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        String dimension = client.world.getRegistryKey().getValue().toString();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        double cameraX = cameraPos.x, cameraY = cameraPos.y, cameraZ = cameraPos.z;
        int width = context.getScaledWindowWidth(), height = context.getScaledWindowHeight();
        double fov = Math.toRadians(client.options.getFov().getValue());
        double focal = (height * 0.5) / Math.tan(fov * 0.5);
        double yaw = Math.toRadians(camera.getYaw()), pitch = Math.toRadians(camera.getPitch());
        double rightX = Math.cos(yaw), rightZ = Math.sin(yaw);
        double upX = -Math.sin(yaw) * Math.sin(pitch);
        double upY = Math.cos(pitch);
        double upZ = Math.cos(yaw) * Math.sin(pitch);
        double forwardX = -Math.sin(yaw) * Math.cos(pitch);
        double forwardY = -Math.sin(pitch);
        double forwardZ = Math.cos(yaw) * Math.cos(pitch);

        for (Waypoint point : POINTS) {
            if (!point.enabled || !safe(point.dimension).equals(dimension)) continue;
            double dx = point.x + 0.5 - cameraX;
            double dy = point.y + 1.65 - cameraY;
            double dz = point.z + 0.5 - cameraZ;
            double depth = dx * forwardX + dy * forwardY + dz * forwardZ;
            if (depth <= 0.15) continue;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq < 25.0 || distanceSq > 96.0 * 96.0) continue;
            double distance = Math.sqrt(distanceSq);
            double sx = width * 0.5 + (dx * rightX + dz * rightZ) * focal / depth;
            double sy = height * 0.5 - (dx * upX + dy * upY + dz * upZ) * focal / depth;
            if (sx < -100 || sx > width + 100 || sy < -50 || sy > height + 50) continue;

            float alpha = MathUtil.clamp((float)((distance - 5.0) / 3.0), 0, 1);
            String name = point.name;
            String meters = String.format(Locale.ROOT, "%.0f m", distance);
            int nameWidth = client.textRenderer.getWidth(name);
            int metersWidth = client.textRenderer.getWidth(meters);
            int panelWidth = nameWidth + metersWidth + 29, panelHeight = 22;
            int x = Math.max(3, Math.min(width - panelWidth - 3, (int)Math.round(sx - panelWidth * 0.5)));
            int y = Math.max(3, Math.min(height - panelHeight - 3, (int)Math.round(sy - panelHeight - 8)));

            Render2D.drawShadow(context, x, y, panelWidth, panelHeight, 7,
                    MathUtil.withAlpha(0xE8100E18, alpha), 4, 3);
            Render2D.drawRoundedRect(context, x, y, panelWidth, panelHeight, 7,
                    MathUtil.withAlpha(0xE91A1724, alpha));
            Render2D.drawRoundedRect(context, x + 7, y + 7, 6, 8, 3,
                    MathUtil.withAlpha(point.color, alpha));
            context.drawText(client.textRenderer, name, x + 19, y + 7,
                    MathUtil.withAlpha(0xFFF5F1FF, alpha), false);
            context.drawText(client.textRenderer, meters, x + panelWidth - metersWidth - 8, y + 7,
                    MathUtil.withAlpha(0xFFBEB5CC, alpha), false);
        }
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
