package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class MystScreen extends Screen {
    private TextFieldWidget original, alias;
    public MystScreen() { super(Text.literal("MYST Client")); }
    @Override protected void init() {
        int cx = width / 2;
        original = new TextFieldWidget(textRenderer, cx - 110, height / 2 - 42, 220, 20, Text.literal("Original nickname"));
        original.setPlaceholder(Text.literal("Настоящий ник")); addDrawableChild(original);
        alias = new TextFieldWidget(textRenderer, cx - 110, height / 2 - 14, 220, 20, Text.literal("Streamer nickname"));
        alias.setPlaceholder(Text.literal("Как показывать")); addDrawableChild(alias);
        addDrawableChild(ButtonWidget.builder(Text.literal("Добавить / обновить"), b -> {
            String from = original.getText().trim(), to = alias.getText().trim();
            if (!from.isEmpty() && !to.isEmpty()) { StreamerConfig.aliases.put(from, to); StreamerConfig.save(); original.setText(""); alias.setText(""); }
        }).dimensions(cx - 110, height / 2 + 16, 220, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Streamer: " + (StreamerConfig.enabled ? "ВКЛ" : "ВЫКЛ")), b -> {
            StreamerConfig.enabled = !StreamerConfig.enabled; StreamerConfig.save(); b.setMessage(Text.literal("Streamer: " + (StreamerConfig.enabled ? "ВКЛ" : "ВЫКЛ")));
        }).dimensions(cx - 110, height / 2 + 42, 220, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close()).dimensions(cx - 110, height / 2 + 70, 220, 20).build());
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta); super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 78, 0x55CCFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Локальная замена ников в TAB"), width / 2, height / 2 + 98, 0xAAAAAA);
    }
}
