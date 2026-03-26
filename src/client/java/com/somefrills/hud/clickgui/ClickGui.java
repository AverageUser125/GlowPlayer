package com.somefrills.hud.clickgui;

import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.*;
import com.somefrills.config.FeatureRegistry.FeatureInfo;
import com.somefrills.hud.clickgui.components.FlatSlider;
import com.somefrills.hud.clickgui.components.FlatTextbox;
import com.somefrills.hud.clickgui.components.KeybindButton;
import com.somefrills.hud.clickgui.components.ToggleButton;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import static com.somefrills.Main.mc;
import com.somefrills.events.InputEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.lwjgl.glfw.GLFW;
import org.joml.Vector2d;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Migrated Click GUI using the new ui-lib's AbstractScreen. This screen builds a simple,
 * fully-working UI using FeatureRegistry as the source of truth. It implements a search box,
 * a left-side category list and a right-side feature list. Left-click toggles a feature; right-click is reserved for settings.
 */
public class ClickGui extends AbstractScreen {
    private final List<CategoryData> categories = new ArrayList<>();

    // transient layout info used for click detection
    private final Map<FeatureInfo, Rect> featureBounds = new HashMap<>();

    public ClickGui() {
        super(Component.literal("SomeFrills - Click GUI"));
    }

    @Override
    protected void init() {
        super.init();

        // Build categories by grouping features by their package segment
        Map<String, List<FeatureInfo>> byCategory = new TreeMap<>();
        for (FeatureInfo info : FeatureRegistry.getFeatures()) {
            String pkg = info.clazz.getPackage() != null ? info.clazz.getPackage().getName() : "";
            String[] parts = pkg.split("\\.");
            String cat = parts.length > 0 ? parts[parts.length - 1] : "misc";
            if ("features".equals(cat) && parts.length > 1) cat = parts[parts.length - 2];
            cat = humanize(cat);
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(info);
        }

        for (Map.Entry<String, List<FeatureInfo>> e : byCategory.entrySet()) {
            this.categories.add(new CategoryData(e.getKey(), e.getValue()));
        }

        // create search box (vanilla EditBox works with ui-lib)
        int sbWidth = 200;
        EditBox searchBox = new EditBox(this.font, 10, 10, sbWidth, 20, Component.literal("Search"));
        searchBox.setValue("");
        searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(searchBox);

        // initial filter
        this.refreshSearchResults("");
    }

    private void onSearchChanged(String value) {
        this.refreshSearchResults(value);
    }

