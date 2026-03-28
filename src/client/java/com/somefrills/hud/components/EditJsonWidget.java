package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.SettingJson;
import net.minecraft.text.Text;

import java.util.List;

import static com.somefrills.Main.mc;

public class EditJsonWidget extends EditBoxWidget implements IWidget {
    private final SettingJson setting;

    public EditJsonWidget(int x, int y, int width, int height, SettingJson set) {
        super(mc.textRenderer, x, y, width, height, Text.empty());
        this.setting = set;
        setText(cleanup(setting.value().toString()));
        this.setChangedListener(str -> {
            setting.set(setting.parse(str));
        });
    }

    // removes all LF, CR
    private static String cleanup(String str) {
        return str.replaceAll("[\\n\\r]", "");
    }

    public List<Text> validateInput(String input) {
        try {
            setting.parse(input);
            return List.of();
        } catch (Exception e) {
            return List.of(Text.literal("Invalid JSON: " + e.getMessage()));
        }
    }

}