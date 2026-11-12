package dev.myst.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class MistHud {
    private static final int PURPLE = 0xFFB58AFF;
    private static final int PANEL = 0xD91A1425;
    private static java.util.UUID trackedTarget;
    private static float previousHealth = -1.0F;
    private static long killEffectUntil;
    private static boolean lastAttackPressed, lastUsePressed;
    private static int leftClicks, rightClicks, shownLeftCps, shownRightCps;
    private static long cpsWindowStart = System.currentTimeMillis();
    private static float displayedTargetHealth = -1.0F;
    private MistHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        int y = 8;
        if (MistConfig.enabled("watermark")) {
            panel(ctx, 8, y, 116, 20);
            ctx.drawTextWithShadow(mc.textRenderer, "MV  Myst Visuals", 14, y + 6, PURPLE);
            y += 25;
        }
        if (MistConfig.enabled("fps")) {
            panel(ctx, 8, y, 100, 18);
            ctx.drawTextWithShadow(mc.textRenderer, "FPS  " + mc.getCurrentFps(), 14, y + 5, 0xFFF1E9FF);
            y += 22;
        }
        if (MistConfig.enabled("coords")) {
            panel(ctx, 8, y, 150, 30);
            ctx.drawTextWithShadow(mc.textRenderer, "X " + (int)mc.player.getX(), 14, y + 4, 0xFFF1E9FF);
            ctx.drawTextWithShadow(mc.textRenderer, "Y " + (int)mc.player.getY() + "   Z " + (int)mc.player.getZ(), 14, y + 16, 0xFFF1E9FF);
            y += 34;
        }
        if (MistConfig.enabled("ping")) {
            var entry = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                panel(ctx, 8, y, 100, 18);
                ctx.drawTextWithShadow(mc.textRenderer, "Ping  " + entry.getLatency() + " ms", 14, y + 5, 0xFFF1E9FF);
            }
        }
        if (MystVisualConfig.cpsEnabled) {
            String cpsText = MystVisualConfig.cpsShowBoth
                    ? "LMB " + shownLeftCps + "  |  RMB " + shownRightCps
                    : "LMB " + shownLeftCps;
            int cpsWidth = mc.textRenderer.getWidth(cpsText) + 14;
            panel(ctx, 8, y, cpsWidth, 20);
            ctx.drawTextWithShadow(mc.textRenderer, cpsText, 14, y + 6, 0xFFF1E9FF);
            y += 24;
        }
        if (MistConfig.enabled("target_hud") && mc.targetedEntity instanceof LivingEntity target && target.isAlive()) {
            int x = ctx.getScaledWindowWidth() / 2 + 12;
            int ty = ctx.getScaledWindowHeight() / 2 + 8;
            panel(ctx, x, ty, 138, 38);
            String targetName = target.getName().getString();
            if (MystVisualConfig.targetDistance) targetName += String.format(java.util.Locale.ROOT, "  %.1fm", mc.player.distanceTo(target));
            ctx.drawTextWithShadow(mc.textRenderer, targetName, x + 7, ty + 6, 0xFFF1E9FF);
            float max = Math.max(1.0F, target.getMaxHealth());
            float actual = Math.max(0.0F, Math.min(1.0F, target.getHealth() / max));
            if (displayedTargetHealth < 0) displayedTargetHealth = actual;
            displayedTargetHealth = MystVisualConfig.targetSmoothBar ? displayedTargetHealth + (actual - displayedTargetHealth) * 0.22F : actual;
            ctx.fill(x + 7, ty + 23, x + 131, ty + 29, 0xFF45384F);
            ctx.fill(x + 7, ty + 23, x + 7 + (int)(124 * displayedTargetHealth), ty + 29, MystUiConfig.accent());
            ctx.drawTextWithShadow(mc.textRenderer, String.format(java.util.Locale.ROOT, "%.1f / %.1f HP", target.getHealth(), max), x + 7, ty + 31, 0xFFD9CDEB);
        }
        if (MistConfig.enabled("block_info") && mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            String name = mc.world.getBlockState(hit.getBlockPos()).getBlock().getName().getString();
            int x = ctx.getScaledWindowWidth() / 2 + 12;
            int by = ctx.getScaledWindowHeight() / 2 + 54;
            panel(ctx, x, by, Math.min(190, mc.textRenderer.getWidth(name) + 18), 20);
            ctx.drawTextWithShadow(mc.textRenderer, name, x + 8, by + 6, 0xFFF1E9FF);
        }
        if (MistConfig.enabled("waypoints")) {
            int markY = 8;
            for (MistConfig.Waypoint point : MistConfig.waypoints) {
                if (!point.enabled) continue;
                double dx = point.x - mc.player.getX();
                double dy = point.y - mc.player.getY();
                double dz = point.z - mc.player.getZ();
                int distance = (int)Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
                String label = point.name + "  " + (int)point.x + " " + (int)point.y + " " + (int)point.z
                        + (point.showDistance ? "  (" + distance + "m)" : "");
                int w = mc.textRenderer.getWidth(label) + 16;
                int x = ctx.getScaledWindowWidth() - w - 8;
                panel(ctx, x, markY, w, 20);
                ctx.drawTextWithShadow(mc.textRenderer, label, x + 8, markY + 6, point.color);
                markY += 24;
            }
        }
        if (MistConfig.enabled("crosshair")) drawCrosshair(ctx);
        if (MistConfig.enabled("keystrokes")) drawKeystrokes(ctx, mc);
        if (MistConfig.enabled("kill_effect") && System.currentTimeMillis() < killEffectUntil) drawKillEffect(ctx);
    }

    private static void drawCrosshair(DrawContext ctx) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int size = MystVisualConfig.crosshairSize, gap = MystVisualConfig.crosshairGap;
        int t = MystVisualConfig.crosshairThickness, color = MystVisualConfig.crosshairColor;
        switch (MystVisualConfig.crosshairStyle) {
            case 1 -> ctx.fill(cx - t, cy - t, cx + t + 1, cy + t + 1, color);
            case 2 -> {
                ctx.fill(cx - t, cy - size, cx + t + 1, cy + size + 1, color);
                ctx.fill(cx - size, cy - t, cx + size + 1, cy + t + 1, color);
            }
            case 3 -> {
                int r = size;
                ctx.fill(cx - r, cy - r, cx - r + t, cy - r + t, color);
                ctx.fill(cx + r - t, cy - r, cx + r, cy - r + t, color);
                ctx.fill(cx - r, cy + r - t, cx - r + t, cy + r, color);
                ctx.fill(cx + r - t, cy + r - t, cx + r, cy + r, color);
            }
            default -> {
                ctx.fill(cx - t / 2, cy - gap - size, cx + (t + 1) / 2, cy - gap, color);
                ctx.fill(cx - t / 2, cy + gap + 1, cx + (t + 1) / 2, cy + gap + size + 1, color);
                ctx.fill(cx - gap - size, cy - t / 2, cx - gap, cy + (t + 1) / 2, color);
                ctx.fill(cx + gap + 1, cy - t / 2, cx + gap + size + 1, cy + (t + 1) / 2, color);
            }
        }
    }

    private static void drawKeystrokes(DrawContext ctx, MinecraftClient mc) {
        int x = 8, y = ctx.getScaledWindowHeight() - 76;
        drawKey(ctx, mc, x + 22, y, "W", mc.options.forwardKey.isPressed());
        drawKey(ctx, mc, x, y + 22, "A", mc.options.leftKey.isPressed());
        drawKey(ctx, mc, x + 22, y + 22, "S", mc.options.backKey.isPressed());
        drawKey(ctx, mc, x + 44, y + 22, "D", mc.options.rightKey.isPressed());
        drawKey(ctx, mc, x, y + 44, "LMB", mc.options.attackKey.isPressed());
        drawKey(ctx, mc, x + 44, y + 44, "RMB", mc.options.useKey.isPressed());
    }

    private static void drawKey(DrawContext ctx, MinecraftClient mc, int x, int y, String label, boolean pressed) {
        ctx.fill(x, y, x + 40, y + 19, pressed ? 0xFF65458C : PANEL);
        ctx.fill(x, y, x + 40, y + 1, pressed ? PURPLE : 0xFF493B58);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawTextWithShadow(mc.textRenderer, label, x + (40 - tw) / 2, y + 6, 0xFFF5EEFF);
    }

    private static void drawKillEffect(DrawContext ctx) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        long remaining = Math.max(0L, killEffectUntil - System.currentTimeMillis());
        int alpha = (int)(Math.min(1.0, remaining / 650.0) * 150);
        int color = (alpha << 24) | 0xB58AFF;
        for (int i = 0; i < 4; i++) {
            int radius = 12 + i * 7;
            ctx.fill(cx - radius, cy - radius, cx + radius, cy - radius + 1, color);
            ctx.fill(cx - radius, cy + radius - 1, cx + radius, cy + radius, color);
            ctx.fill(cx - radius, cy - radius, cx - radius + 1, cy + radius, color);
            ctx.fill(cx + radius - 1, cy - radius, cx + radius, cy + radius, color);
        }
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            boolean attack = mc.options.attackKey.isPressed(), use = mc.options.useKey.isPressed();
            if (attack && !lastAttackPressed) leftClicks++;
            if (use && !lastUsePressed) rightClicks++;
            lastAttackPressed = attack; lastUsePressed = use;
            long now = System.currentTimeMillis();
            if (now - cpsWindowStart >= 1000L) {
                shownLeftCps = leftClicks; shownRightCps = rightClicks;
                leftClicks = rightClicks = 0; cpsWindowStart = now;
            }
        } else lastAttackPressed = lastUsePressed = false;
        if (mc.player == null || mc.targetedEntity == null) {
            trackedTarget = null;
            previousHealth = -1.0F;
            displayedTargetHealth = -1.0F;
            return;
        }
        if (!(mc.targetedEntity instanceof LivingEntity target)) return;
        java.util.UUID id = target.getUuid();
        if (!id.equals(trackedTarget)) {
            trackedTarget = id;
            previousHealth = target.getHealth();
            return;
        }
        float health = target.getHealth();
        if (previousHealth > 0.0F && health <= 0.0F && MistConfig.enabled("kill_effect")) {
            killEffectUntil = System.currentTimeMillis() + 650L;
        }
        previousHealth = health;
    }

    private static void panel(DrawContext ctx, int x, int y, int w, int h) {
        if (MystUiConfig.hudBackground) {
            int alpha = Math.max(35, Math.min(255, Math.round(255.0f * MystUiConfig.hudOpacity / 100.0f)));
            int background = (alpha << 24) | 0x001A1425;
            ctx.fill(x, y, x + w, y + h, background);
        }
        ctx.fill(x, y, x + 2, y + h, MystUiConfig.accent());
    }
}
