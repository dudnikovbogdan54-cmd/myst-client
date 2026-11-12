package dev.myst.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MystUiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("myst-visuals-ui.json");
    public static int width = 700;
    public static int height = 430;
    public static int theme = 0;
    public static boolean compact = true;
    public static boolean hudBackground = true;
    public static int hudOpacity = 85;
    public static final int[] ACCENTS = {0xFFFF7B1D, 0xFFB58AFF, 0xFF4CC9FF, 0xFF65E39A, 0xFFFF5964};
    public static final String[] THEME_NAMES = {"Orange", "Purple", "Neon Blue", "Mint", "Red"};
    public static String themeName() { return THEME_NAMES[Math.floorMod(theme, THEME_NAMES.length)]; }

    private static final class Data {
        int width = 700, height = 430, theme = 0;
        boolean compact = true;
        boolean hudBackground = true;
        int hudOpacity = 85;
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d != null) {
                width = Math.max(520, Math.min(1160, d.width));
                height = Math.max(330, Math.min(700, d.height));
                theme = Math.floorMod(d.theme, ACCENTS.length);
                compact = d.compact;
                hudBackground = d.hudBackground;
                hudOpacity = Math.max(35, Math.min(100, d.hudOpacity));
            }
        } catch (Exception ignored) { }
    }

    public static int accent() { return ACCENTS[Math.floorMod(theme, ACCENTS.length)]; }

    public static void save() {
        try {
            Data d = new Data();
            d.width = width; d.height = height; d.theme = theme; d.compact = compact;
            d.hudBackground = hudBackground; d.hudOpacity = hudOpacity;
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception ignored) { }
    }
}
