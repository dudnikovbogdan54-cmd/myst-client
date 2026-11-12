package dev.myst.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mist-visuals.json");
    public static final Map<String, Boolean> modules = new LinkedHashMap<>();
    public static final List<Waypoint> waypoints = new ArrayList<>();
    public static boolean waypointSet = false;
    public static double waypointX, waypointY, waypointZ;

    public static final class Waypoint {
        public String name;
        public double x, y, z;
        public boolean enabled = true;
        public boolean showDistance = true;
        public int color = 0xFFB58AFF;
        public String dimension = "";
        public Waypoint(String name, double x, double y, double z) {
            this.name = name; this.x = x; this.y = y; this.z = z;
            this.dimension = currentDimension();
        }
    }

    private static final class Data {
        Map<String, Boolean> modules = new LinkedHashMap<>();
        List<Waypoint> waypoints = new ArrayList<>();
        boolean waypointSet;
        double waypointX, waypointY, waypointZ;
    }

    public static String currentDimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world == null ? "" : client.world.getRegistryKey().getValue().toString();
    }

    public static boolean enabled(String id) { return modules.getOrDefault(id, defaultValue(id)); }
    public static void toggle(String id) { modules.put(id, !enabled(id)); save(); }
    private static boolean defaultValue(String id) {
        return id.equals("watermark") || id.equals("fps") || id.equals("coords") || id.equals("target_hud") || id.equals("block_info");
    }
    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            Data data = GSON.fromJson(Files.readString(FILE), Data.class);
            if (data != null) {
                if (data.modules != null) modules.putAll(data.modules);
                if (data.waypoints != null) {
                    waypoints.clear();
                    for (Waypoint point : data.waypoints) if (point != null) {
                        if (point.dimension == null) point.dimension = "";
                        waypoints.add(point);
                    }
                }
                waypointSet = data.waypointSet;
                waypointX = data.waypointX; waypointY = data.waypointY; waypointZ = data.waypointZ;
                if (waypoints.isEmpty() && waypointSet) {
                    waypoints.add(new Waypoint("Старая метка", waypointX, waypointY, waypointZ));
                }
            }
        } catch (Exception ignored) { }
    }
    public static void addWaypoint(String name, double x, double y, double z) {
        String safeName = name == null || name.trim().isEmpty() ? "Метка " + (waypoints.size() + 1) : name.trim();
        waypoints.add(new Waypoint(safeName, x, y, z));
        syncLegacyWaypoint(); save();
    }
    public static void setWaypoint(double x, double y, double z) {
        addWaypoint("Метка " + (waypoints.size() + 1), x, y, z);
    }
    public static void syncLegacyWaypoint() {
        waypointSet = !waypoints.isEmpty();
        if (waypointSet) {
            Waypoint p = waypoints.get(0);
            waypointX = p.x; waypointY = p.y; waypointZ = p.z;
        }
    }
    public static void save() {
        try {
            syncLegacyWaypoint();
            Data data = new Data();
            data.modules.putAll(modules);
            data.waypoints.addAll(waypoints);
            data.waypointSet = waypointSet;
            data.waypointX = waypointX; data.waypointY = waypointY; data.waypointZ = waypointZ;
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception ignored) { }
    }
}