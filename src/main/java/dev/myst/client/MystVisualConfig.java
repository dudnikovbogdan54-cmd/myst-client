package dev.myst.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MystVisualConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("myst-visuals-style.json");
    public static int crosshairStyle = 0;
    public static int crosshairColor = 0xFFF1E9FF;
    public static int crosshairSize = 7;
    public static int crosshairGap = 3;
    public static int crosshairThickness = 2;
    public static boolean cpsEnabled = false;
    public static boolean cpsShowBoth = true;
    public static boolean targetDistance = true;
    public static boolean targetSmoothBar = true;
    private static final class Data {
        int crosshairStyle = 0, crosshairColor = 0xFFF1E9FF, crosshairSize = 7, crosshairGap = 3, crosshairThickness = 2;
        boolean cpsEnabled = false, cpsShowBoth = true, targetDistance = true, targetSmoothBar = true;
    }
    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d == null) return;
            crosshairStyle = clamp(d.crosshairStyle, 0, 3);
            crosshairColor = d.crosshairColor;
            crosshairSize = clamp(d.crosshairSize, 3, 15);
            crosshairGap = clamp(d.crosshairGap, 0, 9);
            crosshairThickness = clamp(d.crosshairThickness, 1, 4);
            cpsEnabled = d.cpsEnabled; cpsShowBoth = d.cpsShowBoth;
            targetDistance = d.targetDistance; targetSmoothBar = d.targetSmoothBar;
        } catch (Exception ignored) { }
    }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    public static void save() {
        try {
            Data d = new Data();
            d.crosshairStyle=crosshairStyle; d.crosshairColor=crosshairColor;
            d.crosshairSize=crosshairSize; d.crosshairGap=crosshairGap; d.crosshairThickness=crosshairThickness;
            d.cpsEnabled=cpsEnabled; d.cpsShowBoth=cpsShowBoth;
            d.targetDistance=targetDistance; d.targetSmoothBar=targetSmoothBar;
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception ignored) { }
    }
}
