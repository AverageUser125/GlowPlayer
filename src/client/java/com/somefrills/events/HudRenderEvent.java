package com.somefrills.events;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

public class HudRenderEvent {
    public GuiGraphics context;
    public Font textRenderer;
    public DeltaTracker tickCounter;

    public HudRenderEvent(GuiGraphics context, Font textRenderer, DeltaTracker tickCounter) {
        this.context = context;
        this.textRenderer = textRenderer;
        this.tickCounter = tickCounter;
    }
}
