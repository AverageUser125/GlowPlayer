package com.somefrills.mixin;

import com.somefrills.misc.Utils;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public class ArmorStandEntityMixin {
    @Inject(method="isPartOfGame", at = @At("HEAD"), cancellable = true)
    private void isPartOfGame(CallbackInfoReturnable<Boolean> cir) {
        if(Utils.isGlowing((ArmorStandEntity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method="isMarker", at = @At("HEAD"), cancellable = true)
    private void isMarker(CallbackInfoReturnable<Boolean> cir) {
        if(Utils.isGlowing((ArmorStandEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
