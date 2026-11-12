package dev.myst.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class MystClient implements ClientModInitializer {
    public static final String MOD_ID = "mystclient";
    private static KeyBinding openMenu;
    @Override public void onInitializeClient() {
        StreamerConfig.load();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mystclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) client.setScreen(new MystScreen());
        });
    }
}
