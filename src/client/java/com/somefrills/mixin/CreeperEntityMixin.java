package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.mining.GhostVision;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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


