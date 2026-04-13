package com.somefrills.events;


import net.minecraft.item.ItemStack;

/**
 * Posted when the player's held item (main hand) changes.
 * Used to track tool switches for mining speed detection.
 */
public class OnHeldSlotEvent {
    public final ItemStack itemStack;

    public OnHeldSlotEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}

