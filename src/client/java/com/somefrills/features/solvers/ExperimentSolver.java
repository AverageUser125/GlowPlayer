package com.somefrills.features.solvers;

import com.somefrills.config.*;
import com.somefrills.events.HudTickEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class ExperimentSolver {
    public static final Feature instance = new Feature("experimentSolver");

    @SettingDescription("Enable automatic solving of Chronomatron experiments")
    public static final SettingBool enableChronomatron = new SettingBool(true);

    @SettingDescription("Enable automatic solving of Ultrasequencer experiments")
    public static final SettingBool enableUltrasequencer = new SettingBool(true);

    @SettingDescription("Try to maximize XP when solving experiments")
    public static final SettingBool getMaxXp = new SettingBool(false);

    @SettingDescription("Automatically close the experiment screen when conditions are met")
    public static final SettingBool autoClose = new SettingBool(true);

    @SettingDescription("Number of serum items to keep in Chronomatron")
    public static final SettingIntSlider serumCount = new SettingIntSlider(3, 0, 3);

    @SettingDescription("Delay between automated clicks (ms)")
    public static final SettingInt clickDelay = new SettingInt(100);

    // Regex pattern for experiment table detection
    // Matches: Superpairs, Chronomatron, Ultrasequencer with optional suffixes like (Metaphysical), ➜ Stakes, Rewards
    // Also matches: Experimentation Table, Experiment Over
    private static final Pattern EXPERIMENT_PATTERN = Pattern.compile(
            "(?:Superpairs|Chronomatron|Ultrasequencer) ?(?:\\(.+\\)|➜ Stakes|Rewards)|Experiment(?:ation Tabl| [Oo]v)er?",
            Pattern.CASE_INSENSITIVE
    );
    // ultrasequencerIndex: index = (dye count - 1) -> slot index; null when not initialized
    private static int[] ultrasequencerIndex = null;
    private static final List<Integer> chronomatronOrder = new ArrayList<>();
    private static long lastClickTime = 0;
    private static boolean hasAdded = false;
    private static int lastAdded = 0;
    private static int clicks = 0;

    @EventHandler
    private static void onHudTick(HudTickEvent event) {
        if (mc == null || mc.player == null) return;
        ClientPlayerEntity player = mc.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        String title = "";
        if (mc.currentScreen != null) {
            Text txt = mc.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Use regex pattern to detect experiment table
        if (EXPERIMENT_PATTERN.matcher(title).find()) {

            // Determine which experiment type
            if (enableChronomatron.value() && title.contains("Chronomatron")) {
                solveChronomatron(handler);
            } else if (enableUltrasequencer.value() && title.contains("Ultrasequencer")) {
                solveUltraSequencer(handler);
            }
        } else {
            if (!title.isEmpty()) {
                reset();
            }
        }
    }

    private static void solveChronomatron(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxChronomatron = getMaxXp.value() ? 15 : (11 - serumCount.value());

        // Check if slot 49 is glowstone AND last added slot is not enchanted (click registered)
        Slot slot49 = invSlots.get(49);
        Slot lastAddedSlot = invSlots.get(lastAdded);
        boolean slot49IsGlowstone = slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:glowstone");
        boolean lastAddedNotEnchanted = lastAddedSlot.getStack() != null && !isEnchanted(lastAddedSlot.getStack());

        if (slot49IsGlowstone && lastAddedNotEnchanted) {
            if (autoClose.value() && chronomatronOrder.size() > maxChronomatron) {
                if (mc.player != null) {
                    mc.player.closeHandledScreen();
                }
            }
            hasAdded = false;
        }

        // Detect new item entering: slot 49 is clock
        if (!hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")) {
            // Scan for enchanted TERRACOTTA ONLY, filtering duplicates
            // Each color appears in 2 rows (row height = 9), so skip slots that are 9 apart (same color)

            int lastSlotAdded = -1;
            chronomatronOrder.clear();
            for (int i = 11; i < 57; i++) { // Scan the colored glass/terracotta area (slots 11-56)
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty() && isEnchanted(s.getStack())) {
                    String itemName = Registries.ITEM.getId(s.getStack().getItem()).toString();

                    // ONLY add terracotta blocks, skip everything else
                    if (!itemName.contains("terracotta")) {
                        continue;
                    }

                    // Skip if this is a duplicate (same color, 9 slots away)
                    if (lastSlotAdded != -1 && (i - lastSlotAdded) == 9) {
                        continue;
                    }

                    chronomatronOrder.add(i);
                    lastSlotAdded = i;
                }
            }

            if (!chronomatronOrder.isEmpty()) {
                lastAdded = chronomatronOrder.getLast();
                hasAdded = true;
                clicks = 0;
            }
        }

        // Perform clicking: slot 49 is clock AND we have items to click
        if (hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")
                && chronomatronOrder.size() > clicks && System.currentTimeMillis() - lastClickTime > clickDelay.value()) {
            int slotToClick = chronomatronOrder.get(clicks);
            Utils.clickSlot(slotToClick);
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private static void solveUltraSequencer(ScreenHandler handler) {
        List<Slot> invSlots = handler.slots;
        int maxUltraSequencer = getMaxXp.value() ? 20 : (9 - serumCount.value());

        // Reset when slot 49 becomes clock (new round)
        Slot slot49 = invSlots.get(49);
        if (slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")) {
            hasAdded = false;
        }

        // Detect and rebuild map when slot 49 becomes glowstone
        if (!hasAdded && slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:glowstone")) {
            // Build an index array keyed by dye count (count 1 -> index 0)
            int start = 11;
            int end = Math.min(57, invSlots.size());
            int maxCount = 0;
            for (int i = start; i < end; i++) {
                Slot s = invSlots.get(i);
                if (s.getStack() != null && !s.getStack().isEmpty() && isItem(s.getStack(), "minecraft:dye")) {
                    int cnt = s.getStack().getCount();
                    if (cnt > maxCount) maxCount = cnt;
                }
            }

            if (maxCount > 0) {
                int[] idx = new int[maxCount];
                for (int i = 0; i < maxCount; i++) idx[i] = -1;
                int valid = 0;
                for (int i = start; i < end; i++) {
                    Slot s = invSlots.get(i);
                    if (s.getStack() != null && !s.getStack().isEmpty() && isItem(s.getStack(), "minecraft:dye")) {
                        int cnt = s.getStack().getCount();
                        int pos = cnt - 1;
                        if (pos >= 0 && pos < maxCount && idx[pos] == -1) {
                            idx[pos] = i;
                            valid++;
                        }
                    }
                }

                // Convert index array to ultrasequencerIndex only if we found at least one mapping
                if (valid > 0) {
                    ultrasequencerIndex = idx;
                    hasAdded = true;
                    clicks = 0;

                    if (valid > maxUltraSequencer && autoClose.value()) {
                        if (mc.player != null) mc.player.closeHandledScreen();
                    }
                }
            }
        }

        // Perform clicking: slot 49 is clock AND we have dyes to click
        if (slot49.getStack() != null && isItem(slot49.getStack(), "minecraft:clock")
                && ultrasequencerIndex != null && clicks < ultrasequencerIndex.length && ultrasequencerIndex[clicks] != -1
                && System.currentTimeMillis() - lastClickTime > clickDelay.value()) {
            int slotToClick = ultrasequencerIndex[clicks];
            Utils.clickSlot(slotToClick);
            lastClickTime = System.currentTimeMillis();
            clicks++;
        }
    }

    private static boolean isItem(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) return false;
        String actualId = Registries.ITEM.getId(stack.getItem()).toString();

        // Handle dye variants for 1.21.10 (white_dye, red_dye, etc.) and dye-like items
        if (itemId.equals("minecraft:dye")) {
            return actualId.endsWith("_dye") ||
                    actualId.equals("minecraft:bone_meal") ||
                    actualId.equals("minecraft:lapis_lazuli") ||
                    actualId.equals("minecraft:cocoa_beans") ||
                    actualId.equals("minecraft:ink_sac") ||
                    actualId.equals("minecraft:glow_ink_sac");
        }

        return actualId.equals(itemId);
    }

    private static boolean isEnchanted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check for both actual enchantments AND the enchanted glint effect
        return stack.hasEnchantments() || stack.hasGlint();
    }

    private static void reset() {
        ultrasequencerIndex = null;
        chronomatronOrder.clear();
        hasAdded = false;
        lastAdded = 0;
        clicks = 0;
    }
}