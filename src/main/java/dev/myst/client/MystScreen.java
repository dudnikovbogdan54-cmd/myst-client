package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class MystScreen extends Screen {
    private TextFieldWidget original, alias;

    public MystScreen() {
        super(Text.literal("MYST Client"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int top = height / 2 - 38;

        original = new TextFieldWidget(textRenderer, cx - 110, top, 220, 20, Text.literal("Original nickname"));
        original.setPlaceholder(Text.literal("Настоящий ник"));
        addDrawableChild(original);

        alias = new TextFieldWidget(textRenderer, cx - 110, top + 28, 220, 20, Text.literal("Streamer nickname"));
        alias.setPlaceholder(Text.literal("Как показывать"));
        addDrawableChild(alias);

        addDrawableChild(ButtonWidget.builder(Text.literal("Добавить / обновить"), button -> {
            String from = original.getText().trim();
            String to = alias.getText().trim();
            if (!from.isEmpty() && !to.isEmpty()) {
                StreamerConfig.aliases.put(from, to);
                StreamerConfig.save();
                original.setText("");
                alias.setText("");
            }
        }).dimensions(cx - 110, top + 56, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(streamerButtonText(), button -> {
            StreamerConfig.enabled = !StreamerConfig.enabled;
            StreamerConfig.save();
            button.setMessage(streamerButtonText());
        }).dimensions(cx - 110, top + 82, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> close())
                .dimensions(cx - 110, top + 110, 220, 20).build());
    }

    private static Text streamerButtonText() {
        return Text.literal("Streamer Mode: " + (StreamerConfig.enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Minecraft 1.21.11 applies menu blur before rendering the screen.
        // Calling renderBackground() here applies blur a second time and crashes.
        context.fill(0, 0, width, height, 0xC00A1224);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 72, 0x55CCFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Owner: Aloxmyst_YT"), width / 2, height / 2 + 88, 0x55CCFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Локальная замена ников в TAB"), width / 2, height / 2 + 101, 0xB8C7DA);
    }
}
