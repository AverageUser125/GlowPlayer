package com.somefrills.hud.clickgui.components;

import net.minecraft.network.chat.Component;
import com.daqem.uilib.gui.widget.ButtonWidget;
import java.util.ArrayList;
import java.util.List;

public class ToggleButton {
    private boolean toggle;
    private final List<ToggleChanged> listeners = new ArrayList<>();

    public ToggleButton(boolean initial) {
        this.toggle = initial;
    }

    public ButtonWidget createButton(int x, int y, int width, int height) {
        ButtonWidget btn = new ButtonWidget(x, y, width, height, Component.literal(this.toggle ? "Enabled" : "Disabled"), (b) -> {
            this.setToggle(!this.toggle);
        });
        return btn;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
        for (ToggleChanged l : listeners) l.onToggle(toggle);
    }

    public void addListener(ToggleChanged l) { listeners.add(l); }

    public interface ToggleChanged { void onToggle(boolean newValue); }
}
