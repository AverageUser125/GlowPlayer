package com.somefrills.mixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.mining.GhostVision;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @ModifyReturnValue(method = "isCharged", at = @At("RETURN"))
    private boolean isCharged(boolean original) {
        if (GhostVision.isGhost((CreeperEntity) (Object) this)) {
            return false;
        }
        return original;
    }
}


