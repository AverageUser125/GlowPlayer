package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.somefrills.Main.mc;

/**
 * Simple integer-only edit box widget. Uses UILib's EditBoxWidget so it
 * integrates with the validation tooltip machinery. The widget exposes
 * getNumber/setNumber and validates input to ensure only integers are accepted.
 */
public class NumberInt extends EditBoxWidget {
    private int num;

    public NumberInt(int number) {
        this(0, 0, number);
    }

    public NumberInt(int x, int y, int number) {
        this(x, y, Button.DEFAULT_WIDTH, number);
    }

    public NumberInt(int x, int y, int width, int number) {
        this(x, y, width, Button.DEFAULT_HEIGHT, number);
    }

    public NumberInt(int x, int y, int width, int height, int number) {
        super(mc.font, x, y, width, height, Component.empty());
        this.num = number;
        this.setValue(Integer.toString(number));
    }

    public int getNumber() {
        return this.num;
    }

    public void setNumber(int number) {
        this.num = number;
        this.setValue(Integer.toString(number));
    }

    void onValueChange(Consumer<Integer> callback) {
        this.setResponder(str -> {
            List<Component> errors = validateInput(str);
            if (errors.isEmpty()) {
                callback.accept(Integer.parseInt(str.trim()));
            }
        });
    }

    @Override
    public List<Component> validateInput(String input) {
        List<Component> errors = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            errors.add(Component.literal("Please enter a number"));
            return errors;
        }
        try {
            Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            errors.add(Component.literal("Not a valid integer"));
        }
        return errors;
    }
}
