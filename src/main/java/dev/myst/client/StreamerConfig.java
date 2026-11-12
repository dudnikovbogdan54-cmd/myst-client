package dev.myst.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StreamerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mystclient-streamer.json");
    public static boolean enabled = true;
    public static final Map<String,String> aliases = new LinkedHashMap<>();
    private static final class Data { boolean enabled = true; Map<String,String> aliases = new LinkedHashMap<>(); }
    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        try { Data d = GSON.fromJson(Files.readString(FILE), Data.class); if (d != null) { enabled = d.enabled; aliases.clear(); if (d.aliases != null) aliases.putAll(d.aliases); } }
        catch (Exception ignored) { }
    }
    public static void save() {
        try { Data d = new Data(); d.enabled = enabled; d.aliases.putAll(aliases); Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(d)); }
        catch (IOException ignored) { }
    }
    public static String replace(String original) { return enabled ? aliases.getOrDefault(original, original) : original; }
}
