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

    public Text getKeyLabel(int keycode) {
        // Mouse buttons
        if (keycode >= GLFW.GLFW_MOUSE_BUTTON_1 && keycode <= GLFW.GLFW_MOUSE_BUTTON_8) {
            int idx = keycode - GLFW.GLFW_MOUSE_BUTTON_1 + 1;
            return Text.literal("Mouse " + idx).formatted(net.minecraft.util.Formatting.WHITE);
        }

        // Special-case the grave/tilde key for a nicer label
        if (keycode == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            return Text.literal("` / ~").formatted(net.minecraft.util.Formatting.WHITE);
        }

        InputUtil.Key input = InputUtil.Type.KEYSYM.createFromCode(keycode);
        // Some keys lack a localized name — fall back to mouse representation if applicable
        String localized = input.getLocalizedText().getString();
        if (localized == null || localized.isEmpty() || localized.equals(input.getTranslationKey())) {
            // try mouse fallback
            InputUtil.Key mouse = InputUtil.Type.MOUSE.createFromCode(keycode);
            String mouseLabel = mouse.getLocalizedText().getString();
            if (mouseLabel != null && !mouseLabel.isEmpty() && !mouseLabel.equals(mouse.getTranslationKey())) {
                return mouse.getLocalizedText();
            }
            // fallback to a generic label
            return Text.literal("Key " + keycode).formatted(net.minecraft.util.Formatting.WHITE);
        }
        return input.getLocalizedText();
    }

    public void bind(int key) {
        if (!this.valid(key)) {
            this.setMessage(this.unbound);
            changedEvents.sink().onBind(GLFW.GLFW_KEY_UNKNOWN);
        } else {
            this.setMessage(getKeyLabel(key));
            changedEvents.sink().onBind(key);
        }
        this.isBinding = false;
    }

    public void clearBinding() {
        this.bind(GLFW.GLFW_KEY_UNKNOWN);
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

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        // Right click clears binding when not currently binding
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_2 && !this.isBinding) {
            this.clearBinding();
            return true;
        }
        return super.mouseClicked(click, doubleClick);
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
