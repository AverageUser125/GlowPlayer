package com.somefrills.mixin;

import com.somefrills.features.mining.GhostVision;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperPowerLayer.class)
public class CreeperPowerLayerMixin {
    @Inject(method = "isPowered", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CreeperRenderState creeperEntityRenderState, CallbackInfoReturnable<Boolean> cir) {
        // Hide charged creeper effect if config enabled
        if (GhostVision.removeCharge.value()) {
            cir.setReturnValue(false);
        }
    }
}
