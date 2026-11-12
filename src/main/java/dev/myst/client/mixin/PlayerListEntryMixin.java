package dev.myst.client.mixin;

import dev.myst.client.StreamerConfig;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void mystclient$replaceTabName(CallbackInfoReturnable<Text> cir) {
        PlayerListEntry self = (PlayerListEntry)(Object)this;
        String original = self.getProfile().name();
        String replacement = StreamerConfig.replace(original);
        if (!replacement.equals(original)) cir.setReturnValue(Text.literal(replacement));
    }
}
