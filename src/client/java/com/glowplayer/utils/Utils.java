package com.glowplayer.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.glowplayer.Main.mc;

public class Utils {
    public static String toPlain(Text text) {
        if (text != null) {
            return Formatting.strip(text.getString());
        }
        return "";
    }

    public static ItemStack getHeldItem() {
        return mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
    }

    private static List<Text> getLoreText(ItemStack stack) {
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return new ArrayList<>();
    }

    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        for (Text line : getLoreText(stack)) {
            lines.add(toPlain(line).trim());
        }
        return lines;
    }

    public static String getRightClickAbility(ItemStack stack) {
        for (String line : getLoreLines(stack)) {
            if (line.contains("Ability: ") && line.endsWith("RIGHT CLICK")) {
                return line;
            }
        }
        return "";
    }

    public static NbtCompound getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (data != null) {
                return data.copyNbt();
            }
        }
        return null;
    }

    public static String getSkyblockId(NbtCompound customData) {
        if (customData != null && customData.contains("id")) {
            return customData.getString("id").orElse("");
        }
        return "";
    }

    public static void clickSlot(int slotIdx) {
        if (mc.interactionManager == null || mc.player == null) {
            return;
        }

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    public static String getSkyblockId(ItemStack stack) {
        return getSkyblockId(getCustomData(stack));
    }

    public static boolean hasRightClickAbility(ItemStack stack) {
        return !getRightClickAbility(stack).isEmpty();
    }
}
