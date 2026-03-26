package com.somefrills.hud.clickgui.components;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.somefrills.Main.mc;

public class FlatSlider {
    private final EditBox box;
    private double min = 0, max = 100, step = 1;

    public FlatSlider(int trackColor, int sliderColor) {
        this.box = new EditBox(mc.font, 0,0,80,20, Component.empty());
    }

    public void min(double v) { this.min = v; }
    public void max(double v) { this.max = v; }
    public void stepSize(double s) { this.step = s; }
    public void horizontalSizing(Object o) { /* placeholder */ }
    public void verticalSizing(Object o) { /* placeholder */ }
    public void value(double v) { this.box.setValue(String.valueOf(v)); }
    public double value() {
        try {
            double v = Double.parseDouble(this.box.getValue());
            // clamp to min/max
            v = Math.max(min, Math.min(max, v));
            // quantize to step if step > 0
            if (step > 0) {
                v = min + Math.round((v - min) / step) * step;
            }
            return v;
        } catch (Exception e) {
            return min;
        }
    }
    public void onChanged(Consumer<Double> c) {
        this.box.setResponder(s -> {
            try {
                double v = Double.parseDouble(s);
                // clamp
                v = Math.max(min, Math.min(max, v));
                if (step > 0) v = min + Math.round((v - min) / step) * step;
                // update the box with the clamped/quantized value so the UI reflects it
                this.box.setValue(String.valueOf(v));
                c.accept(v);
            } catch (Exception ignored) {}
        });
    }
    public EditBox getEditBox() { return box; }
}
