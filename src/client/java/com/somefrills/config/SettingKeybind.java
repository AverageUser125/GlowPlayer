package com.somefrills.config;

import org.lwjgl.glfw.GLFW;
import com.somefrills.hud.clickgui.components.KeybindButton;
import net.minecraft.text.Text;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue) {
        super(defaultValue);
    }

    public int key() {
        return this.value();
    }

    public boolean bound() {
        return this.value() != GLFW.GLFW_KEY_UNKNOWN;
    }

    public boolean isKey(int key) {
        return key != GLFW.GLFW_KEY_UNKNOWN && key == this.value();
    }

    // Returns a human-readable label for the current binding (never a raw number)
    public Text getLabel() {
        int k = this.value();
        if (k == GLFW.GLFW_KEY_UNKNOWN) return Text.literal("Not Bound").formatted(net.minecraft.util.Formatting.WHITE);
        return KeybindButton.staticGetKeyLabel(k);
    }
}