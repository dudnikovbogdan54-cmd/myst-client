package dev.myst.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mist-visuals.json");
    public static final Map<String, Boolean> modules = new LinkedHashMap<>();
    private static final class Data {
        Map<String, Boolean> modules = new LinkedHashMap<>();
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
            if (data != null && data.modules != null) modules.putAll(data.modules);
        } catch (Exception ignored) { }
    }
    public static void save() {
        try {
            Data data = new Data();
            data.modules.putAll(modules);
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data));
        } catch (Exception ignored) { }
    }
}
