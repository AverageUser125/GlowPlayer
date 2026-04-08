package com.somefrills.config;

import com.somefrills.Main;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::getConfigScreen;
    }

    private static Screen getConfigScreen(Screen previous) {
        var editor = Main.config.getEditor();
        GuiContext guiContext = new GuiContext(new GuiElementComponent(editor));
        return new MoulConfigScreenComponent(Text.empty(), guiContext, previous);
    }
}