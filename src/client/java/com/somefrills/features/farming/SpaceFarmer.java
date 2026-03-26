package com.somefrills.features.farming;

import com.somefrills.config.Feature;
import com.somefrills.events.InputEvent;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

import static com.somefrills.Main.mc;

public class SpaceFarmer {
    public static final Feature instance = new Feature("spaceFarmer");
    public static boolean spaceHeld = false;

    @EventHandler
    public static void onKey(InputEvent event) {
        if (!instance.isActive() || event.key != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (mc.screen != null && spaceHeld) {
            spaceHeld = false;
            mc.options.keyAttack.setDown(false);
            return;
        }
        if (event.action == GLFW.GLFW_PRESS && mc.options.keyShift.isDown() && Utils.isOnGardenPlot()) {
            spaceHeld = true;
            mc.options.keyAttack.setDown(true);
            event.cancel();
        } else if (event.action == GLFW.GLFW_RELEASE && spaceHeld) {
            spaceHeld = false;
            if (mc.options.keyAttack.isDown()) {
                mc.options.keyAttack.setDown(false);
            }
            event.cancel();
        } else if (spaceHeld) {
            event.cancel();
        }
    }

    @EventHandler
    public static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && spaceHeld) {
            spaceHeld = false;
            mc.options.keyAttack.setDown(false);
        }
    }
}
