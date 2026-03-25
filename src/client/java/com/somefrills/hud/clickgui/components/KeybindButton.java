package com.somefrills.hud.clickgui.components;

import com.somefrills.misc.Rendering;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindButton extends ButtonComponent {
    private final EventStream<KeybindChanged> changedEvents = KeybindChanged.newStream();
    private final List<Integer> keybindBlacklist = List.of(
            GLFW.GLFW_KEY_UNKNOWN,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            GLFW.GLFW_KEY_ESCAPE
    );
    public Text unbound = Text.literal("Not Bound").withColor(0xffffff);
    public Text binding = Text.literal("Press Key...").withColor(0xffffff);
    public boolean isBinding = false;

    public KeybindButton() {
        super(Text.empty(), button -> {
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
    public Text getKeyLabel(int keycode) {
        return staticGetKeyLabel(keycode);
    }

    // Static variant for use by other classes (e.g., SettingKeybind)
    public static Text staticGetKeyLabel(int keycode) {
        // explicit quick mapping for common printable keys and navigation keys
        // letters
        if (keycode >= GLFW.GLFW_KEY_A && keycode <= GLFW.GLFW_KEY_Z) {
            char c = (char) ('A' + (keycode - GLFW.GLFW_KEY_A));
            return Text.literal(String.valueOf(c)).formatted(net.minecraft.util.Formatting.WHITE);
        }
        // numbers row
        if (keycode >= GLFW.GLFW_KEY_0 && keycode <= GLFW.GLFW_KEY_9) {
            char c = (char) ('0' + (keycode - GLFW.GLFW_KEY_0));
            return Text.literal(String.valueOf(c)).formatted(net.minecraft.util.Formatting.WHITE);
        }
        // numpad
        if (keycode >= GLFW.GLFW_KEY_KP_0 && keycode <= GLFW.GLFW_KEY_KP_9) {
            int n = keycode - GLFW.GLFW_KEY_KP_0;
            return Text.literal("Num " + n).formatted(net.minecraft.util.Formatting.WHITE);
        }
        // arrows and navigation
        switch (keycode) {
            case GLFW.GLFW_KEY_LEFT: return Text.literal("Left").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT: return Text.literal("Right").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_UP: return Text.literal("Up").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_DOWN: return Text.literal("Down").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_UP: return Text.literal("Page Up").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_DOWN: return Text.literal("Page Down").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_HOME: return Text.literal("Home").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_END: return Text.literal("End").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_INSERT: return Text.literal("Insert").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_DELETE: return Text.literal("Delete").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_BACKSPACE: return Text.literal("Backspace").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_ENTER: return Text.literal("Enter").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_TAB: return Text.literal("Tab").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_SPACE: return Text.literal("Space").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_ESCAPE: return Text.literal("Escape").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return Text.literal("` / ~").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_COMMA: return Text.literal(",").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_PERIOD: return Text.literal(".").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_SLASH: return Text.literal("/").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_SEMICOLON: return Text.literal(";").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_APOSTROPHE: return Text.literal("'").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_LEFT_BRACKET: return Text.literal("[").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return Text.literal("]").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_MINUS: return Text.literal("-").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_EQUAL: return Text.literal("=").formatted(net.minecraft.util.Formatting.WHITE);
            case GLFW.GLFW_KEY_BACKSLASH: return Text.literal("\\").formatted(net.minecraft.util.Formatting.WHITE);
        }
        // function keys
        if (keycode >= GLFW.GLFW_KEY_F1 && keycode <= GLFW.GLFW_KEY_F25) {
            int f = keycode - GLFW.GLFW_KEY_F1 + 1;
            return Text.literal("F" + f).formatted(net.minecraft.util.Formatting.WHITE);
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
                    if (name.length() == 1) return Text.literal(name.toUpperCase()).formatted(net.minecraft.util.Formatting.WHITE);
                    return Text.literal(name.replace('_', ' ')).formatted(net.minecraft.util.Formatting.WHITE);
                }
             }
         } catch (Throwable ignored) {}

        // 2) Keyboard localized text via InputUtil
        try {
            InputUtil.Key input = InputUtil.Type.KEYSYM.createFromCode(keycode);
            String localized = input.getLocalizedText().getString();
            String translation = input.getTranslationKey();
            if (localized != null && !localized.isEmpty() && !localized.equals(translation)) {
                if (!localized.matches("\\d+")) {
                    return input.getLocalizedText();
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
                return Text.literal(pretty).formatted(net.minecraft.util.Formatting.WHITE);
            }
        } catch (Throwable ignored) {}

        // 3) Mouse fallback
        try {
            InputUtil.Key mouse = InputUtil.Type.MOUSE.createFromCode(keycode);
            String mouseLabel = mouse.getLocalizedText().getString();
            String mouseTrans = mouse.getTranslationKey();
            if (mouseLabel != null && !mouseLabel.isEmpty() && !mouseLabel.equals(mouseTrans)) {
                return mouse.getLocalizedText();
            }
            if (keycode >= GLFW.GLFW_MOUSE_BUTTON_1 && keycode <= GLFW.GLFW_MOUSE_BUTTON_8) {
                int idx = keycode - GLFW.GLFW_MOUSE_BUTTON_1 + 1;
                return Text.literal("Mouse " + idx).formatted(net.minecraft.util.Formatting.WHITE);
            }
        } catch (Throwable ignored) {}

        return Text.literal("Unknown").formatted(net.minecraft.util.Formatting.WHITE);
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
            Text label = getKeyLabel(key);
            String s = label.getString();
            if ("Unknown".equals(s)) {
                // Try ASCII fallback (show printable char instead of a number)
                if (key >= 32 && key <= 126) {
                    char ch = (char) key;
                    this.setMessage(Text.literal(String.valueOf(ch)).formatted(net.minecraft.util.Formatting.WHITE));
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
