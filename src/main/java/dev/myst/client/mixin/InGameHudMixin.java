package dev.myst.client.mixin;
import dev.myst.client.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
 @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
 private void myst$replaceVanillaCrosshair(DrawContext context,RenderTickCounter tickCounter,CallbackInfo ci){CustomCrosshair.render(context,tickCounter);ci.cancel();}
}