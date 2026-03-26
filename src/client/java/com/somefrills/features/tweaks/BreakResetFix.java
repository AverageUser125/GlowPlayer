package com.somefrills.features.tweaks;

import com.somefrills.config.Feature;
import com.somefrills.events.InventoryUpdateEvent;
import com.somefrills.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.mc;

public class BreakResetFix {
    public static final Feature instance = new Feature("breakResetFix", true);

    @EventHandler
    public static void onBreakReset(InventoryUpdateEvent event) {
        if (!instance.isActive()) return;
        if (mc.player != null && mc.gameMode != null) {
            if (event.slotId >= 36 && event.slotId <= 44 && mc.player.getInventory().getSelectedSlot() == event.slotId - 36) {
                ((MultiPlayerGameModeAccessor) mc.gameMode).setStack(event.stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }

    }
}
