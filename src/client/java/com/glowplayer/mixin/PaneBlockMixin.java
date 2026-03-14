package com.glowplayer.mixin;

import com.glowplayer.tweaks.GemstoneDesyncFix;
import com.glowplayer.utils.AllConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PaneBlock.class)
public abstract class PaneBlockMixin {

    @ModifyReturnValue(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    private BlockState onGetUpdateState(BlockState original) {
        if (AllConfig.gemstoneDsyncFix && GemstoneDesyncFix.isDefaultPane(original)) {
            return GemstoneDesyncFix.asFullPane(original);
        }
        return original;
    }
}