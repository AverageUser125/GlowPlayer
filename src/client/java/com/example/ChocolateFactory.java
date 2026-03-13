package com.example;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Fabric 1.21.10 port of the Kotlin ChocolateFactory module.
 * Automatically claims stray rabbits in the Chocolate Factory menu.
 */
public class ChocolateFactory {
    private static final String CHOCOLATE_FACTORY_TITLE = "Chocolate Factory";

    private long lastClaimTime = 0;

    public ChocolateFactory() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client == null || client.player == null) return;

        // Check if auto claim is enabled
        if (!AllConfig.claimStray) return;

        ClientPlayerEntity player = client.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        String title = "";
        if (client.currentScreen != null) {
            Text txt = client.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Check if we're in the Chocolate Factory menu
        if (!title.equals(CHOCOLATE_FACTORY_TITLE)) return;

        // Check click delay
        if (System.currentTimeMillis() - lastClaimTime < AllConfig.claimDelay) return;

        // Scan for claimable items
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty()) continue;

            String displayName = stack.getName().getString();

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("CLICK ME!") || displayName.contains("Golden Rabbit")) {
                sendClickPacket(handler, slot.getIndex());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }
            // convert to string
            List<Text> loreLines = stack.getComponents().get(DataComponentTypes.LORE).lines();
            if (loreLines == null) continue;
            String loreName = loreLines
                    .stream()
                    .map(Text::getString)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
            if (loreName.contains("does not stop it from being")) {
                sendClickPacket(handler, slot.getIndex());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }
        }
    }

    private void sendClickPacket(ScreenHandler handler, int slotIdx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager == null || client.player == null) {
            return;
        }

        client.interactionManager.clickSlot(
                handler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                client.player
        );
    }
}
