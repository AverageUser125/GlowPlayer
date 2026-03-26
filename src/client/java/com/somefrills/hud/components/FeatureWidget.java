package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.FeatureRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class FeatureWidget extends ButtonWidget {
    private final FeatureRegistry.FeatureInfo info;

    public FeatureWidget(int x, int y, int width, int height, FeatureRegistry.FeatureInfo info) {
        super(x, y, width, height, Component.literal(info.name), button -> {
            var btn = (FeatureWidget) button;
            btn.updateState();
        }, Button.DEFAULT_NARRATION);;
        this.info = info;
        setTooltip(Tooltip.create(Component.literal(info.description).withStyle(ChatFormatting.GRAY)));
    }

    private void updateState() {
        info.featureInstance.setActive(!info.featureInstance.isActive());
        FeatureRegistry.subscribeFeature(info.featureInstance);
        if(info.featureInstance.isActive()) {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.GREEN));
        } else {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.RED));
        }
    }
}
