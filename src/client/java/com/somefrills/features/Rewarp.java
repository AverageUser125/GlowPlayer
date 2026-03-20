package com.somefrills.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.somefrills.config.Feature;
import com.somefrills.config.SettingJson;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Rendering;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static com.somefrills.Main.mc;

public class Rewarp {
    public static final Feature instance = new Feature("rewarp");
    // Use the feature key as the parent so settings are grouped under the feature
    public static SettingJson warps = new SettingJson(new JsonObject(), "warps", instance);

    public static void addWarp(String name) {
        if (mc.player == null) return;
        BlockPos pos = mc.player.getBlockPos();
        warps.edit(data -> {
            data.addProperty(name, pos.getX() + "," + pos.getY() + "," + pos.getZ());
        });
        Utils.infoFormat("Added rewarp '{}' at {},{},{}.", name, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void removeWarp(String name) {
        com.google.gson.JsonObject data = warps.value();
        if (data == null || !data.has(name)) {
            Utils.infoFormat("No rewarp named '{}' found.", name);
            return;
        }
        warps.edit(d -> d.remove(name));
        Utils.infoFormat("Removed rewarp '{}'.", name);
    }

    public static void clearWarps() {
        // Replace the whole object with an empty one to clear all entries
        warps.set(new com.google.gson.JsonObject());
        Utils.info("Cleared all rewarp points.");
    }

    @EventHandler
    public static void onWorldRender(WorldRenderEvent event) {
        // Only render in the garden
        if (mc.player == null) return;

        com.google.gson.JsonObject data = warps.value();
        if (data == null) return;

        RenderColor cyan = RenderColor.fromHex(0x00FFFF, 0.4f);

        for (java.util.Map.Entry<String, JsonElement> entry : data.entrySet()) {
            try {
                String[] parts = entry.getValue().getAsString().split(",");
                if (parts.length != 3) continue;
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                // center the box on the block
                Vec3d center = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
                Box box = Box.of(center, 0.5, 0.5, 0.5);
                // throughWalls = true to see through walls; use Rendering directly
                Rendering.drawFilled(event.matrices, event.consumer, event.camera, box, true, cyan);
            } catch (Exception ignored) {
            }
        }
    }
}
