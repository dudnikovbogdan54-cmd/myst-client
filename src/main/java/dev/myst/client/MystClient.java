package dev.myst.client;

import dev.myst.client.mixin.ScreenAccessor;
import dev.myst.client.module.ModuleManager;
import dev.myst.client.module.marks.Waypoints;
import dev.myst.client.module.visuals.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class MystClient implements ClientModInitializer {
    public static final String MOD_ID = "mystclient";

    private boolean kDown;
    private boolean shiftDown;
    private int guiOpenTicks;

    @Override
    public void onInitializeClient() {
        StreamerConfig.load();
        ModuleManager.initialize();
        ConfigManager.initialize();

        Waypoints.initialize();
        JumpCircles.initialize();
        Trails.initialize();
        ChinaHat.initialize();
        HudManager.initialize();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                int buttonWidth = 100;
                int buttonHeight = 20;
                int buttonX = Math.max(4, scaledWidth - buttonWidth - 8);

                ButtonWidget button = ButtonWidget.builder(
                    Text.literal("Myst Visuals"),
                    widget -> client.setScreen(new ClickGuiScreen())
                ).dimensions(buttonX, 8, buttonWidth, buttonHeight).build();

                ((ScreenAccessor) screen).myst$addDrawableChild(button);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("mystgui").executes(context -> {
                // Wait past the chat screen's close tick before replacing it.
                guiOpenTicks = 2;
                return 1;
            }))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (guiOpenTicks > 0) {
                guiOpenTicks--;
                if (guiOpenTicks == 0 && client.player != null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }

            boolean k = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_K);
            boolean rightShift = InputUtil.isKeyPressed(
                client.getWindow(),
                GLFW.GLFW_KEY_RIGHT_SHIFT
            );

            if (client.player != null && client.currentScreen == null
                && ((k && !kDown) || (rightShift && !shiftDown))) {
                client.setScreen(new ClickGuiScreen());
            }

            kDown = k;
            shiftDown = rightShift;
        });
    }
}
