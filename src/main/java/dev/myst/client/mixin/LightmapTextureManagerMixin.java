package dev.myst.client.mixin;

import dev.myst.client.module.ModuleManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("HEAD"), cancellable = true)
    private static void mystvisuals$fullbright(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.enabled("Fullbright")) cir.setReturnValue(1.0F);
    }

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void mystvisuals$fullbrightAmbient(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.enabled("Fullbright")) cir.setReturnValue(1.0F);
    }
}
