package com.somefrills.hud.clickgui.components;

import com.somefrills.misc.Rendering;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindButton extends ButtonComponent {
    private final EventStream<KeybindChanged> changedEvents = KeybindChanged.newStream();
    private final List<Integer> keybindBlacklist = List.of(
            GLFW.GLFW_KEY_UNKNOWN,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            GLFW.GLFW_KEY_ESCAPE
    );
    public Component unbound = Component.literal("Not Bound").withColor(0xffffff);
    public Component binding = Component.literal("Press Key...").withColor(0xffffff);
    public boolean isBinding = false;

    public KeybindButton() {
        super(Component.empty(), button -> {
        });
        this.onPress(button -> {
            if (this.isBinding) {
                this.bind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                this.setMessage(this.binding);
                this.isBinding = true;
            }
        });
        this.renderer((context, btn, delta) -> {
            context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
            Rendering.drawBorder(context, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xff5ca0bf);
        });
        this.horizontalSizing(Sizing.fixed(80));
        this.setMessage(this.unbound);
    }

    // Human-friendly label for a key or mouse button. Never returns a raw numeric string.
    public Component getKeyLabel(int keycode) {
        return staticGetKeyLabel(keycode);
    }

    // Static variant for use by other classes (e.g., SettingKeybind)
    public static Component staticGetKeyLabel(int keycode) {
        // explicit quick mapping for common printable keys and navigation keys
        // letters
        if (keycode >= GLFW.GLFW_KEY_A && keycode <= GLFW.GLFW_KEY_Z) {
            char c = (char) ('A' + (keycode - GLFW.GLFW_KEY_A));
            return Component.literal(String.valueOf(c)).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        // numbers row
        if (keycode >= GLFW.GLFW_KEY_0 && keycode <= GLFW.GLFW_KEY_9) {
            char c = (char) ('0' + (keycode - GLFW.GLFW_KEY_0));
            return Component.literal(String.valueOf(c)).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        // numpad
        if (keycode >= GLFW.GLFW_KEY_KP_0 && keycode <= GLFW.GLFW_KEY_KP_9) {
            int n = keycode - GLFW.GLFW_KEY_KP_0;
            return Component.literal("Num " + n).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        // arrows and navigation
        switch (keycode) {
            case GLFW.GLFW_KEY_LEFT: return Component.literal("Left").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT: return Component.literal("Right").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_UP: return Component.literal("Up").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_DOWN: return Component.literal("Down").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_UP: return Component.literal("Page Up").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_DOWN: return Component.literal("Page Down").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_HOME: return Component.literal("Home").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_END: return Component.literal("End").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_INSERT: return Component.literal("Insert").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_DELETE: return Component.literal("Delete").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_BACKSPACE: return Component.literal("Backspace").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_ENTER: return Component.literal("Enter").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_TAB: return Component.literal("Tab").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SPACE: return Component.literal("Space").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_ESCAPE: return Component.literal("Escape").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return Component.literal("` / ~").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_COMMA: return Component.literal(",").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PERIOD: return Component.literal(".").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SLASH: return Component.literal("/").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SEMICOLON: return Component.literal(";").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_APOSTROPHE: return Component.literal("'").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_LEFT_BRACKET: return Component.literal("[").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return Component.literal("]").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_MINUS: return Component.literal("-").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_EQUAL: return Component.literal("=").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_BACKSLASH: return Component.literal("\\").withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        // function keys
        if (keycode >= GLFW.GLFW_KEY_F1 && keycode <= GLFW.GLFW_KEY_F25) {
            int f = keycode - GLFW.GLFW_KEY_F1 + 1;
            return Component.literal("F" + f).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        // continue to other fallbacks below
         // 1) Try GLFW native name (may be layout sensitive)
         try {
             String name = GLFW.glfwGetKeyName(keycode, 0);
             if (name != null && !name.isEmpty()) {
                // some platforms may return numeric strings for certain keys (e.g. "96").
                // avoid showing raw numbers — fall through to other, friendlier fallbacks.
                if (name.matches("\\d+")) {
                    // ignore numeric glfw name
                } else {
                    if (name.length() == 1) return Component.literal(name.toUpperCase()).withStyle(net.minecraft.ChatFormatting.WHITE);
                    return Component.literal(name.replace('_', ' ')).withStyle(net.minecraft.ChatFormatting.WHITE);
                }
             }
         } catch (Throwable ignored) {}

        // 2) Keyboard localized text via InputUtil
        try {
            InputConstants.Key input = InputConstants.Type.KEYSYM.getOrCreate(keycode);
            String localized = input.getDisplayName().getString();
            String translation = input.getName();
            if (localized != null && !localized.isEmpty() && !localized.equals(translation)) {
                if (!localized.matches("\\d+")) {
                    return input.getDisplayName();
                }
            }
            if (translation != null && !translation.isEmpty()) {
                String token = translation;
                if (token.startsWith("key.keyboard.")) token = token.substring("key.keyboard.".length());
                else if (token.startsWith("key.mouse.")) token = token.substring("key.mouse.".length());
                else if (token.contains(".")) token = token.substring(token.lastIndexOf('.') + 1);
                token = token.replace('_', ' ');
                String pretty = staticCapitalize(token);
                if (pretty.equalsIgnoreCase("grave accent")) pretty = "` / ~";
                return Component.literal(pretty).withStyle(net.minecraft.ChatFormatting.WHITE);
            }
        } catch (Throwable ignored) {}

        // 3) Mouse fallback
        try {
            InputConstants.Key mouse = InputConstants.Type.MOUSE.getOrCreate(keycode);
            String mouseLabel = mouse.getDisplayName().getString();
            String mouseTrans = mouse.getName();
            if (mouseLabel != null && !mouseLabel.isEmpty() && !mouseLabel.equals(mouseTrans)) {
                return mouse.getDisplayName();
            }
            if (keycode >= GLFW.GLFW_MOUSE_BUTTON_1 && keycode <= GLFW.GLFW_MOUSE_BUTTON_8) {
                int idx = keycode - GLFW.GLFW_MOUSE_BUTTON_1 + 1;
                return Component.literal("Mouse " + idx).withStyle(net.minecraft.ChatFormatting.WHITE);
            }
        } catch (Throwable ignored) {}

        return Component.literal("Unknown").withStyle(net.minecraft.ChatFormatting.WHITE);
    }

    private static String staticCapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    public void bind(int key) {
        if (!this.valid(key)) {
            this.setMessage(this.unbound);
            changedEvents.sink().onBind(GLFW.GLFW_KEY_UNKNOWN);
        } else {
            Component label = getKeyLabel(key);
            String s = label.getString();
            if ("Unknown".equals(s)) {
                // Try ASCII fallback (show printable char instead of a number)
                if (key >= 32 && key <= 126) {
                    char ch = (char) key;
                    this.setMessage(Component.literal(String.valueOf(ch)).withStyle(net.minecraft.ChatFormatting.WHITE));
                } else {
                    this.setMessage(label);
                }
            } else {
                this.setMessage(label);
            }
            changedEvents.sink().onBind(key);
        }
        this.isBinding = false;
    }

    public EventSource<KeybindChanged> onBound() {
        return changedEvents.source();
    }

    private boolean valid(int key) {
        for (int blacklisted : this.keybindBlacklist) {
            if (key == blacklisted) {
                return false;
            }
        }
        return true;
    }

    public interface KeybindChanged {
        static EventStream<KeybindChanged> newStream() {
            return new EventStream<>(subscribers -> (keycode) -> {
                for (var subscriber : subscribers) {
                    subscriber.onBind(keycode);
                }
            });
        }

        void onBind(int keycode);
    }
}
