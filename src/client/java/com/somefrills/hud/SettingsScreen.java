package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.*;
import com.somefrills.hud.components.*;
import net.minecraft.network.chat.Component;

import static com.somefrills.Main.mc;

public class SettingsScreen extends AbstractScreen {
    private final FeatureRegistry.FeatureInfo info;

    public SettingsScreen(FeatureRegistry.FeatureInfo info) {
        super(Component.literal(info.name + " Settings"));
        this.info = info;
    }

    @Override
    protected void init() {
        super.init();
        for(FeatureRegistry.SettingInfo entry : info.settings) {
            var setting = entry.settingInstance;
            var clazz = setting.getClass();
            if (clazz.equals(SettingBool.class)) {
                var s = (SettingBool) setting;
                addRenderableWidget(new ToggleButton(10, 10, 100, 20, s.value()));
            } else if(clazz.equals(SettingKeybind.class)) {
                var s = (SettingKeybind) setting;
                addRenderableWidget(new KeybindButton(10, 10, 100, 20, s.value()));
            } else if(clazz.equals(SettingInt.class)) {
                var s = (SettingInt) setting;
                addRenderableWidget(new NumberInt(10, 10, 100, 20, s.value()));
            } else if(clazz.equals(SettingDouble.class)) {
                var s = (SettingDouble) setting;
                addRenderableWidget(new NumberDouble(10, 10, 100, 20, s.value()));
            } else if(clazz.equals(SettingColor.class)) {
                // TODO
                //var s = (SettingColor) setting;
                //addRenderableWidget(new ColorPicker(10, 10, 100, 20, s.value()));
            } else if(clazz.equals(SettingEnum.class)) {
                // TODO
            } else if(clazz.equals(SettingBlockPosList.class)) {
                // TODO
            } else if(clazz.equals(SettingString.class)) {
                var s = (SettingString) setting;
                addRenderableWidget(new EditBoxWidget(mc.font, 10, 10, 100, 20, Component.literal(s.value())));
            } else if(clazz.equals(SettingJson.class)){
                // TODO
            } else {
                // Unsupported setting type
            }
        }

    }
}
