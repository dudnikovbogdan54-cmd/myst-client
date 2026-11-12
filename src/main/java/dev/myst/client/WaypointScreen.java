package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class WaypointScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField, xField, yField, zField;
    private int selected = -1;
    private boolean showDistance = true;
    private int selectedColor = 0xFFB58AFF;
    private static final int[] COLORS = {0xFFB58AFF, 0xFF55D6FF, 0xFF65E39A, 0xFFFFD166, 0xFFFF7B9C, 0xFFFFFFFF};
    private static final int PANEL = 0xF0141021, CARD = 0xE91D172B;
    private static final int PURPLE = 0xFFB58AFF, WHITE = 0xFFF2ECFF, MUTED = 0xFF9D91AD;
    private int left, top, widthPanel;

    public WaypointScreen(Screen parent) {
        super(Text.literal("Myst Visuals — Waypoints"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        widthPanel = Math.min(620, width - 24);
        left = (width - widthPanel) / 2;
        top = Math.max(12, (height - 454) / 2);
        int fx = left + 22, fw = Math.min(260, widthPanel - 44);
        nameField = new TextFieldWidget(textRenderer, fx, top + 83, fw, 20, Text.literal("Название"));
        nameField.setMaxLength(32);
        xField = new TextFieldWidget(textRenderer, fx, top + 132, 76, 20, Text.literal("X"));
        yField = new TextFieldWidget(textRenderer, fx + 88, top + 132, 76, 20, Text.literal("Y"));
        zField = new TextFieldWidget(textRenderer, fx + 176, top + 132, 76, 20, Text.literal("Z"));
        addDrawableChild(nameField);
        addDrawableChild(xField);
        addDrawableChild(yField);
        addDrawableChild(zField);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xD9080610);
        c.fill(left, top, left + widthPanel, top + 454, PANEL);
        c.drawTextWithShadow(textRenderer, "WAYPOINT MANAGER", left + 22, top + 18, PURPLE);
        c.drawTextWithShadow(textRenderer, "Создавай метки по точным координатам", left + 22, top + 38, MUTED);
        c.drawTextWithShadow(textRenderer, "НАЗВАНИЕ", left + 22, top + 67, MUTED);
        c.drawTextWithShadow(textRenderer, "КООРДИНАТЫ X / Y / Z", left + 22, top + 116, MUTED);
        c.drawTextWithShadow(textRenderer, "Расстояние: " + (showDistance ? "ВКЛ" : "ВЫКЛ") + "  (нажми, чтобы переключить)",
                left + 22, top + 166, showDistance ? PURPLE : MUTED);
        c.drawTextWithShadow(textRenderer, "Цвет: " + colorName(selectedColor) + "  (нажми, чтобы сменить)",
                left + 22, top + 188, selectedColor);
        drawButton(c, left + 22, top + 210, 132, "СОХРАНИТЬ / ДОБАВИТЬ");
        drawButton(c, left + 164, top + 210, 122, "ТЕКУЩАЯ ТОЧКА");
        drawButton(c, left + 296, top + 210, 86, "НОВАЯ");
        c.drawTextWithShadow(textRenderer, "ТВОИ МЕТКИ  •  ЛКМ — ВКЛ/ВЫКЛ, ПКМ — ИЗМЕНИТЬ",
                left + 22, top + 252, MUTED);
        int rowY = top + 274;
        for (int i = 0; i < MistConfig.waypoints.size() && i < 6; i++) {
            MistConfig.Waypoint p = MistConfig.waypoints.get(i);
            int y = rowY + i * 25;
            c.fill(left + 20, y, left + widthPanel - 20, y + 21, i == selected ? 0xFF392653 : CARD);
            c.drawTextWithShadow(textRenderer, (p.enabled ? "● " : "○ ") + p.name,
                    left + 28, y + 6, p.enabled ? p.color : MUTED);
            String coords = (int)p.x + " " + (int)p.y + " " + (int)p.z
                    + (p.showDistance ? "  •  дистанция ВКЛ" : "  •  дистанция ВЫКЛ");
            c.drawTextWithShadow(textRenderer, coords, left + Math.min(245, widthPanel / 2), y + 6, MUTED);
        }
        drawButton(c, left + 22, top + 430, 112, "УДАЛИТЬ");
        drawButton(c, left + widthPanel - 132, top + 430, 110, "ГОТОВО");
        super.render(c, mx, my, delta);
    }

    private void drawButton(DrawContext c, int x, int y, int w, String label) {
        c.fill(x, y, x + w, y + 24, 0xFF493365);
        c.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + 8, WHITE);
    }

    private String colorName(int color) {
        return switch (color) {
            case 0xFF55D6FF -> "Голубой";
            case 0xFF65E39A -> "Зелёный";
            case 0xFFFFD166 -> "Жёлтый";
            case 0xFFFF7B9C -> "Розовый";
            case 0xFFFFFFFF -> "Белый";
            default -> "Фиолетовый";
        };
    }

    private void loadSelected() {
        if (selected < 0 || selected >= MistConfig.waypoints.size()) return;
        MistConfig.Waypoint p = MistConfig.waypoints.get(selected);
        nameField.setText(p.name);
        xField.setText(Double.toString(p.x));
        yField.setText(Double.toString(p.y));
        zField.setText(Double.toString(p.z));
        showDistance = p.showDistance;
        selectedColor = p.color;
    }

    private void savePoint() {
        try {
            String name = nameField.getText().trim();
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            double z = Double.parseDouble(zField.getText().trim());
            if (selected >= 0 && selected < MistConfig.waypoints.size()) {
                MistConfig.Waypoint p = MistConfig.waypoints.get(selected);
                p.name = name.isEmpty() ? p.name : name;
                p.x = x;
                p.y = y;
                p.z = z;
                p.showDistance = showDistance;
                p.color = selectedColor;
                p.enabled = true;
            } else {
                MistConfig.addWaypoint(name, x, y, z);
                selected = MistConfig.waypoints.size() - 1;
                MistConfig.waypoints.get(selected).showDistance = showDistance;
                MistConfig.waypoints.get(selected).color = selectedColor;
            }
            MistConfig.save();
        } catch (NumberFormatException ignored) {
            // Invalid coordinate input is left in the fields so it can be corrected.
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() == 0 && mx >= left + 22 && mx <= left + widthPanel - 22) {
            if (my >= top + 166 && my <= top + 185) {
                showDistance = !showDistance;
                return true;
            }
            if (my >= top + 186 && my <= top + 206) {
                int next = 0;
                for (int i = 0; i < COLORS.length; i++) {
                    if (COLORS[i] == selectedColor) next = (i + 1) % COLORS.length;
                }
                selectedColor = COLORS[next];
                return true;
            }
            if (my >= top + 210 && my <= top + 234) {
                if (mx <= left + 154) {
                    savePoint();
                    return true;
                }
                if (mx <= left + 286) {
                    var player = net.minecraft.client.MinecraftClient.getInstance().player;
                    if (player != null) {
                        xField.setText(Double.toString(player.getX()));
                        yField.setText(Double.toString(player.getY()));
                        zField.setText(Double.toString(player.getZ()));
                        if (nameField.getText().isBlank()) nameField.setText("Текущая точка");
                    }
                    return true;
                }
                selected = -1;
                nameField.setText("");
                xField.setText("");
                yField.setText("");
                zField.setText("");
                showDistance = true;
                selectedColor = PURPLE;
                return true;
            }
            if (my >= top + 274 && my < top + 424) {
                int index = (int)((my - (top + 274)) / 25);
                if (index >= 0 && index < MistConfig.waypoints.size()) {
                    selected = index;
                    MistConfig.Waypoint p = MistConfig.waypoints.get(index);
                    p.enabled = !p.enabled;
                    MistConfig.save();
                    loadSelected();
                    return true;
                }
            }
            if (my >= top + 430 && my <= top + 454) {
                if (mx < left + 145 && selected >= 0 && selected < MistConfig.waypoints.size()) {
                    MistConfig.waypoints.remove(selected);
                    selected = -1;
                    MistConfig.save();
                    nameField.setText("");
                    xField.setText("");
                    yField.setText("");
                    zField.setText("");
                } else {
                    close();
                }
                return true;
            }
        }
        if (click.button() == 1 && my >= top + 274 && my < top + 424) {
            int index = (int)((my - (top + 274)) / 25);
            if (index >= 0 && index < MistConfig.waypoints.size()) {
                selected = index;
                loadSelected();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
