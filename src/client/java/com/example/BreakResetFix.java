package com.example;

import com.example.mixin.ClientPlayerInteractionManagerAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

import static com.example.ExampleModClient.mc;

public class BreakResetFix {
    public static void onInventoryUpdate(ScreenHandlerSlotUpdateS2CPacket packet, ItemStack stack, int slotId) {
        if (AllConfig.breakResetFix && mc.player != null && mc.interactionManager != null) {
            if (slotId >= 36 && slotId <= 44 && mc.player.getInventory().getSelectedSlot() == slotId - 36) {
                ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setStack(stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }
    }
}