package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class MystScreen extends Screen {
    private enum Page {
        HUD("HUD"), VISUALS("Visuals"), MARKS("Marks"),
        UTILITIES("Utilities"), STREAMER("Streamer"), CONFIGS("Configs");

        final String label;
        Page(String label) { this.label = label; }
    }

    private Page page = Page.HUD;
    private TextFieldWidget original;
    private TextFieldWidget alias;
    private int panelX, panelY, panelW, panelH, sideW;
    private static final int BG = 0xF00B0914;
    private static final int PANEL = 0xF0141021;
    private static final int CARD = 0xE91D172B;
    private static final int CARD_ALT = 0xE9231A34;
    private static final int PURPLE = 0xFF9B6CFF;
    private static final int PURPLE_DARK = 0xFF7042C8;
    private static final int TEXT = 0xFFF2ECFF;
    private static final int MUTED = 0xFF9D91AD;

    public MystScreen() {
        super(Text.literal("Mist Visuals"));
    }

    @Override
    protected void init() {
        panelW = Math.min(980, width - 32);
        panelH = Math.min(590, height - 32);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        sideW = Math.max(150, Math.min(190, panelW / 5));

        original = null;
        alias = null;
        if (page == Page.STREAMER) {
            int x = panelX + sideW + 34;
            int y = panelY + 160;
            int w = Math.min(360, panelW - sideW - 70);
            original = new TextFieldWidget(textRenderer, x, y, w, 22, Text.literal("Original nickname"));
            original.setPlaceholder(Text.literal("Настоящий ник"));
            alias = new TextFieldWidget(textRenderer, x, y + 34, w, 22, Text.literal("Display nickname"));
            alias.setPlaceholder(Text.literal("Как показывать"));
            addDrawableChild(original);
            addDrawableChild(alias);
        }
    }

    private void select(Page next) {
        if (page == next) return;
        page = next;
        clearChildren();
        init();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xD9080610);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.fill(panelX, panelY, panelX + sideW, panelY + panelH, 0xFF100C1B);
        ctx.fill(panelX + sideW, panelY + 62, panelX + panelW, panelY + 63, 0xFF30243D);
        ctx.fill(panelX + sideW - 1, panelY, panelX + sideW, panelY + panelH, 0xFF30243D);

        // Brand mark and sidebar
        ctx.fill(panelX + 22, panelY + 20, panelX + 58, panelY + 56, PURPLE_DARK);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("MV"), panelX + 40, panelY + 33, TEXT);
        ctx.drawTextWithShadow(textRenderer, Text.literal("MIST"), panelX + 68, panelY + 24, TEXT);
        ctx.drawTextWithShadow(textRenderer, Text.literal("VISUALS"), panelX + 68, panelY + 37, PURPLE);
        ctx.drawTextWithShadow(textRenderer, Text.literal("CLIENT INTERFACE"), panelX + 22, panelY + 82, MUTED);

        int navY = panelY + 108;
        for (Page item : Page.values()) {
            boolean active = item == page;
            int y = navY + item.ordinal() * 43;
            if (active) {
                ctx.fill(panelX + 12, y - 5, panelX + sideW - 12, y + 28, 0xFF392653);
                ctx.fill(panelX + 12, y - 5, panelX + 15, y + 28, PURPLE);
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal(icon(item) + "  " + item.label),
                    panelX + 25, y + 6, active ? TEXT : MUTED);
        }

        ctx.drawTextWithShadow(textRenderer, Text.literal("MIST VISUALS  /  " + page.label.toUpperCase()),
                panelX + sideW + 28, panelY + 24, TEXT);
        ctx.drawTextWithShadow(textRenderer, Text.literal("MV  •  CLIENT SETTINGS"),
                panelX + sideW + 28, panelY + 42, MUTED);
        ctx.fill(panelX + panelW - 46, panelY + 18, panelX + panelW - 18, panelY + 46, 0xFF2A2037);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("×"),
                panelX + panelW - 32, panelY + 27, TEXT);

        drawPage(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(textRenderer, Text.literal("MIST VISUALS  •  1.21.11"),
                panelX + 20, panelY + panelH - 24, 0xFF756A82);
        ctx.drawTextWithShadow(textRenderer, Text.literal("OWNER  ALOXMYST_YT"),
                panelX + sideW + 28, panelY + panelH - 24, 0xFF756A82);
    }

    private void drawPage(DrawContext ctx) {
        int x = panelX + sideW + 28;
        int y = panelY + 88;
        int available = panelW - sideW - 56;
        int gap = 18;
        int cardW = (available - gap) / 2;

        switch (page) {
            case HUD -> {
                heading(ctx, x, y, "HUD", "Настрой элементы интерфейса на экране");
                card(ctx, x, y + 48, cardW, 92, "Watermark", "Компактный логотип Mist Visuals", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 48, cardW, 92, "FPS Counter", "Отображение текущего FPS", "В РАЗРАБОТКЕ");
                card(ctx, x, y + 154, cardW, 92, "Coordinates", "Координаты игрока XYZ", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 154, cardW, 92, "Ping Display", "Задержка соединения", "В РАЗРАБОТКЕ");
                card(ctx, x, y + 260, cardW, 92, "Keystrokes", "Подсветка нажатий клавиш", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 260, cardW, 92, "CPS Counter", "Счётчик кликов в секунду", "В РАЗРАБОТКЕ");
            }
            case VISUALS -> {
                heading(ctx, x, y, "VISUALS", "Визуальные эффекты и оформление");
                card(ctx, x, y + 48, cardW, 92, "Custom Crosshair", "Настраиваемый прицел", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 48, cardW, 92, "Hit Color", "Цветовая индикация попаданий", "В РАЗРАБОТКЕ");
                card(ctx, x, y + 154, cardW, 92, "Particles", "Настройка визуальных частиц", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 154, cardW, 92, "Animations", "Плавность анимаций интерфейса", "В РАЗРАБОТКЕ");
            }
            case MARKS -> {
                heading(ctx, x, y, "MARKS", "Метки и визуальные индикаторы");
                card(ctx, x, y + 48, cardW, 92, "Target Marker", "Индикатор выбранной цели", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 48, cardW, 92, "Waypoints", "Личные метки местности", "В РАЗРАБОТКЕ");
                card(ctx, x, y + 154, cardW, 92, "Damage Indicator", "Числовые индикаторы урона", "В РАЗРАБОТКЕ");
            }
            case UTILITIES -> {
                heading(ctx, x, y, "UTILITIES", "Удобные инструменты клиента");
                card(ctx, x, y + 48, cardW, 92, "Zoom", "Плавное приближение камеры", "В РАЗРАБОТКЕ");
                card(ctx, x + cardW + gap, y + 48, cardW, 92, "Fullbright", "Настройка яркости изображения", "В РАЗРАБОТКЕ");
                card(ctx, x, y + 154, cardW, 92, "Auto Messages", "Быстрые текстовые шаблоны", "В РАЗРАБОТКЕ");
            }
            case STREAMER -> {
                heading(ctx, x, y, "STREAMER MODE", "Локальная замена отображаемых ников в TAB");
                int w = Math.min(420, available);
                ctx.fill(x, y + 48, x + w, y + 88, CARD);
                ctx.drawTextWithShadow(textRenderer, Text.literal("Streamer Mode"), x + 16, y + 62, TEXT);
                boolean enabled = StreamerConfig.enabled;
                ctx.fill(x + w - 90, y + 57, x + w - 16, y + 81, enabled ? 0xFF6541A4 : 0xFF403849);
                ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(enabled ? "ON" : "OFF"),
                        x + w - 53, y + 65, TEXT);
                ctx.drawTextWithShadow(textRenderer, Text.literal("Добавь соответствие настоящего и отображаемого ника."),
                        x, y + 120, MUTED);
                if (original != null && alias != null) {
                    ctx.drawTextWithShadow(textRenderer, Text.literal("НАСТОЯЩИЙ НИК"), x, y + 145, MUTED);
                    ctx.drawTextWithShadow(textRenderer, Text.literal("ОТОБРАЖАЕМЫЙ НИК"), x, y + 179, MUTED);
                    int by = y + 218;
                    ctx.fill(x, by, x + 150, by + 30, PURPLE_DARK);
                    ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("СОХРАНИТЬ НИК"),
                            x + 75, by + 10, TEXT);
                    ctx.drawTextWithShadow(textRenderer, Text.literal("Замены видны только на твоём клиенте."),
                            x, by + 48, MUTED);
                }
            }
            case CONFIGS -> {
                heading(ctx, x, y, "CONFIGS", "Управление настройками Mist Visuals");
                card(ctx, x, y + 48, cardW, 92, "Default Profile", "Базовый профиль клиента", "АКТИВЕН");
                card(ctx, x + cardW + gap, y + 48, cardW, 92, "Export / Import", "Перенос конфигураций", "СКОРО");
                ctx.drawTextWithShadow(textRenderer, Text.literal("Настройки Streamer Mode сохраняются автоматически."),
                        x, y + 170, MUTED);
            }
        }
    }

    private void heading(DrawContext ctx, int x, int y, String title, String subtitle) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(title), x, y, PURPLE);
        ctx.drawTextWithShadow(textRenderer, Text.literal(subtitle), x, y + 19, MUTED);
    }

    private void card(DrawContext ctx, int x, int y, int w, int h, String title, String desc, String state) {
        ctx.fill(x, y, x + w, y + h, CARD);
        ctx.fill(x, y, x + 3, y + h, 0xFF594078);
        ctx.drawTextWithShadow(textRenderer, Text.literal(title), x + 16, y + 17, TEXT);
        ctx.drawTextWithShadow(textRenderer, Text.literal(desc), x + 16, y + 39, MUTED);
        int sw = textRenderer.getWidth(state);
        ctx.drawTextWithShadow(textRenderer, Text.literal(state), x + w - sw - 14, y + h - 22,
                state.equals("АКТИВЕН") ? PURPLE : 0xFF897C98);
    }

    private String icon(Page p) {
        return switch (p) {
            case HUD -> "▣";
            case VISUALS -> "◈";
            case MARKS -> "⚑";
            case UTILITIES -> "✧";
            case STREAMER -> "◎";
            case CONFIGS -> "☷";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int navY = panelY + 108;
            for (Page item : Page.values()) {
                int y = navY + item.ordinal() * 43;
                if (mouseX >= panelX + 10 && mouseX <= panelX + sideW - 10
                        && mouseY >= y - 6 && mouseY <= y + 29) {
                    select(item);
                    return true;
                }
            }
            if (mouseX >= panelX + panelW - 48 && mouseX <= panelX + panelW - 16
                    && mouseY >= panelY + 16 && mouseY <= panelY + 48) {
                close();
                return true;
            }
            if (page == Page.STREAMER) {
                int x = panelX + sideW + 28;
                int y = panelY + 88;
                int w = Math.min(420, panelW - sideW - 56);
                if (mouseX >= x + w - 94 && mouseX <= x + w - 12
                        && mouseY >= y + 52 && mouseY <= y + 86) {
                    StreamerConfig.enabled = !StreamerConfig.enabled;
                    StreamerConfig.save();
                    return true;
                }
                if (mouseX >= x && mouseX <= x + 150
                        && mouseY >= y + 218 && mouseY <= y + 250
                        && original != null && alias != null) {
                    String from = original.getText().trim();
                    String to = alias.getText().trim();
                    if (!from.isEmpty() && !to.isEmpty()) {
                        StreamerConfig.aliases.put(from, to);
                        StreamerConfig.save();
                        original.setText("");
                        alias.setText("");
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
