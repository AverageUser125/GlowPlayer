package com.somefrills.features.tweaks;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.InteractBlockEvent;
import com.somefrills.events.InteractItemEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

import static com.somefrills.Main.mc;

// description moved into constructors

public class DoubleUseFix extends Feature {

    public DoubleUseFix() {
        super(FrillsConfig.instance.tweaks.doubleUseFixEnabled);
    }

    private static type getDisableType() {
        ItemStack held = Utils.getHeldItem();
        if (held.getItem().equals(Items.FISHING_ROD)) {
            return type.Rod;
        }
        if (Utils.getRightClickAbility(held).contains("Attunement")) {
            return type.Dagger;
        }
        return type.None;
    }

    @EventHandler
    private void onUseItem(InteractItemEvent event) {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType().equals(HitResult.Type.BLOCK) && getDisableType().equals(type.Dagger)) {
            event.cancel();
        }
    }

    @EventHandler
    private void onUseBlock(InteractBlockEvent event) {
        if (getDisableType().equals(type.Rod)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            event.cancel();
        }
    }

    private enum type {
        Dagger,
        Rod,
        None
    }
}