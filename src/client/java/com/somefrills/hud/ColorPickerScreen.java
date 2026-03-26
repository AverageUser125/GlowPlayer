package com.somefrills.hud;

import com.somefrills.config.SettingColor;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static com.somefrills.Main.mc;

/**
 * Simpler ColorPickerScreen that uses vanilla widgets to edit a SettingColor.
 * Not feature-complete compared to the previous OWO-based implementation but functional.
 */
public class ColorPickerScreen extends Screen {
    private final Screen previous;
    private final SettingColor setting;
    private EditBox argbBox;
    private EditBox rBox, gBox, bBox, aBox;

    public ColorPickerScreen(SettingColor setting, Screen previous) {
        super(Component.literal("Color Picker"));
        this.previous = previous;
        this.setting = setting;
    }

    @Override
    protected void init() {
        super.init();
        int x = 10;
        int y = 10;
        int w = Math.max(100, this.width - 20);

        argbBox = new EditBox(this.font, x, y, 200, 20, Component.literal("ARGB"));
        argbBox.setValue("0x" + Integer.toHexString(setting.value().argb));
        argbBox.setResponder(v -> Utils.parseHex(v).ifPresent(i -> setting.set(RenderColor.fromArgb(i))));
        this.addRenderableWidget(argbBox);
        y += 26;

        rBox = new EditBox(this.font, x, y, 40, 20, Component.literal("R"));
        rBox.setValue(String.valueOf((int) (setting.value().r * 255)));
        rBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> setting.set(setting.value().withRed(i / 255.0f))));
        this.addRenderableWidget(rBox);
        gBox = new EditBox(this.font, x + 46, y, 40, 20, Component.literal("G"));
        gBox.setValue(String.valueOf((int) (setting.value().g * 255)));
        gBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> setting.set(setting.value().withGreen(i / 255.0f))));
        this.addRenderableWidget(gBox);
        bBox = new EditBox(this.font, x + 92, y, 40, 20, Component.literal("B"));
        bBox.setValue(String.valueOf((int) (setting.value().b * 255)));
        bBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> setting.set(setting.value().withBlue(i / 255.0f))));
        this.addRenderableWidget(bBox);
        aBox = new EditBox(this.font, x + 138, y, 40, 20, Component.literal("A"));
        aBox.setValue(String.valueOf((int) (setting.value().a * 255)));
        aBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> setting.set(setting.value().withAlpha(i / 255.0f))));
        this.addRenderableWidget(aBox);
        y += 26;

        // Save & back button (use uilib ButtonWidget to avoid constructor mismatch)
        com.daqem.uilib.gui.widget.ButtonWidget back = new com.daqem.uilib.gui.widget.ButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, Component.literal("Back"), btn -> {
            com.somefrills.config.Config.save();
            mc.setScreen(this.previous);
        });
        this.addRenderableWidget(back);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        // Draw a color preview
        int px = 10; int py = 10; int pw = 200; int ph = 10;
        RenderColor color = setting.value();
        int argb = color.argb;
        graphics.fill(px + 220, py, px + 260, py + 20, argb);
    }

    @Override
    public void onClose() {
        mc.setScreen(this.previous);
        super.onClose();
    }
}
