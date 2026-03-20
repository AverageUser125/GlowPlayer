package com.somefrills.hud;

import com.google.common.collect.Lists;
import com.somefrills.config.Config;
import com.somefrills.features.farming.SpaceFarmer;
import com.somefrills.hud.components.FlatTextbox;
import com.somefrills.hud.components.PlainLabel;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;

    private boolean matchSearch(String text, String search) {
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() != GLFW.GLFW_KEY_LEFT && input.key() != GLFW.GLFW_KEY_RIGHT && input.key() != GLFW.GLFW_KEY_PAGE_DOWN && input.key() != GLFW.GLFW_KEY_PAGE_UP) {
            return super.keyPressed(input);
        } else {
            for (Category category : this.categories) {
                for (Module module : category.features) {
                    if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                        return category.scroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
                    }
                }
            }
            return this.mainScroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Category category : this.categories) {
            for (Module module : category.features) {
                if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                    return category.scroll.onMouseScroll(0, 0, verticalAmount * 2);
                }
            }
        }
        return this.mainScroll.onMouseScroll(0, 0, verticalAmount * 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int height = context.getScaledWindowHeight();
        context.drawTextWithShadow(this.textRenderer, "Left click a feature to toggle", 1, height - 20, RenderColor.white.argb);
        context.drawTextWithShadow(this.textRenderer, "Right click a feature open its settings", 1, height - 10, RenderColor.white.argb);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        this.categories = Lists.newArrayList(
                new Category("Farming", List.of(
                        new Module("Space Farmer", SpaceFarmer.instance, "Allows you to farm by holding space bar, sneak and press space to activate.\nThis feature will also lock your view once you start holding space."),
                        new Module("Rewarp", SpaceFarmer.instance, "Warps to the start of the farm when done.")
                )));

        this.categories.getLast().margins(Insets.of(5, 0, 3, 3));
        for (Category category : this.categories) {
            parent.child(category);
        }
        this.mainScroll = Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        root.child(this.mainScroll);
        FlatTextbox searchBox = new FlatTextbox(Sizing.fixed(200));
        searchBox.setSuggestion("Search...");
        searchBox.margins(Insets.of(0, 3, 0, 0));
        searchBox.positioning(Positioning.relative(50, 100));
        searchBox.onChanged().subscribe(value -> {
            if (value.isEmpty()) {
                searchBox.setSuggestion("Search...");
                for (Category category : this.categories) {
                    category.scroll.child().clearChildren();
                    for (Module module : category.features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            } else {
                searchBox.setSuggestion("");
                for (Category category : this.categories) {
                    List<Module> features = new ArrayList<>(category.features);
                    features.removeIf(feature -> {
                        if (matchSearch(feature.label.getText(), value) || matchSearch(feature.label.getTooltip(), value)) {
                            return false;
                        }
                        if (feature.options != null) {
                            for (FlowLayout setting : feature.options.settings) {
                                for (Component child : setting.children()) {
                                    if (child instanceof PlainLabel label) {
                                        if (matchSearch(label.getText(), value) || matchSearch(label.getTooltip(), value)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    });
                    category.scroll.child().clearChildren();
                    for (Module module : features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            }
        });
        root.child(searchBox);
    }

    @Override
    public void close() {
        Config.save();
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.close();
    }
}
