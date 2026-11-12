package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class MystScreen extends Screen {
    private enum Page {
        HUD("HUD", "Экранные элементы"),
        VISUALS("Visuals", "Внешний вид игры"),
        EFFECTS("Effects", "Частицы и анимации"),
        MARKS("Marks", "Метки и ориентиры"),
        UTILITIES("Utilities", "Полезные инструменты"),
        STREAMER("Streamer", "Приватность ника"),
        CONFIGS("Settings", "Тема и интерфейс"),
        VISUAL_LAB("Visual Lab", "Точная настройка визуалов");

        final String label, subtitle;
        Page(String label, String subtitle) { this.label = label; this.subtitle = subtitle; }
    }

    private record ModuleCard(String id, String title, String description, boolean implemented) {}
    private record Rect(int x, int y, int w, int h) {
        boolean contains(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private Page page = Page.HUD;
    private TextFieldWidget original, alias;
    private int px, py, pw, ph, sw, scrollOffset, contentHeight;
    private boolean resizing;
    private int dragStartX, dragStartY, startWidth, startHeight;

    private static final int MIN_W = 520, MIN_H = 330, MAX_W = 1160, MAX_H = 760;
    private static final int PANEL = 0xF0110D1B, SIDEBAR = 0xF00D0A15;
    private static final int CARD = 0xF01D1728, CARD_HOVER = 0xFF292039;
    private static final int WHITE = 0xFFF4F0FA, MUTED = 0xFF9D91AD;
    private int accent() { return MystUiConfig.accent(); }

    public MystScreen() { super(Text.literal("Myst Visuals")); }

    @Override
    protected void init() {
        pw = Math.max(Math.min(MIN_W, width - 16), Math.min(pw == 0 ? MystUiConfig.width : pw, Math.min(MAX_W, width - 16)));
        ph = Math.max(Math.min(MIN_H, height - 16), Math.min(ph == 0 ? MystUiConfig.height : ph, Math.min(MAX_H, height - 16)));
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        sw = Math.max(132, Math.min(154, pw / 5));
        clearChildren();
        original = null;
        alias = null;
        if (page == Page.STREAMER) {
            int x = px + sw + 22;
            int y = py + 270 - scrollOffset;
            int w = Math.min(340, pw - sw - 44);
            original = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal("Original nickname"));
            original.setPlaceholder(Text.literal("Настоящий ник"));
            alias = new TextFieldWidget(textRenderer, x, y + 38, w, 20, Text.literal("Как показывать"));
            alias.setPlaceholder(Text.literal("Новый ник"));
            addDrawableChild(original);
            addDrawableChild(alias);
        }
        clampScroll();
    }

    private void select(Page next) {
        if (page != next) {
            page = next;
            scrollOffset = 0;
            init();
        }
    }

    private List<ModuleCard> cards() {
        List<ModuleCard> list = new ArrayList<>();
        switch (page) {
            case HUD -> {
                list.add(new ModuleCard("watermark", "Watermark", "Логотип Myst Visuals", true));
                list.add(new ModuleCard("fps", "FPS Counter", "Частота кадров", true));
                list.add(new ModuleCard("coords", "Coordinates", "Координаты XYZ", true));
                list.add(new ModuleCard("ping", "Ping Display", "Задержка соединения", true));
                list.add(new ModuleCard("target_hud", "Target HUD", "Информация о выбранной цели", true));
                list.add(new ModuleCard("block_info", "Block Info", "Название блока под прицелом", true));
                list.add(new ModuleCard("keystrokes", "Keystrokes", "Отображение нажатий клавиш", true));
                list.add(new ModuleCard("cps", "CPS Counter", "Счётчик кликов мыши", true));
                list.add(new ModuleCard("direction", "Direction", "Сторона света и направление", true));
                list.add(new ModuleCard("speed", "Speed", "Скорость движения в блоках/с", true));
            }
            case VISUALS -> {
                list.add(new ModuleCard("fullbright", "Fullbright", "Повышенная яркость мира", true));
                list.add(new ModuleCard("crosshair", "Custom Crosshair", "Форма, цвет и размер прицела", true));
                list.add(new ModuleCard("block_overlay", "Block Overlay", "Настраиваемая подсветка блока", false));
                list.add(new ModuleCard("weather_fx", "Weather FX", "Декоративные эффекты погоды", false));
                list.add(new ModuleCard("zoom", "Zoom", "Плавное приближение по клавише C", true));
            }
            case EFFECTS -> {
                list.add(new ModuleCard("kill_effect", "Kill Effect", "3D-эффект частиц в мире", true));
                list.add(new ModuleCard("hit_particles", "Hit Particles", "Настройка частиц попадания", false));
                list.add(new ModuleCard("totem_fx", "Totem Pop FX", "Эффект активации тотема", false));
                list.add(new ModuleCard("animations", "Animations", "Плавность интерфейса", false));
            }
            case MARKS -> {
                list.add(new ModuleCard("waypoints", "Waypoints", "3D-метки с координатами", true));
                list.add(new ModuleCard("death_marker", "Death Marker", "Личная метка последней точки", false));
            }
            case UTILITIES -> {
                list.add(new ModuleCard("zoom", "Zoom", "Приближение при удержании C", true));
                list.add(new ModuleCard("keystrokes", "Keystrokes", "Визуализация клавиш", true));
                list.add(new ModuleCard("cps", "CPS Counter", "Счётчик кликов", true));
            }
            case STREAMER -> list.add(new ModuleCard("streamer", "Streamer Mode", "Локальная замена ников в TAB", true));
            case VISUAL_LAB -> { }
            case CONFIGS -> { }
        }
        return list;
    }

    private boolean isEnabled(String id) {
        if (id.equals("streamer")) return StreamerConfig.enabled;
        if (id.equals("cps")) return MystVisualConfig.cpsEnabled;
        return MistConfig.enabled(id);
    }

    private void toggle(String id) {
        if (id.equals("cps")) {
            MystVisualConfig.cpsEnabled = !MystVisualConfig.cpsEnabled;
            MystVisualConfig.save();
        } else if (id.equals("streamer")) {
            StreamerConfig.enabled = !StreamerConfig.enabled;
            StreamerConfig.save();
        } else MistConfig.toggle(id);
    }

    private int contentLeft() { return px + sw + 22; }
    private int contentRight() { return px + pw - 18; }
    private int contentTop() { return py + 82; }
    private int contentBottom() { return py + ph - 34; }
    private int contentWidth() { return Math.max(120, contentRight() - contentLeft()); }
    private int columns() { return contentWidth() >= 500 ? 2 : 1; }
    private int cardHeight() { return 76; }
    private int cardGap() { return 10; }
    private int cardStep() { return cardHeight() + cardGap(); }

    private void clampScroll() {
        int rows = (cards().size() + columns() - 1) / columns();
        int heightForCards = 8 + rows * cardStep();
        int extra = page == Page.VISUAL_LAB ? 300 : page == Page.CONFIGS ? 300 : page == Page.STREAMER ? 260 : 0;
        contentHeight = Math.max(heightForCards, extra + 20);
        int viewport = Math.max(90, contentBottom() - contentTop());
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, contentHeight - viewport)));
    }

    private static void rounded(DrawContext c, int x, int y, int w, int h, int color, int radius) {
        if (w <= 0 || h <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(w / 2, h / 2)));
        if (r == 0) { c.fill(x, y, x + w, y + h, color); return; }
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < r) {
                double dy = r - row - 0.5;
                inset = r - (int)Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            } else if (row >= h - r) {
                double dy = row - (h - r) + 0.5;
                inset = r - (int)Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            }
            c.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

    private void action(DrawContext c, Rect r, String label, int mx, int my, boolean active) {
        boolean hover = r.contains(mx, my);
        rounded(c, r.x, r.y, r.w, r.h, active ? 0xFF4D326C : hover ? 0xFF393044 : 0xFF292333, 6);
        if (active) rounded(c, r.x + 1, r.y + 1, r.w - 2, 2, accent(), 1);
        String shown = fit(label, Math.max(12, r.w - 14));
        c.drawCenteredTextWithShadow(textRenderer, shown, r.x + r.w / 2, r.y + (r.h - 8) / 2, WHITE);
    }

    private String fit(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String dots = "…";
        int end = value.length();
        while (end > 0 && textRenderer.getWidth(value.substring(0, end) + dots) > maxWidth) end--;
        return end <= 0 ? dots : value.substring(0, end) + dots;
    }

    private void toggleSwitch(DrawContext c, int x, int y, boolean enabled) {
        rounded(c, x, y, 38, 19, enabled ? accent() : 0xFF51495C, 10);
        rounded(c, enabled ? x + 21 : x + 2, y + 2, 15, 15, 0xFFF8F4FF, 8);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC9080610);
        rounded(c, px - 4, py - 4, pw + 8, ph + 8, 0x553D2B56, 13);
        rounded(c, px, py, pw, ph, PANEL, 11);
        rounded(c, px, py, pw, 3, accent(), 2);
        rounded(c, px, py, sw, ph, SIDEBAR, 11);
        c.fill(px + sw - 1, py + 8, px + sw, py + ph - 8, 0xFF352844);
        c.fill(px + sw + 12, py + 66, px + pw - 12, py + 67, 0xFF352844);

        rounded(c, px + 17, py + 17, 36, 36, accent(), 9);
        c.drawCenteredTextWithShadow(textRenderer, "M", px + 35, py + 30, WHITE);
        c.drawTextWithShadow(textRenderer, "MYST", px + 62, py + 19, WHITE);
        c.drawTextWithShadow(textRenderer, "VISUALS", px + 62, py + 34, accent());
        c.drawTextWithShadow(textRenderer, "WORKSPACE", px + 18, py + 72, MUTED);

        int navY = py + 86;
        for (Page p : Page.values()) {
            int y = navY + p.ordinal() * 27;
            Rect nav = new Rect(px + 9, y, sw - 18, 23);
            boolean active = p == page;
            if (active) rounded(c, nav.x, nav.y, nav.w, nav.h, 0xFF30223F, 6);
            if (active) rounded(c, nav.x, nav.y + 4, 3, nav.h - 8, accent(), 2);
            c.drawTextWithShadow(textRenderer, String.format(java.util.Locale.ROOT, "%02d", p.ordinal() + 1),
                    nav.x + 10, y + 8, active ? accent() : 0xFF655A72);
            c.drawTextWithShadow(textRenderer, p.label, nav.x + 34, y + 8, active ? WHITE : MUTED);
        }
        c.drawTextWithShadow(textRenderer, "FABRIC 1.21.11", px + 18, py + ph - 23, 0xFF756A82);

        int headX = contentLeft();
        c.drawTextWithShadow(textRenderer, page.label.toUpperCase(java.util.Locale.ROOT), headX, py + 18, WHITE);
        c.drawTextWithShadow(textRenderer, page.subtitle, headX, py + 36, MUTED);
        action(c, new Rect(px + pw - 112, py + 15, 26, 25), "−", mx, my, false);
        action(c, new Rect(px + pw - 80, py + 15, 26, 25), "+", mx, my, false);
        action(c, new Rect(px + pw - 43, py + 15, 26, 25), "×", mx, my, false);
        c.drawTextWithShadow(textRenderer, pw + " × " + ph, px + pw - 112, py + 45, MUTED);

        int left = contentLeft(), right = contentRight(), top = contentTop(), bottom = contentBottom();
        c.enableScissor(left, top, right, bottom);
        if (page == Page.VISUAL_LAB) renderVisualLab(c, mx, my);
        else if (page == Page.CONFIGS) renderSettings(c, mx, my);
        else if (page == Page.STREAMER) renderStreamer(c, mx, my);
        else renderCards(c, mx, my);
        c.disableScissor();

        int viewport = bottom - top;
        if (contentHeight > viewport) {
            int trackX = right - 4;
            rounded(c, trackX, top + 2, 3, viewport - 4, 0xFF30283A, 2);
            int thumbHeight = Math.max(24, viewport * viewport / Math.max(viewport, contentHeight));
            int maxScroll = Math.max(1, contentHeight - viewport);
            int thumbY = top + 2 + (viewport - 4 - thumbHeight) * scrollOffset / maxScroll;
            rounded(c, trackX - 1, thumbY, 5, thumbHeight, accent(), 3);
        }

        c.drawTextWithShadow(textRenderer, "КОЛЁСИКО — ПРОКРУТКА", px + 18, py + ph - 13, 0xFF756A82);
        c.drawTextWithShadow(textRenderer, "MYST VISUALS  •  CLIENT SIDE", left, py + ph - 13, 0xFF756A82);
        rounded(c, px + pw - 16, py + ph - 16, 10, 10, accent(), 4);
        super.render(c, mx, my, delta);
    }

    private void renderCards(DrawContext c, int mx, int my) {
        List<ModuleCard> list = cards();
        int cols = columns(), gap = cardGap();
        int cw = (contentWidth() - gap * (cols - 1)) / cols;
        for (int i = 0; i < list.size(); i++) {
            ModuleCard card = list.get(i);
            int col = i % cols, row = i / cols;
            int x = contentLeft() + col * (cw + gap);
            int y = contentTop() + 5 + row * cardStep() - scrollOffset;
            Rect r = new Rect(x, y, cw, cardHeight());
            boolean hover = r.contains(mx, my);
            rounded(c, x, y, cw, cardHeight(), hover ? CARD_HOVER : CARD, 7);
            rounded(c, x + 8, y + 9, 3, cardHeight() - 18, card.implemented ? accent() : 0xFF51465D, 2);
            int toggleSpace = card.implemented ? 72 : 52;
            c.drawTextWithShadow(textRenderer, fit(card.title, cw - toggleSpace - 32), x + 18, y + 10, WHITE);
            c.drawTextWithShadow(textRenderer, fit(card.description, cw - 28), x + 18, y + 29, MUTED);
            c.drawTextWithShadow(textRenderer, card.implemented ? "ГОТОВО" : "В ПЛАНАХ",
                    x + 18, y + 54, card.implemented ? accent() : 0xFF8A8195);
            if (card.implemented) {
                boolean enabled = isEnabled(card.id);
                c.drawTextWithShadow(textRenderer, enabled ? "ON" : "OFF", x + cw - 72, y + 56, enabled ? accent() : MUTED);
                toggleSwitch(c, x + cw - 43, y + 52, enabled);
            } else {
                c.drawTextWithShadow(textRenderer, "SOON", x + cw - 43, y + 56, MUTED);
            }
        }
        if (page == Page.MARKS) {
            int y = contentTop() + 5 + list.size() * cardStep() - scrollOffset;
            c.drawTextWithShadow(textRenderer, "Нажми ПКМ по карточке Waypoints, чтобы открыть редактор меток.",
                    contentLeft(), y + 4, MUTED);
        }
    }

    private void renderVisualLab(DrawContext c, int mx, int my) {
        int x = contentLeft(), y = contentTop() + 8 - scrollOffset;
        c.drawTextWithShadow(textRenderer, "CUSTOM CROSSHAIR", x, y, WHITE);
        int half = (contentWidth() - 10) / 2;
        action(c, new Rect(x, y + 18, half, 26), "СТИЛЬ: " + new String[]{"CLASSIC", "DOT", "PLUS", "BRACKETS"}[MystVisualConfig.crosshairStyle], mx, my, true);
        action(c, new Rect(x + half + 10, y + 18, half, 26), "ЦВЕТ ПРИЦЕЛА", mx, my, false);
        settingStepper(c, x, y + 55, "Размер", MystVisualConfig.crosshairSize, x + contentWidth() - 104, mx, my);
        settingStepper(c, x, y + 91, "Зазор", MystVisualConfig.crosshairGap, x + contentWidth() - 104, mx, my);
        settingStepper(c, x, y + 127, "Толщина", MystVisualConfig.crosshairThickness, x + contentWidth() - 104, mx, my);
        c.drawTextWithShadow(textRenderer, "HUD И TARGET", x, y + 172, WHITE);
        action(c, new Rect(x, y + 190, half, 28), MystVisualConfig.cpsEnabled ? "CPS COUNTER: ON" : "CPS COUNTER: OFF", mx, my, MystVisualConfig.cpsEnabled);
        action(c, new Rect(x + half + 10, y + 190, half, 28), MystVisualConfig.cpsShowBoth ? "LMB + RMB" : "ТОЛЬКО LMB", mx, my, false);
        action(c, new Rect(x, y + 226, half, 28), MystVisualConfig.targetDistance ? "ДИСТАНЦИЯ: ON" : "ДИСТАНЦИЯ: OFF", mx, my, MystVisualConfig.targetDistance);
        action(c, new Rect(x + half + 10, y + 226, half, 28), MystVisualConfig.targetSmoothBar ? "ПЛАВНОЕ HP: ON" : "ПЛАВНОЕ HP: OFF", mx, my, MystVisualConfig.targetSmoothBar);
    }

    private void settingStepper(DrawContext c, int x, int y, String label, int value, int controlsX, int mx, int my) {
        c.drawTextWithShadow(textRenderer, label + ": " + value, x, y + 8, MUTED);
        action(c, new Rect(controlsX, y, 42, 25), "−", mx, my, false);
        action(c, new Rect(controlsX + 50, y, 42, 25), "+", mx, my, false);
    }

    private void renderSettings(DrawContext c, int mx, int my) {
        int x = contentLeft(), y = contentTop() + 8 - scrollOffset;
        int half = (contentWidth() - 10) / 2;
        c.drawTextWithShadow(textRenderer, "ОФОРМЛЕНИЕ МЕНЮ", x, y, WHITE);
        action(c, new Rect(x, y + 18, half, 28), "ТЕМА: " + (MystUiConfig.theme + 1), mx, my, true);
        action(c, new Rect(x + half + 10, y + 18, half, 28), MystUiConfig.compact ? "КОМПАКТНО: ON" : "КОМПАКТНО: OFF", mx, my, MystUiConfig.compact);
        c.drawTextWithShadow(textRenderer, "РАЗМЕР ОКНА", x, y + 66, WHITE);
        action(c, new Rect(x, y + 84, half, 28), "МЕНЬШЕ", mx, my, false);
        action(c, new Rect(x + half + 10, y + 84, half, 28), "БОЛЬШЕ", mx, my, false);
        c.drawTextWithShadow(textRenderer, "HUD-ПАНЕЛИ", x, y + 132, WHITE);
        action(c, new Rect(x, y + 150, half, 28), MystUiConfig.hudBackground ? "ФОН: ON" : "ФОН: OFF", mx, my, MystUiConfig.hudBackground);
        action(c, new Rect(x + half + 10, y + 150, half, 28), "ПРОЗРАЧНОСТЬ: " + MystUiConfig.hudOpacity + "%", mx, my, false);
        action(c, new Rect(x, y + 194, contentWidth(), 28), "СБРОСИТЬ РАЗМЕР И ТЕМУ", mx, my, false);
        c.drawTextWithShadow(textRenderer, "Скруглённые карточки и переключатели включены в стиль Myst.",
                x, y + 238, MUTED);
    }

    private void renderStreamer(DrawContext c, int mx, int my) {
        int x = contentLeft(), y = contentTop() + 10 - scrollOffset;
        c.drawTextWithShadow(textRenderer, "STREAMER MODE", x, y, WHITE);
        c.drawTextWithShadow(textRenderer, "Замена ника действует только локально на твоём клиенте.", x, y + 20, MUTED);
        c.drawTextWithShadow(textRenderer, "НАСТОЯЩИЙ НИК", x, y + 62, MUTED);
        c.drawTextWithShadow(textRenderer, "КАК ПОКАЗЫВАТЬ", x, y + 120, MUTED);
        action(c, new Rect(x, y + 174, 190, 28), "СОХРАНИТЬ НИК", mx, my, true);
    }

    private void drawSmallButton(DrawContext c, int x, int y, int w, String label, int mx, int my) {
        action(c, new Rect(x, y, w, 26), label, mx, my, false);
    }

    private void rebuild() { init(); }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() == 0 && mx >= px + pw - 20 && my >= py + ph - 20) {
            resizing = true; dragStartX = (int)mx; dragStartY = (int)my; startWidth = pw; startHeight = ph;
            return true;
        }
        if (click.button() == 0 && my >= py + 10 && my <= py + 45) {
            if (mx >= px + pw - 114 && mx < px + pw - 84) {
                resizeWindow(-55, -35); return true;
            }
            if (mx >= px + pw - 82 && mx < px + pw - 52) {
                resizeWindow(55, 35); return true;
            }
            if (mx >= px + pw - 46) { close(); return true; }
        }
        if (click.button() == 0) {
            int navY = py + 86;
            for (Page p : Page.values()) {
                Rect nav = new Rect(px + 9, navY + p.ordinal() * 27, sw - 18, 23);
                if (nav.contains(mx, my)) { select(p); return true; }
            }
            if (page == Page.VISUAL_LAB && clickVisualLab(mx, my)) return true;
            if (page == Page.CONFIGS && clickSettings(mx, my)) return true;
            if (page == Page.STREAMER && clickStreamer(mx, my)) return true;

            if (mx >= contentLeft() && mx <= contentRight() && my >= contentTop() && my <= contentBottom()) {
                List<ModuleCard> list = cards();
                int cols = columns(), gap = cardGap();
                int cw = (contentWidth() - gap * (cols - 1)) / cols;
                for (int i = 0; i < list.size(); i++) {
                    ModuleCard card = list.get(i);
                    int col = i % cols, row = i / cols;
                    Rect r = new Rect(contentLeft() + col * (cw + gap),
                            contentTop() + 5 + row * cardStep() - scrollOffset, cw, cardHeight());
                    if (r.contains(mx, my) && card.implemented) {
                        if (card.id.equals("waypoints") && click.button() == 1) client.setScreen(new WaypointScreen(this));
                        else toggle(card.id);
                        return true;
                    }
                }
            }
        }
        if (click.button() == 1 && page == Page.MARKS) {
            List<ModuleCard> list = cards();
            int cols = columns(), gap = cardGap();
            int cw = (contentWidth() - gap * (cols - 1)) / cols;
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).id.equals("waypoints")) continue;
                Rect r = new Rect(contentLeft(), contentTop() + 5 - scrollOffset, cw, cardHeight());
                if (r.contains(mx, my)) { client.setScreen(new WaypointScreen(this)); return true; }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private void resizeWindow(int dw, int dh) {
        pw = Math.max(Math.min(MIN_W, width - 16), Math.min(Math.min(MAX_W, width - 16), pw + dw));
        ph = Math.max(Math.min(MIN_H, height - 16), Math.min(Math.min(MAX_H, height - 16), ph + dh));
        MystUiConfig.width = pw; MystUiConfig.height = ph; MystUiConfig.save(); rebuild();
    }

    private boolean clickVisualLab(double mx, double my) {
        int x = contentLeft(), y = contentTop() + 8 - scrollOffset;
        int half = (contentWidth() - 10) / 2;
        if (new Rect(x, y + 18, half, 26).contains(mx, my)) MystVisualConfig.crosshairStyle = (MystVisualConfig.crosshairStyle + 1) % 4;
        else if (new Rect(x + half + 10, y + 18, half, 26).contains(mx, my)) {
            int[] colors = {0xFFF1E9FF, 0xFFB58AFF, 0xFF62D6E8, 0xFFFF8AB3, 0xFF8BE0A1, 0xFFFFD166};
            int current = 0; for (int i = 0; i < colors.length; i++) if (colors[i] == MystVisualConfig.crosshairColor) current = i;
            MystVisualConfig.crosshairColor = colors[(current + 1) % colors.length];
        } else if (stepperHit(mx, my, x, y + 55)) MystVisualConfig.crosshairSize = clamp(MystVisualConfig.crosshairSize + (mx < x + contentWidth() - 54 ? -1 : 1), 3, 15);
        else if (stepperHit(mx, my, x, y + 91)) MystVisualConfig.crosshairGap = clamp(MystVisualConfig.crosshairGap + (mx < x + contentWidth() - 54 ? -1 : 1), 0, 9);
        else if (stepperHit(mx, my, x, y + 127)) MystVisualConfig.crosshairThickness = clamp(MystVisualConfig.crosshairThickness + (mx < x + contentWidth() - 54 ? -1 : 1), 1, 4);
        else if (new Rect(x, y + 190, half, 28).contains(mx, my)) MystVisualConfig.cpsEnabled = !MystVisualConfig.cpsEnabled;
        else if (new Rect(x + half + 10, y + 190, half, 28).contains(mx, my)) MystVisualConfig.cpsShowBoth = !MystVisualConfig.cpsShowBoth;
        else if (new Rect(x, y + 226, half, 28).contains(mx, my)) MystVisualConfig.targetDistance = !MystVisualConfig.targetDistance;
        else if (new Rect(x + half + 10, y + 226, half, 28).contains(mx, my)) MystVisualConfig.targetSmoothBar = !MystVisualConfig.targetSmoothBar;
        else return false;
        MystVisualConfig.save(); return true;
    }

    private boolean stepperHit(double mx, double my, int x, int y) {
        int controlsX = x + contentWidth() - 104;
        return new Rect(controlsX, y, 42, 25).contains(mx, my) || new Rect(controlsX + 50, y, 42, 25).contains(mx, my);
    }

    private boolean clickSettings(double mx, double my) {
        int x = contentLeft(), y = contentTop() + 8 - scrollOffset;
        int half = (contentWidth() - 10) / 2;
        if (new Rect(x, y + 18, half, 28).contains(mx, my)) MystUiConfig.theme = (MystUiConfig.theme + 1) % MystUiConfig.ACCENTS.length;
        else if (new Rect(x + half + 10, y + 18, half, 28).contains(mx, my)) MystUiConfig.compact = !MystUiConfig.compact;
        else if (new Rect(x, y + 84, half, 28).contains(mx, my)) resizeWindow(-40, -25);
        else if (new Rect(x + half + 10, y + 84, half, 28).contains(mx, my)) resizeWindow(40, 25);
        else if (new Rect(x, y + 150, half, 28).contains(mx, my)) MystUiConfig.hudBackground = !MystUiConfig.hudBackground;
        else if (new Rect(x + half + 10, y + 150, half, 28).contains(mx, my)) MystUiConfig.hudOpacity = MystUiConfig.hudOpacity <= 55 ? 100 : MystUiConfig.hudOpacity - 15;
        else if (new Rect(x, y + 194, contentWidth(), 28).contains(mx, my)) {
            pw = Math.min(700, width - 16); ph = Math.min(430, height - 16);
            MystUiConfig.theme = 0; MystUiConfig.compact = true;
            MystUiConfig.width = pw; MystUiConfig.height = ph;
        } else return false;
        MystUiConfig.save(); clampScroll(); return true;
    }

    private boolean clickStreamer(double mx, double my) {
        int x = contentLeft(), y = contentTop() + 10 - scrollOffset;
        if (new Rect(x, y + 174, 190, 28).contains(mx, my) && original != null && alias != null) {
            String from = original.getText().trim(), to = alias.getText().trim();
            if (!from.isEmpty() && !to.isEmpty()) {
                StreamerConfig.aliases.put(from, to);
                StreamerConfig.save();
                original.setText(""); alias.setText("");
            }
            return true;
        }
        return false;
    }

    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (resizing && click.button() == 0) {
            pw = clamp(startWidth + (int)(click.x() - dragStartX), Math.min(MIN_W, width - 16), Math.min(MAX_W, width - 16));
            ph = clamp(startHeight + (int)(click.y() - dragStartY), Math.min(MIN_H, height - 16), Math.min(MAX_H, height - 16));
            px = (width - pw) / 2; py = (height - ph) / 2;
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (resizing) {
            resizing = false;
            MystUiConfig.width = pw; MystUiConfig.height = ph; MystUiConfig.save();
            rebuild(); return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= contentLeft() && mouseX <= contentRight() && mouseY >= contentTop() && mouseY <= contentBottom()) {
            scrollOffset -= (int)(verticalAmount * 32);
            clampScroll();
            if (page == Page.STREAMER) rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public boolean shouldPause() { return false; }
}