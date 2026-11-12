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
            ctx.drawTextWithShadow(mc.textRenderer, "MV  Mist Visuals", 14, y + 6, PURPLE);
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
        if (MistConfig.enabled("target_hud") && mc.targetedEntity instanceof LivingEntity target && target.isAlive()) {
            int x = ctx.getScaledWindowWidth() / 2 + 12;
            int ty = ctx.getScaledWindowHeight() / 2 + 8;
            panel(ctx, x, ty, 138, 38);
            ctx.drawTextWithShadow(mc.textRenderer, target.getName().getString(), x + 7, ty + 6, 0xFFF1E9FF);
            float max = Math.max(1.0F, target.getMaxHealth());
            float ratio = Math.max(0.0F, Math.min(1.0F, target.getHealth() / max));
            ctx.fill(x + 7, ty + 23, x + 131, ty + 29, 0xFF45384F);
            ctx.fill(x + 7, ty + 23, x + 7 + (int)(124 * ratio), ty + 29, PURPLE);
            ctx.drawTextWithShadow(mc.textRenderer, String.format(java.util.Locale.ROOT, "%.1f / %.1f HP", target.getHealth(), max), x + 7, ty + 31, 0xFFD9CDEB);
        }
        if (MistConfig.enabled("block_info") && mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            String name = mc.world.getBlockState(hit.getBlockPos()).getBlock().getName().getString();
            int x = ctx.getScaledWindowWidth() / 2 + 12;
            int by = ctx.getScaledWindowHeight() / 2 + 54;
            panel(ctx, x, by, Math.min(190, mc.textRenderer.getWidth(name) + 18), 20);
            ctx.drawTextWithShadow(mc.textRenderer, name, x + 8, by + 6, 0xFFF1E9FF);
        }
    }

    private static void panel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x, y, x + 2, y + h, PURPLE);
    }
}