    private String humanize(String in) {
        if (in == null || in.isEmpty()) return "";
        String withSpaces = in.replace('_', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder();
        char prev = ' ';
        for (int i = 0; i < withSpaces.length(); i++) {
            char c = withSpaces.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev))) out.append(' ');
            out.append(c);
            prev = c;
        }
        String res = out.toString().trim();
        if (res.isEmpty()) return res;
        return Character.toUpperCase(res.charAt(0)) + res.substring(1);
    }

    private boolean matchSearch(String text, String search) {
        if (text == null) return false;
        if (search == null || search.isEmpty()) return true;
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    private void refreshSearchResults(String value) {
        for (CategoryData category : this.categories) {
            List<FeatureInfo> features = new ArrayList<>(category.features);
            if (value != null && !value.isEmpty()) {
                features.removeIf(info -> {
                    String name = humanize(info.featureInstance.key());
                    String tooltip = info.featureInstance.key();
                    if (matchSearch(name, value) || matchSearch(tooltip, value)) return false;
                    for (Map.Entry<Field, SettingGeneric> entry : info.settings.entrySet()) {
                        Field f = entry.getKey();
                        String fieldName = humanize(f.getName());
                        String desc = info.descriptions.getOrDefault(f, "");
                        if (matchSearch(fieldName, value) || matchSearch(desc, value)) return false;
                    }
                    return true;
                });
            }
            category.filteredFeatures.clear();
            category.filteredFeatures.addAll(features);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int left = 10;
        int top = 40;
        int gap = 6;

        // draw categories and their features beneath each category
        featureBounds.clear();
        int y = top;
        int fh = 12 + gap;
        for (CategoryData c : this.categories) {
            String txt = c.name + " (" + c.filteredFeatures.size() + "/" + c.features.size() + ")";
            graphics.drawString(this.font, txt, left, y, 0xff5ca0bf);
            y += 12;
            for (FeatureInfo info : c.filteredFeatures) {
                String name = humanize(info.featureInstance.key());
                boolean active = info.featureInstance.isActive();
                int color = active ? 0xffaaffaa : 0xffffffff;
                graphics.drawString(this.font, (active ? "[ON] " : "[OFF] ") + name, left + 8, y, color);
                featureBounds.put(info, new Rect(left + 8, y, 200, fh));
                y += fh;
            }
            y += gap; // extra gap after category
        }

        // status / hint
        int h = this.height;
        graphics.drawString(this.font, "Left click = toggle, Right click = open settings (if available)", 10, h - 20, 0xffffffff);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Debug: log click coordinates to help verify this method is invoked at runtime
        com.somefrills.misc.Utils.infoFormat("ClickGui.mouseClicked: x={} y={} button={}", mouseX, mouseY, button);
        // feature clicks (we render all categories and their features in a single column)
        for (Map.Entry<FeatureInfo, Rect> e : featureBounds.entrySet()) {
            Rect r = e.getValue();
            if (mouseX >= r.x && mouseX <= r.x + r.w && mouseY >= r.y && mouseY <= r.y + r.h) {
                FeatureInfo info = e.getKey();
                if (button == 0) { // left click -> toggle
                    boolean newState = !info.featureInstance.isActive();
                    info.featureInstance.setActive(newState);
                    if (newState) FeatureRegistry.subscribeFeature(info.featureInstance);
                    else FeatureRegistry.unsubscribeFeature(info.featureInstance);
                    return;
                } else if (button == 1) { // right click -> open settings screen
                    SettingsScreen screen = new SettingsScreen(info, this);
                    mc.setScreen(screen);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onInput(InputEvent event) {
        if (mc == null || mc.screen == null) return;
        if (event.isCancelled()) return;
        try {
            // Mouse presses: forward to active screen.mouseClicked(x,y,button)
            if (event.isMouse && event.action == GLFW.GLFW_PRESS) {
                Vector2d mousePos = Utils.getMousePos();
                double mx = mousePos.x;
                double my = mousePos.y;
                int btn = event.key;
                Object screenObj = mc.screen;
                if (screenObj instanceof ClickGui cg) {
                    // Forward the click to our local screen handling. Do not cancel the global
                    // InputEvent; other listeners (including vanilla GUI) should still receive it.
                    cg.mouseClicked(mx, my, btn);
                } else {
                    try {
                        java.lang.reflect.Method m = screenObj.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                        // invoke if available; ignore return value
                        m.invoke(screenObj, mx, my, btn);
                    } catch (NoSuchMethodException ignored) {
                        // no 3-arg mouseClicked on this screen - ignore
                    } catch (Throwable ignored) {
                        // ignore other reflection/invocation issues here
                    }
                }
                return;
            }

            // Keyboard presses: forward to SettingsScreen to support KeybindButton binding
            if (event.isKeyboard && event.action == GLFW.GLFW_PRESS) {
                Object screenObj = mc.screen;
                if (screenObj instanceof SettingsScreen ss) {
                    // Only forward key events if ESC was pressed or a keybind button is actively binding
                    boolean shouldForward = event.key == GLFW.GLFW_KEY_ESCAPE;
                    if (!shouldForward) {
                        if (ss.hasActiveKeybindBinding()) shouldForward = true;
                    }
                    if (shouldForward) {
                        ss.handleKeyEvent(event.key, event.action);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public void onClose() {
        // persist config
        com.somefrills.config.Config.save();
        super.onClose();
    }

    public static class CategoryData {
        public final String name;
        public final List<FeatureInfo> features;
        public final List<FeatureInfo> filteredFeatures = new ArrayList<>();

        public CategoryData(String name, List<FeatureInfo> features) {
            this.name = name;
            this.features = features;
            this.filteredFeatures.addAll(features);
        }
    }

    private static class Rect { int x, y, w, h; Rect(int x,int y,int w,int h){this.x=x;this.y=y;this.w=w;this.h=h;} }


    private static class SettingsScreen extends AbstractScreen {
        private final FeatureRegistry.FeatureInfo info;
        private final AbstractScreen previous;
        // no persistent widget tracking needed
        private final List<KeybindButton> keybindButtons = new ArrayList<>();

        public SettingsScreen(FeatureRegistry.FeatureInfo info, AbstractScreen previous) {
            super(net.minecraft.network.chat.Component.literal("Settings - " + info.featureInstance.key()));
            this.info = info;
            this.previous = previous;
        }

        @Override
        protected void init() {
            super.init();
            int x = 10, y = 10;
            for (Map.Entry<java.lang.reflect.Field, SettingGeneric> entry : this.info.settings.entrySet()) {
                SettingGeneric setting = entry.getValue();

                if (setting.getClass().equals(SettingBool.class)) {
                    SettingBool sb = (SettingBool) setting;
                    ToggleButton t = new ToggleButton(sb.value());
                    ButtonWidget btn = t.createButton(x, y, 80, 20);
                    t.addListener(sb::set);
                    this.addRenderableWidget(btn);
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingEnum.class)) {
                    try {
                        SettingEnum<?> se = (SettingEnum<?>) setting;
                        Object[] vals = se.values;
                        int startIdx = 0;
                        for (int i = 0; i < vals.length; i++) if (vals[i].equals(se.value())) { startIdx = i; break; }
                        final int[] idx = new int[] { startIdx };
                        ButtonWidget eb = new ButtonWidget(x, y, 100, 20, Component.literal(vals[idx[0]].toString()), btn -> {
                            idx[0] = (idx[0] + 1) % vals.length;
                            Object newVal = vals[idx[0]];
                            try { se.set(newVal); } catch (Throwable ignored) {}
                            btn.setMessage(Component.literal(newVal.toString()));
                        });
                        this.addRenderableWidget(eb);
                    } catch (Throwable ignored) {}
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingDouble.class) || setting.getClass().equals(SettingInt.class) || setting.getClass().equals(SettingIntSlider.class)) {
                    FlatSlider slider = new FlatSlider(0xff888888, 0xffffffff);
                    if (setting.getClass().equals(SettingDouble.class)) {
                        SettingDouble sd = (SettingDouble) setting;
                        // sensible defaults for doubles
                        slider.min(0);
                        slider.max(100);
                        slider.stepSize(0.1);
                        slider.value(sd.value());
                        slider.onChanged(sd::set);
                    } else if (setting.getClass().equals(SettingIntSlider.class)) {
                        // integer slider with explicit bounds
                        SettingIntSlider sis = (SettingIntSlider) setting;
                        slider.min(sis.min());
                        slider.max(sis.max());
                        slider.stepSize(1);
                        slider.value(sis.value());
                        slider.onChanged(d -> sis.set((int) Math.round(d)));
                    } else {
                        // plain integer setting — use reasonable defaults
                        SettingInt si = (SettingInt) setting;
                        slider.min(0);
                        slider.max(100);
                        slider.stepSize(1);
                        slider.value(si.value());
                        slider.onChanged(d -> si.set((int) Math.round(d)));
                    }
                    this.addRenderableWidget(slider.getEditBox());
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingKeybind.class)) {
                    SettingKeybind sk = (SettingKeybind) setting;
                    KeybindButton kb = new KeybindButton();
                    ButtonWidget b = kb.createButton(x, y, 100, 20);
                    kb.setBoundKey(sk.value());
                    kb.onBound().subscribe(sk::set);
                    keybindButtons.add(kb);
                    this.addRenderableWidget(b);
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingString.class)) {
                    SettingString ss = (SettingString) setting;
                    FlatTextbox tb = new FlatTextbox(200);
                    tb.setValue(ss.value());
                    tb.onChanged(ss::set);
                    this.addRenderableWidget(tb.getEditBox());
                    y += 24; continue;
                }

                // fallback: plain label describing the setting (no editor available)
                y += 18;
            }
            // add Save & Back
            ButtonWidget save = new ButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, net.minecraft.network.chat.Component.literal("Save & Back"), b -> {
                com.somefrills.config.Config.save();
                mc.setScreen(previous);
            });
            this.addRenderableWidget(save);
        }

        // Returns true if the key event was consumed
        public boolean handleKeyEvent(int key, int action) {
            // ESC closes the settings screen
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                mc.setScreen(previous);
                return true;
            }
            // If any keybind button is awaiting binding, consume this key and bind it
            for (KeybindButton kb : keybindButtons) {
                if (kb.isBinding) {
                    kb.bind(key);
                    return true;
                }
            }
            return false;
        }

        // Public accessor used by ClickGui to decide whether to forward key events
        public boolean hasActiveKeybindBinding() {
            for (KeybindButton kb : keybindButtons) if (kb.isBinding) return true;
            return false;
        }

        // capitalize removed: no longer needed
    }
}



