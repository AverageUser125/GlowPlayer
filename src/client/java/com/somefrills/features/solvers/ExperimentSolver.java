package com.somefrills.features.solvers;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingDescription;
import com.somefrills.config.SettingInt;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.SlotUpdateEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

import java.util.*;

import static com.somefrills.Main.mc;
public class ExperimentSolver {
    public static final Feature instance = new Feature("experimentSolver");

    @SettingDescription("Automatically solve the Chronomatron")
    public static final SettingBool chronomatron = new SettingBool(true);
    @SettingDescription("Automatically solve the Ultrasequencer")
    public static final SettingBool ultrasequencer = new SettingBool(true);
    @SettingDescription("Click delay")
    public static final SettingInt clickDelay = new SettingInt(300);

    private static final List<Solution> chronoSolution = new ArrayList<>();
    private static final List<Solution> ultraSolution = new ArrayList<>();
    private static boolean rememberPhase = true;
    private static long lastClickTime = 0;

    private static void updatePhase(ItemStack stack) {
        Item item = stack.getItem();
        if (!rememberPhase && item.equals(Items.GLOWSTONE)) {
            rememberPhase = true;
        }
        if (rememberPhase && item.equals(Items.CLOCK)) {
            rememberPhase = false;
        }
    }

    public static ExperimentType getExperimentType() {
        if (Utils.isOnPrivateIsland() && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.startsWith("Chronomatron (")) return ExperimentType.Chronomatron;
            if (title.startsWith("Ultrasequencer (")) return ExperimentType.Ultrasequencer;
        }
        return ExperimentType.None;
    }

    private static boolean isStatus(ItemStack stack) {
        Item item = stack.getItem();
        String name = Utils.toPlain(stack.getName());
        return item.equals(Items.CLOCK)
                || item.equals(Items.BOOKSHELF)
                || (item.equals(Items.GLOWSTONE) && !name.equals("Enchanted Book"))
                || item.equals(Items.CAULDRON);
    }

    private static boolean isDye(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeItem
                || item.equals(Items.INK_SAC)
                || item.equals(Items.BONE_MEAL)
                || item.equals(Items.LAPIS_LAZULI)
                || item.equals(Items.COCOA_BEANS);
    }

    private static boolean isTerracotta(ItemStack stack) {
        return stack.getItem().toString().endsWith("terracotta");
    }

    private static boolean isStainedGlass(ItemStack stack) {
        return stack.getItem().toString().endsWith("stained_glass");
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (!instance.isActive() || rememberPhase) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < clickDelay.value()) return;

        ExperimentType type = getExperimentType();

        // Auto-solver logic for Chronomatron
        if (chronomatron.value() && type == ExperimentType.Chronomatron && !chronoSolution.isEmpty()) {
            Solution next = chronoSolution.getFirst();
            // We click the first slot in the current requirement group
            if (!next.slots.isEmpty()) {
                Slot slotToClick = next.slots.get(0);
                Utils.clickSlot(slotToClick.id);
                next.slots.remove(0);
                if (next.slots.isEmpty()) chronoSolution.removeFirst();
                lastClickTime = currentTime;
            }
        }

        // Auto-solver logic for Ultrasequencer
        if (ultrasequencer.value() && type == ExperimentType.Ultrasequencer && !ultraSolution.isEmpty()) {
            Slot slotToClick = ultraSolution.get(0).slot;
            Utils.clickSlot(slotToClick.id);
            ultraSolution.remove(0);
            lastClickTime = currentTime;
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (!instance.isActive() || event.isInventory || event.slot == null) return;

        ExperimentType experimentType = getExperimentType();
        if (experimentType == ExperimentType.None) return;

        updatePhase(event.stack);

        // Recording Phase for Chronomatron
        if (chronomatron.value() && experimentType == ExperimentType.Chronomatron) {
            if (rememberPhase) {
                if (isTerracotta(event.stack)) {
                    if (chronoSolution.isEmpty()) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                    chronoSolution.get(chronoSolution.size() - 1).slots.add(event.slot);
                } else if (isStainedGlass(event.stack)) {
                    if (!chronoSolution.isEmpty() && chronoSolution.get(chronoSolution.size() - 1).slots.stream().anyMatch(slot -> slot.id == event.slotId)) {
                        chronoSolution.add(new Solution(new ArrayList<>()));
                    }
                }
            }
        }

        // Recording Phase for Ultrasequencer
        if (ultrasequencer.value() && experimentType == ExperimentType.Ultrasequencer) {
            if (event.stack.getItem().equals(Items.GLOWSTONE)) {
                List<Solution> tempSolution = new ArrayList<>();
                for (Slot slot : Utils.getContainerSlots(event.handler)) {
                    if (isDye(slot.getStack())) {
                        tempSolution.add(new Solution(slot.getStack(), slot));
                    }
                }
                // Sort by stack count (1, 2, 3...)
                tempSolution.sort(Comparator.comparingInt(s -> s.stack.getCount()));
                ultraSolution.clear();
                ultraSolution.addAll(tempSolution);
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive()) {
            rememberPhase = true;
            chronoSolution.clear();
            ultraSolution.clear();
            lastClickTime = System.currentTimeMillis(); // Prevent instant click on open
        }
    }

    public enum ExperimentType {
        Chronomatron, Ultrasequencer, None
    }

    private static class Solution {
        public ItemStack stack;
        public Slot slot;
        public List<Slot> slots;

        public Solution(ItemStack stack, Slot slot) {
            this.stack = stack;
            this.slot = slot;
        }

        public Solution(List<Slot> slots) {
            this.slots = slots;
        }
    }
}