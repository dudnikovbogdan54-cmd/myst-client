package dev.myst.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public final class MystScreen extends Screen {
    private enum Page {
        HUD("HUD"), VISUALS("Visuals"), EFFECTS("Effects"), MARKS("Marks"),
        UTILITIES("Utilities"), STREAMER("Streamer"), CONFIGS("Configs");
        final String label;
        Page(String label) { this.label = label; }
    }

    private Page page = Page.HUD;
    private TextFieldWidget original, alias;
    private int px, py, pw, ph, sw;
    private static final int PANEL = 0xB0141021, CARD = 0xE91D172B;
    private static final int PURPLE = 0xFFB58AFF, DARK = 0xFF7042C8;
    private static final int WHITE = 0xFFF2ECFF, MUTED = 0xFF9D91AD;

    public MystScreen() { super(Text.literal("Mist Visuals")); }

    @Override
    protected void init() {
        pw = Math.min(980, width - 24);
        ph = Math.min(590, height - 24);
        px = (width - pw) / 2; py = (height - ph) / 2;
        sw = Math.max(132, Math.min(184, pw / 5));
        original = null; alias = null;
        if (page == Page.STREAMER) {
            int x = px + sw + 30, y = py + 242, w = Math.min(360, pw - sw - 60);
            original = new TextFieldWidget(textRenderer, x, y, w, 22, Text.literal("Original nickname"));
            original.setPlaceholder(Text.literal("Настоящий ник"));
            alias = new TextFieldWidget(textRenderer, x, y + 38, w, 22, Text.literal("Display nickname"));
            alias.setPlaceholder(Text.literal("Как показывать"));
            addDrawableChild(original); addDrawableChild(alias);
        }
    }

    private void select(Page next) {
        if (page != next) { page = next; clearChildren(); init(); }
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xD9080610);
        c.fill(px, py, px + pw, py + ph, PANEL);
        c.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("mystclient", "textures/gui/mist_background.png"), px, py, 0.0F, 0.0F, pw, ph, 2048, 2048, 0x70FFFFFF);
        c.fill(px, py, px + sw, py + ph, 0xF0100C1B);
        c.fill(px + sw - 1, py, px + sw, py + ph, 0xFF30243D);
        c.fill(px + sw, py + 62, px + pw, py + 63, 0xFF30243D);

        c.fill(px + 20, py + 18, px + 56, py + 54, DARK);
        c.drawCenteredTextWithShadow(textRenderer, "MV", px + 38, py + 31, WHITE);
        c.drawTextWithShadow(textRenderer, "MIST", px + 66, py + 22, WHITE);
        c.drawTextWithShadow(textRenderer, "VISUALS", px + 66, py + 36, PURPLE);
        c.drawTextWithShadow(textRenderer, "CLIENT MODULES", px + 20, py + 76, MUTED);

        int navY = py + 100;
        for (Page p : Page.values()) {
            int y = navY + p.ordinal() * 42;
            boolean active = p == page;
            if (active) {
                c.fill(px + 10, y - 4, px + sw - 10, y + 27, 0xFF392653);
                c.fill(px + 10, y - 4, px + 13, y + 27, PURPLE);
            }
            c.drawTextWithShadow(textRenderer, p.label, px + 23, y + 6, active ? WHITE : MUTED);
        }

        c.drawTextWithShadow(textRenderer, "MIST VISUALS  /  " + page.label.toUpperCase(),
                px + sw + 26, py + 22, WHITE);
        c.drawTextWithShadow(textRenderer, "MV  •  CLIENT SETTINGS", px + sw + 26, py + 40, MUTED);
        c.fill(px + pw - 44, py + 16, px + pw - 16, py + 44, 0xFF2A2037);
        c.drawCenteredTextWithShadow(textRenderer, "×", px + pw - 30, py + 24, WHITE);

        drawPage(c);
        super.render(c, mx, my, delta);
        c.drawTextWithShadow(textRenderer, "MIST VISUALS  •  1.21.11", px + 18, py + ph - 20, 0xFF756A82);
        c.drawTextWithShadow(textRenderer, "OWNER  ALOXMYST_YT", px + sw + 26, py + ph - 20, 0xFF756A82);
    }

    private void drawPage(DrawContext c) {
        int x = px + sw + 26, y = py + 82, gap = 14;
        int available = pw - sw - 52, cw = (available - gap) / 2;
        switch (page) {
            case HUD -> {
                heading(c, x, y, "HUD", "Экранные элементы — включай нужные и отключай лишние");
                feature(c,x,y+48,cw,"Watermark","Логотип Mist Visuals","watermark");
                feature(c,x+cw+gap,y+48,cw,"FPS Counter","Текущая частота кадров","fps");
                feature(c,x,y+142,cw,"Coordinates","Координаты XYZ игрока","coords");
                feature(c,x+cw+gap,y+142,cw,"Ping Display","Задержка соединения","ping");
                feature(c,x,y+236,cw,"Target HUD","Здоровье существа под прицелом","target_hud");
                feature(c,x+cw+gap,y+236,cw,"Block Info","Название блока под прицелом","block_info");
            }
            case VISUALS -> {
                heading(c,x,y,"VISUALS","Визуальные настройки мира и интерфейса");
                feature(c,x,y+48,cw,"Block Overlay","Подсветка выбранного блока","block_overlay");
                feature(c,x+cw+gap,y+48,cw,"Custom Crosshair","Настраиваемый прицел","crosshair");
                feature(c,x,y+142,cw,"Fullbright","Повышенная яркость мира","fullbright");
                feature(c,x+cw+gap,y+142,cw,"Clear Weather FX","Снижение визуального шума погоды","weather_fx");
            }
            case EFFECTS -> {
                heading(c,x,y,"EFFECTS","Локальные визуальные эффекты событий");
                feature(c,x,y+48,cw,"Kill Effect","Декоративный эффект после победы","kill_effect");
                feature(c,x+cw+gap,y+48,cw,"Hit Particles","Настройка декоративных частиц","hit_particles");
                feature(c,x,y+142,cw,"Totem Pop FX","Оформление эффекта тотема","totem_fx");
                feature(c,x+cw+gap,y+142,cw,"Animations","Плавность интерфейсных анимаций","animations");
            }
            case MARKS -> {
                heading(c,x,y,"MARKS","Личные метки и точки интереса");
                feature(c,x,y+48,cw,"Waypoints","ЛКМ: включить • ПКМ: список и настройки меток","waypoints");
                feature(c,x+cw+gap,y+48,cw,"Death Marker","Личная метка места смерти","death_marker");
            }
            case UTILITIES -> {
                heading(c,x,y,"UTILITIES","Инструменты комфорта");
                feature(c,x,y+48,cw,"Zoom","Приближение при удержании C","zoom");
                feature(c,x+cw+gap,y+48,cw,"Keystrokes","Отображение нажатий клавиш","keystrokes");
                feature(c,x,y+142,cw,"CPS Counter","Счётчик кликов","cps");
            }
            case STREAMER -> {
                heading(c,x,y,"STREAMER MODE","Локальная приватность отображения");
                feature(c,x,y+48,Math.min(430,available),"Streamer Mode","Замена ников в TAB только на этом клиенте","streamer");
                c.drawTextWithShadow(textRenderer,"НАСТОЯЩИЙ НИК",x,y+145,MUTED);
                c.drawTextWithShadow(textRenderer,"КАК ПОКАЗЫВАТЬ",x,y+184,MUTED);
                c.fill(x,y+228,x+150,y+258,DARK);
                c.drawCenteredTextWithShadow(textRenderer,"СОХРАНИТЬ НИК",x+75,y+238,WHITE);
                c.drawTextWithShadow(textRenderer,"Замены не меняют аккаунты и серверные ники.",x,y+278,MUTED);
            }
            case CONFIGS -> {
                heading(c,x,y,"CONFIGS","Профили и сохранение настроек");
                feature(c,x,y+48,cw,"Default Profile","Базовый набор настроек","default_profile");
                feature(c,x+cw+gap,y+48,cw,"Auto Save","Автоматическое сохранение модулей","auto_save");
                c.drawTextWithShadow(textRenderer,"Конфигурация хранится локально в папке config.",x,y+160,MUTED);
            }
        }
    }

    private void heading(DrawContext c,int x,int y,String title,String subtitle) {
        c.drawTextWithShadow(textRenderer,title,x,y,PURPLE);
        c.drawTextWithShadow(textRenderer,subtitle,x,y+18,MUTED);
    }

    private void feature(DrawContext c,int x,int y,int w,String title,String desc,String id) {
        boolean implemented = id.equals("streamer") || id.equals("watermark") || id.equals("fps")
                || id.equals("coords") || id.equals("ping") || id.equals("target_hud") || id.equals("block_info")
                || id.equals("crosshair") || id.equals("keystrokes") || id.equals("kill_effect") || id.equals("zoom") || id.equals("fullbright") || id.equals("waypoints");
        boolean on = id.equals("streamer") ? StreamerConfig.enabled : MistConfig.enabled(id);
        c.fill(x,y,x+w,y+78,CARD);
        c.fill(x,y,x+3,y+78,0xFF594078);
        c.drawTextWithShadow(textRenderer,title,x+14,y+14,WHITE);
        c.drawTextWithShadow(textRenderer,desc,x+14,y+36,MUTED);
        if (implemented) {
            int sx=x+w-58;
            c.fill(sx,y+12,sx+42,y+32,on?0xFF8255C5:0xFF403849);
            c.fill(on?sx+23:sx+3,y+14,on?sx+39:sx+19,y+30,WHITE);
            c.drawTextWithShadow(textRenderer,on?"ON":"OFF",sx-28,y+18,on?PURPLE:MUTED);
        } else {
            c.drawTextWithShadow(textRenderer,"SOON",x+w-48,y+18,MUTED);
        }
    }

    private void toggleAt(double mx,double my,int x,int y,int w,String id) {
        boolean implemented = id.equals("streamer") || id.equals("watermark") || id.equals("fps")
                || id.equals("coords") || id.equals("ping") || id.equals("target_hud") || id.equals("block_info")
                || id.equals("crosshair") || id.equals("keystrokes") || id.equals("kill_effect") || id.equals("zoom") || id.equals("fullbright") || id.equals("waypoints");
        if (!implemented) return;
        if (mx>=x && mx<=x+w && my>=y && my<=y+78) {
            if (id.equals("streamer")) { StreamerConfig.enabled=!StreamerConfig.enabled; StreamerConfig.save(); }
            else MistConfig.toggle(id);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (page == Page.STREAMER && click.button() == 0 && original != null && alias != null) {
            int fieldX = px + sw + 30;
            int fieldY = py + 242;
            int fieldW = Math.min(360, pw - sw - 60);
            if ((mx >= fieldX && mx <= fieldX + fieldW && my >= fieldY && my <= fieldY + 22)
                    || (mx >= fieldX && mx <= fieldX + fieldW && my >= fieldY + 38 && my <= fieldY + 60)) {
                return super.mouseClicked(click, doubled);
            }
        }
        if (click.button()==1 && page == Page.MARKS) {
            int x=px+sw+26, y=py+82, available=pw-sw-52, cw=(available-14)/2;
            if (mx>=x && mx<=x+cw && my>=y+48 && my<=y+126) {
                client.setScreen(new WaypointScreen(this));
                return true;
            }
        }
        if (click.button()==0) {
            int navY=py+100;
            for(Page p:Page.values()) {
                int y=navY+p.ordinal()*42;
                if(mx>=px+8&&mx<=px+sw-8&&my>=y-5&&my<=y+28){select(p);return true;}
            }
            if(mx>=px+pw-48&&mx<=px+pw-12&&my>=py+12&&my<=py+48){close();return true;}
            int x=px+sw+26,y=py+82,gap=14,available=pw-sw-52,cw=(available-gap)/2;
            switch(page) {
                case HUD -> {
                    toggleAt(mx,my,x,y+48,cw,"watermark"); toggleAt(mx,my,x+cw+gap,y+48,cw,"fps");
                    toggleAt(mx,my,x,y+142,cw,"coords"); toggleAt(mx,my,x+cw+gap,y+142,cw,"ping");
                    toggleAt(mx,my,x,y+236,cw,"target_hud"); toggleAt(mx,my,x+cw+gap,y+236,cw,"block_info");
                }
                case VISUALS -> {
                    toggleAt(mx,my,x,y+48,cw,"block_overlay"); toggleAt(mx,my,x+cw+gap,y+48,cw,"crosshair");
                    toggleAt(mx,my,x,y+142,cw,"fullbright"); toggleAt(mx,my,x+cw+gap,y+142,cw,"weather_fx");
                }
                case EFFECTS -> {
                    toggleAt(mx,my,x,y+48,cw,"kill_effect"); toggleAt(mx,my,x+cw+gap,y+48,cw,"hit_particles");
                    toggleAt(mx,my,x,y+142,cw,"totem_fx"); toggleAt(mx,my,x+cw+gap,y+142,cw,"animations");
                }
                case MARKS -> { toggleAt(mx,my,x,y+48,cw,"waypoints"); toggleAt(mx,my,x+cw+gap,y+48,cw,"death_marker"); }
                case UTILITIES -> {
                    toggleAt(mx,my,x,y+48,cw,"zoom"); toggleAt(mx,my,x+cw+gap,y+48,cw,"keystrokes");
                    toggleAt(mx,my,x,y+142,cw,"cps");
                }
                case STREAMER -> {
                    toggleAt(mx,my,x,y+48,Math.min(430,available),"streamer");
                    if(mx>=x&&mx<=x+150&&my>=y+228&&my<=y+258&&original!=null&&alias!=null) {
                        String from=original.getText().trim(),to=alias.getText().trim();
                        if(!from.isEmpty()&&!to.isEmpty()){StreamerConfig.aliases.put(from,to);StreamerConfig.save();original.setText("");alias.setText("");}
                    }
                }
                case CONFIGS -> { toggleAt(mx,my,x,y+48,cw,"default_profile"); toggleAt(mx,my,x+cw+gap,y+48,cw,"auto_save"); }
            }
            return true;
        }
        return super.mouseClicked(click,doubled);
    }

    @Override public boolean shouldPause(){return false;}
}
