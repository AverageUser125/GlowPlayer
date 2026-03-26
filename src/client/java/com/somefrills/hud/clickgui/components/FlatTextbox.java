package com.somefrills.hud.clickgui.components;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.somefrills.Main.mc;

public class FlatTextbox {
    private final EditBox box;

    public FlatTextbox(int width) {
        this.box = new EditBox(mc.font, 0,0, width, 20, Component.empty());
    }

    public void setValue(String v) { this.box.setValue(v); }
    public String getValue() { return this.box.getValue(); }
    public void setMaxLength(int l) { this.box.setMaxLength(l); }
    public void onChanged(Consumer<String> c) { this.box.setResponder(c::accept); }
    public void text(String t) { this.setValue(t); }
    public EditBox getEditBox() { return this.box; }
}
