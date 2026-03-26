package com.somefrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import static com.somefrills.Main.mc;

public class PlainLabel extends LabelComponent {

    public String plainTooltip = "";
    public String plainText;

    public PlainLabel(net.minecraft.network.chat.Component text) {
        super(text);
        this.plainText = text.getString();
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        return false;
    }

    @Override
    public Component tooltip(net.minecraft.network.chat.Component tooltip) {
        this.plainTooltip = tooltip.getString().replaceAll("\n", " ");
        return super.tooltip(tooltip);
    }

    @Override
    public LabelComponent text(net.minecraft.network.chat.Component text) {
        this.plainText = text.getString();
        return super.text(text);
    }

    public int getTextHeight() {
        List<FormattedCharSequence> lines = mc.font.split(
                text,
                this.horizontalSizing().get().isContent() ? 0 : this.horizontalSizing().get().value
        );
        return (lines.size() * (this.lineHeight() + this.lineSpacing())) - this.lineSpacing();
    }

    public String getTooltip() {
        return this.plainTooltip;
    }

    public String getText() {
        return plainText;
    }
}