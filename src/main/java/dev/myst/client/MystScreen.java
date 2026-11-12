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
        VISUALS("Visuals", "Визуальные настройки"),
        EFFECTS("Effects", "Эффекты интерфейса"),
        MARKS("Marks", "Метки и точки"),
        UTILITIES("Utilities", "Инструменты"),
        STREAMER("Streamer", "Режим приватности"),
        CONFIGS("Settings", "Размер и оформление меню");

        final String label, subtitle;
        Page(String label, String subtitle) { this.label = label; this.subtitle = subtitle; }
    }

    private record ModuleCard(String id, String title, String description, boolean implemented) {}

    private Page page = Page.HUD;
    private TextFieldWidget original, alias;
    private int px, py, pw, ph, sw;
    private int scrollOffset = 0;
    private int contentHeight = 0;
    private boolean resizing = false;
    private int dragStartX, dragStartY, startWidth, startHeight;

    private static final int MIN_W = 520, MIN_H = 330;
    private static final int MAX_W = 1160, MAX_H = 760;
    private static final int PANEL = 0xF0110D1B, SIDEBAR = 0xF00D0A15;
    private static final int CARD = 0xF01D1728, CARD_HOVER = 0xFF292039;
    private int accent() { return MystUiConfig.accent(); }
    private int accentDark() { return 0xFF8152C5; }
    private static final int WHITE = 0xFFF4F0FA, MUTED = 0xFF9D91AD;

    public MystScreen() { super(Text.literal("Myst Visuals")); }

    @Override
    protected void init() {
        pw = Math.max(Math.min(MIN_W, width - 16), Math.min(pw == 0 ? MystUiConfig.width : pw, Math.min(MAX_W, width - 16)));
        ph = Math.max(Math.min(MIN_H, height - 16), Math.min(ph == 0 ? MystUiConfig.height : ph, Math.min(MAX_H, height - 16)));
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        sw = Math.max(126, Math.min(150, pw / 5));
        clearChildren();
        original = null;
        alias = null;
        if (page == Page.STREAMER) {
            int x = px + sw + 26;
            int y = py + 264 - scrollOffset;
            int w = Math.min(340, pw - sw - 52);
            original = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal("Original nickname"));
            original.setPlaceholder(Text.literal("Настоящий ник"));
            alias = new TextFieldWidget(textRenderer, x, y + 34, w, 20, Text.literal("Display nickname"));
            alias.setPlaceholder(Text.literal("Как показывать"));
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
                list.add(new ModuleCard("watermark","Watermark","Логотип Myst Visuals",true));
                list.add(new ModuleCard("fps","FPS Counter","Частота кадров",true));
                list.add(new ModuleCard("coords","Coordinates","Координаты XYZ",true));
                list.add(new ModuleCard("ping","Ping Display","Задержка соединения",true));
                list.add(new ModuleCard("target_hud","Target HUD","Информация о цели",true));
                list.add(new ModuleCard("block_info","Block Info","Блок под прицелом",true));
                list.add(new ModuleCard("keystrokes","Keystrokes","Отображение нажатий",true));
                list.add(new ModuleCard("cps","CPS Counter","Счётчик кликов",false));
            }
            case VISUALS -> {
                list.add(new ModuleCard("fullbright","Fullbright","Повышенная яркость мира",true));
                list.add(new ModuleCard("crosshair","Custom Crosshair","Настраиваемый прицел",true));
                list.add(new ModuleCard("block_overlay","Block Overlay","Подсветка выбранного блока",false));
                list.add(new ModuleCard("weather_fx","Weather FX","Визуальные эффекты погоды",false));
                list.add(new ModuleCard("zoom","Zoom","Плавное приближение",true));
            }
            case EFFECTS -> {
                list.add(new ModuleCard("kill_effect","Kill Effect","Декоративный эффект события",true));
                list.add(new ModuleCard("hit_particles","Hit Particles","Настройка частиц",false));
                list.add(new ModuleCard("totem_fx","Totem Pop FX","Эффект активации тотема",false));
                list.add(new ModuleCard("animations","Animations","Анимации интерфейса",false));
            }
            case MARKS -> {
                list.add(new ModuleCard("waypoints","Waypoints","Метки с координатами и дистанцией",true));
                list.add(new ModuleCard("death_marker","Death Marker","Личная метка последней точки",false));
            }
            case UTILITIES -> {
                list.add(new ModuleCard("zoom","Zoom","Приближение при удержании C",true));
                list.add(new ModuleCard("keystrokes","Keystrokes","Визуализация клавиш",true));
                list.add(new ModuleCard("cps","CPS Counter","Счётчик кликов",false));
            }
            case STREAMER -> {
                list.add(new ModuleCard("streamer","Streamer Mode","Локальная замена ников в TAB",true));
            }
            case CONFIGS -> {
                list.add(new ModuleCard("default_profile","Default Profile","Базовый профиль",false));
                list.add(new ModuleCard("auto_save","Auto Save","Автосохранение настроек",false));
            }
        }
        return list;
    }

    private boolean isEnabled(String id) {
        return id.equals("streamer") ? StreamerConfig.enabled : MistConfig.enabled(id);
    }

    private void toggle(String id) {
        if (id.equals("streamer")) {
            StreamerConfig.enabled = !StreamerConfig.enabled;
            StreamerConfig.save();
        } else {
            MistConfig.toggle(id);
        }
    }

    private void clampScroll() {
        int columns = (pw - sw - 68) < 340 ? 1 : 2;
        int rows = (cards().size() + columns - 1) / columns;
        contentHeight = 40 + rows * ((MystUiConfig.compact ? 64 : 78) + 12);
        int viewport = Math.max(100, ph - 120);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, contentHeight - viewport)));
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC9080610);
        c.fill(px - 5, py - 5, px + pw + 5, py + ph + 5, 0x663D2B56);
        c.fill(px, py, px + pw, py + ph, PANEL);
        c.fill(px, py, px + sw, py + ph, SIDEBAR);
        c.fill(px + sw - 1, py, px + sw, py + ph, 0xFF352844);
        c.fill(px + sw, py + 66, px + pw, py + 67, 0xFF352844);

        c.fill(px + 18, py + 17, px + 52, py + 51, accentDark());
        c.drawCenteredTextWithShadow(textRenderer, "M", px + 35, py + 28, WHITE);
        c.drawTextWithShadow(textRenderer, "MYST", px + 62, py + 19, WHITE);
        c.drawTextWithShadow(textRenderer, "VISUALS", px + 62, py + 34, accent());
        c.drawTextWithShadow(textRenderer, "CLIENT MENU", px + 18, py + 75, MUTED);

        int navY = py + 88;
        for (Page p : Page.values()) {
            int y = navY + p.ordinal() * 33;
            boolean active = p == page;
            if (active) {
                c.fill(px + 9, y - 4, px + sw - 9, y + 25, 0xFF332445);
                c.fill(px + 9, y - 4, px + 12, y + 25, accent());
            }
            c.drawTextWithShadow(textRenderer, p.label, px + 21, y + 5, active ? WHITE : MUTED);
        }

        int headX = px + sw + 24;
        c.drawTextWithShadow(textRenderer, page.label.toUpperCase(), headX, py + 19, WHITE);
        c.drawTextWithShadow(textRenderer, page.subtitle, headX, py + 37, MUTED);
        drawSmallButton(c, px + pw - 112, py + 17, 26, "−");
        drawSmallButton(c, px + pw - 80, py + 17, 26, "+");
        drawSmallButton(c, px + pw - 43, py + 17, 26, "×");
        c.drawTextWithShadow(textRenderer, pw + " × " + ph, px + pw - 112, py + 48, MUTED);

        int contentTop = py + 74;
        int contentBottom = py + ph - 34;
        int contentLeft = px + sw + 24;
        int contentRight = px + pw - 20;
        c.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
        List<ModuleCard> list = cards();
        int columns = (contentRight - contentLeft) < 340 ? 1 : 2;
        int gap = 10;
        int cardHeight = MystUiConfig.compact ? 64 : 78;
        int rowStep = cardHeight + 12;
        int cw = Math.max(120, (contentRight - contentLeft - gap * (columns - 1)) / columns);
        int row = 0;
        for (int i = 0; i < list.size(); i++) {
            ModuleCard card = list.get(i);
            int col = i % columns;
            row = i / columns;
            int x = contentLeft + col * (cw + gap);
            int y = contentTop + 4 + row * rowStep - scrollOffset;
            boolean hovered = mx >= x && mx <= x + cw && my >= y && my <= y + cardHeight;
            c.fill(x, y, x + cw, y + cardHeight, hovered ? CARD_HOVER : CARD);
            c.fill(x, y, x + 3, y + cardHeight, card.implemented ? accentDark() : 0xFF51465D);
            c.drawTextWithShadow(textRenderer, card.title, x + 13, y + 10, WHITE);
            c.drawTextWithShadow(textRenderer, card.description, x + 13, y + 27, MUTED);
            if (card.implemented) {
                boolean enabled = isEnabled(card.id);
                int sx = x + cw - 54;
                c.fill(sx, y + cardHeight - 22, sx + 40, y + cardHeight - 5, enabled ? 0xFF7145A7 : 0xFF403849);
                c.fill(enabled ? sx + 22 : sx + 2, y + cardHeight - 20, enabled ? sx + 38 : sx + 18, y + cardHeight - 7, WHITE);
                c.drawTextWithShadow(textRenderer, enabled ? "ON" : "OFF", sx - 27, y + cardHeight - 18, enabled ? accent() : MUTED);
            } else {
                c.drawTextWithShadow(textRenderer, "SOON", x + cw - 45, y + cardHeight - 18, MUTED);
            }
        }
        if (page == Page.STREAMER) {
            c.drawTextWithShadow(textRenderer, "НАСТОЯЩИЙ НИК", contentLeft, contentTop + 150 - scrollOffset, MUTED);
            c.drawTextWithShadow(textRenderer, "КАК ПОКАЗЫВАТЬ", contentLeft, contentTop + 205 - scrollOffset, MUTED);
            drawSmallButton(c, contentLeft, contentTop + 258 - scrollOffset, 142, "СОХРАНИТЬ НИК");
            c.drawTextWithShadow(textRenderer, "Замена действует только локально.", contentLeft + 154, contentTop + 266 - scrollOffset, MUTED);
        }
        if (page == Page.CONFIGS) {
            int sy = contentTop + 20;
            c.drawTextWithShadow(textRenderer, "ОФОРМЛЕНИЕ", contentLeft, sy, WHITE);
            drawSmallButton(c, contentLeft, sy + 18, 190, "ЦВЕТ АКЦЕНТА: " + (MystUiConfig.theme + 1));
            c.fill(contentLeft + 202, sy + 18, contentLeft + 238, sy + 43, accent());
            c.drawTextWithShadow(textRenderer, "ПЛОТНОСТЬ КАРТОЧЕК", contentLeft, sy + 62, WHITE);
            drawSmallButton(c, contentLeft, sy + 80, 190, MystUiConfig.compact ? "КОМПАКТНО: ВКЛ" : "КОМПАКТНО: ВЫКЛ");
            c.drawTextWithShadow(textRenderer, "РАЗМЕР ОКНА", contentLeft, sy + 124, WHITE);
            drawSmallButton(c, contentLeft, sy + 142, 90, "МЕНЬШЕ");
            drawSmallButton(c, contentLeft + 100, sy + 142, 90, "БОЛЬШЕ");
            drawSmallButton(c, contentLeft + 200, sy + 142, 130, "СБРОСИТЬ");
            c.drawTextWithShadow(textRenderer, "Размер: " + pw + " × " + ph, contentLeft, sy + 184, MUTED);
            c.drawTextWithShadow(textRenderer, "Можно также тянуть за правый нижний угол.", contentLeft, sy + 205, MUTED);
        }
        c.disableScissor();
        int viewportHeight = contentBottom - contentTop;
        if (contentHeight > viewportHeight) {
            int trackX = contentRight - 5;
            c.fill(trackX, contentTop + 3, trackX + 3, contentBottom - 3, 0xFF30283A);
            int thumbHeight = Math.max(24, viewportHeight * viewportHeight / contentHeight);
            int maxScroll = Math.max(1, contentHeight - viewportHeight);
            int thumbY = contentTop + 3 + (viewportHeight - thumbHeight) * scrollOffset / maxScroll;
            c.fill(trackX - 1, thumbY, trackX + 4, thumbY + thumbHeight, accent());
        }

        c.drawTextWithShadow(textRenderer, "КОЛЁСИКО — ПРОКРУТКА", px + 18, py + ph - 20, 0xFF756A82);
        c.drawTextWithShadow(textRenderer, "MYST VISUALS  •  1.21.11", contentLeft, py + ph - 20, 0xFF756A82);
        c.fill(px + pw - 16, py + ph - 16, px + pw - 5, py + ph - 5, accent());
        super.render(c, mx, my, delta);
    }

    private void drawSmallButton(DrawContext c, int x, int y, int w, String label) {
        c.fill(x, y, x + w, y + 25, 0xFF382A4A);
        c.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 8, WHITE);
    }

    private void rebuild() {
        init();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() == 0 && mx >= px + pw - 20 && my >= py + ph - 20) {
            resizing = true;
            dragStartX = (int) mx;
            dragStartY = (int) my;
            startWidth = pw;
            startHeight = ph;
            return true;
        }
        if (click.button() == 0 && my >= py + 12 && my <= py + 48) {
            if (mx >= px + pw - 114 && mx < px + pw - 84) {
                pw = Math.max(Math.min(MIN_W, width - 16), pw - 55);
                ph = Math.max(Math.min(MIN_H, height - 16), ph - 35);
                MystUiConfig.width = pw; MystUiConfig.height = ph; MystUiConfig.save();
                rebuild();
                return true;
            }
            if (mx >= px + pw - 82 && mx < px + pw - 52) {
                pw = Math.min(Math.min(MAX_W, width - 16), pw + 55);
                ph = Math.min(Math.min(MAX_H, height - 16), ph + 35);
                MystUiConfig.width = pw; MystUiConfig.height = ph; MystUiConfig.save();
                rebuild();
                return true;
            }
            if (mx >= px + pw - 46) { close(); return true; }
        }
        if (click.button() == 0 && page == Page.CONFIGS) {
            int contentLeft = px + sw + 24, sy = py + 74 + 20;
            if (mx >= contentLeft && mx <= contentLeft + 190 && my >= sy + 18 && my <= sy + 43) {
                MystUiConfig.theme = (MystUiConfig.theme + 1) % MystUiConfig.ACCENTS.length;
                MystUiConfig.save();
                return true;
            }
            if (mx >= contentLeft && mx <= contentLeft + 190 && my >= sy + 80 && my <= sy + 105) {
                MystUiConfig.compact = !MystUiConfig.compact;
                MystUiConfig.save();
                clampScroll();
                return true;
            }
            if (my >= sy + 142 && my <= sy + 167) {
                if (mx <= contentLeft + 95) {
                    pw = Math.max(MIN_W, pw - 40); ph = Math.max(MIN_H, ph - 25);
                } else if (mx <= contentLeft + 195) {
                    pw = Math.min(Math.min(MAX_W, width - 16), pw + 40); ph = Math.min(Math.min(MAX_H, height - 16), ph + 25);
                } else {
                    pw = Math.min(700, width - 16); ph = Math.min(430, height - 16);
                    MystUiConfig.theme = 0; MystUiConfig.compact = true;
                }
                MystUiConfig.width = pw; MystUiConfig.height = ph; MystUiConfig.save();
                rebuild();
                return true;
            }
        }
        if (click.button() == 0) {
            int navY = py + 88;
            for (Page p : Page.values()) {
                int y = navY + p.ordinal() * 33;
                if (mx >= px + 8 && mx <= px + sw - 8 && my >= y - 5 && my <= y + 28) {
                    select(p);
                    return true;
                }
            }
            int contentTop = py + 74, contentBottom = py + ph - 34;
            int contentLeft = px + sw + 24, contentRight = px + pw - 20;
            if (mx >= contentLeft && mx <= contentRight && my >= contentTop && my <= contentBottom) {
                List<ModuleCard> list = cards();
                int columns = (contentRight - contentLeft) < 340 ? 1 : 2;
                int gap = 10, cw = Math.max(120, (contentRight - contentLeft - gap * (columns - 1)) / columns);
                int cardHeight = MystUiConfig.compact ? 64 : 78;
                int rowStep = cardHeight + 12;
                for (int i = 0; i < list.size(); i++) {
                    ModuleCard card = list.get(i);
                    int col = i % columns, row = i / columns;
                    int x = contentLeft + col * (cw + gap);
                    int y = contentTop + 4 + row * rowStep - scrollOffset;
                    if (mx >= x && mx <= x + cw && my >= y && my <= y + cardHeight && card.implemented) {
                        if (card.id.equals("waypoints") && click.button() == 1) {
                            client.setScreen(new WaypointScreen(this));
                        } else {
                            toggle(card.id);
                        }
                        return true;
                    }
                }
                if (page == Page.STREAMER && original != null && alias != null) {
                    if (mx >= contentLeft && mx <= contentLeft + 142
                            && my >= contentTop + 258 - scrollOffset && my <= contentTop + 283 - scrollOffset) {
                        String from = original.getText().trim(), to = alias.getText().trim();
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
        }
        if (click.button() == 1 && page == Page.MARKS) {
            int contentLeft = px + sw + 24, contentTop = py + 74;
            int columns = (pw - sw - 68) < 340 ? 1 : 2;
            int cw = Math.max(120, (pw - sw - 68 - 10 * (columns - 1)) / columns);
            if (mx >= contentLeft && mx <= contentLeft + cw
                    && my >= contentTop + 4 - scrollOffset && my <= contentTop + (MystUiConfig.compact ? 68 : 82) - scrollOffset) {
                client.setScreen(new WaypointScreen(this));
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (resizing && click.button() == 0) {
            pw = Math.max(Math.min(MIN_W, width - 16), Math.min(Math.min(MAX_W, width - 16), startWidth + (int)(click.x() - dragStartX)));
            ph = Math.max(Math.min(MIN_H, height - 16), Math.min(Math.min(MAX_H, height - 16), startHeight + (int)(click.y() - dragStartY)));
            px = (width - pw) / 2;
            py = (height - ph) / 2;
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (resizing) {
            resizing = false;
            rebuild();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int left = px + sw + 24, right = px + pw - 20;
        int top = py + 82, bottom = py + ph - 38;
        if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
            scrollOffset -= (int)(verticalAmount * 34);
            clampScroll();
            if (page == Page.STREAMER) rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public boolean shouldPause() { return false; }
}
