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
    public static final int[] ACCENTS = {0xFFB58AFF, 0xFF62D6E8, 0xFFFF8AB3, 0xFF8BE0A1};

    private static final class Data {
        int width = 700, height = 430, theme = 0;
        boolean compact = true;
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
            }
        } catch (Exception ignored) { }
    }

    public static int accent() { return ACCENTS[Math.floorMod(theme, ACCENTS.length)]; }

    public static void save() {
        try {
            Data d = new Data();
            d.width = width; d.height = height; d.theme = theme; d.compact = compact;
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception ignored) { }
    }
}
