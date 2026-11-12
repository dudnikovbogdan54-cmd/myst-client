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
    private static KeyBinding zoomKey;
    private static KeyBinding waypointKey;
    private static Integer previousFov;
    private static boolean zooming;

    @Override
    public void onInitializeClient() {
        StreamerConfig.load();
        MistConfig.load();
        MistHud.register();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mystclient.menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mystclient.zoom", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C, KeyBinding.Category.MISC));
        waypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mystclient.waypoint", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B, KeyBinding.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MistHud.tick();
            if (waypointKey.wasPressed() && client.player != null) {
                MistConfig.setWaypoint(client.player.getX(), client.player.getY(), client.player.getZ());
            }
            if (MistConfig.enabled("zoom") && zoomKey.isPressed()) {
                if (!zooming) {
                    previousFov = client.options.getFov().getValue();
                    zooming = true;
                }
                client.options.getFov().setValue(Math.max(20, Math.min(35, previousFov == null ? 70 : previousFov / 2)));
            } else if (zooming) {
                if (previousFov != null) client.options.getFov().setValue(previousFov);
                previousFov = null;
                zooming = false;
            }
            while (openMenu.wasPressed()) client.setScreen(new MystScreen());
        });
    }
}
