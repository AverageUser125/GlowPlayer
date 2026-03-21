package com.somefrills.mixin;

import com.somefrills.features.mining.GhostVision;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperChargeFeatureRenderer.class)
public class CreeperChargeFeatureRendererMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(CreeperEntityRenderState creeperEntityRenderState, CallbackInfoReturnable<Boolean> cir) {
        // Hide charged creeper effect if config enabled
        if (GhostVision.removeCharge.value()) {
            cir.setReturnValue(false);
        }
    }
}
