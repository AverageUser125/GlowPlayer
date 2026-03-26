package com.somefrills.config;

import org.lwjgl.glfw.GLFW;
import com.somefrills.hud.clickgui.components.KeybindButton;
import net.minecraft.network.chat.Component;

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
    public Component getLabel() {
        int k = this.value();
        if (k == GLFW.GLFW_KEY_UNKNOWN) return Component.literal("Not Bound").withStyle(net.minecraft.ChatFormatting.WHITE);
        return KeybindButton.staticGetKeyLabel(k);
    }
}