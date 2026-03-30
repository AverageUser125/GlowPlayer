package com.somefrills.config.solvers;

import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ExperimentSolverConfig {
    @ConfigOption(name = "Enabled", desc = "Enable the experiment solver")
    @ConfigEditorBoolean
    public Property<Boolean> enabled;

    @ConfigOption(name = "Chronomatron", desc = "Automatically solve the Chronomatron")
    @ConfigEditorBoolean
    public boolean chronomatron = true;

    @ConfigOption(name = "Ultrasequencer", desc = "Automatically solve the Ultrasequencer")
    @ConfigEditorBoolean
    public boolean ultrasequencer = false;

    @ConfigOption(name = "Click Delay", desc = "Click delay")
    @ConfigEditorSlider(minValue = 0, maxValue = 1000, minStep = 50)
    public int clickDelay = 400;

    @ConfigOption(name = "Close Chronomatron", desc = "Close menu at threshold")
    @ConfigEditorBoolean
    public boolean closeOnChronomatronThreshold = true;

    @ConfigOption(name = "Chronomatron Threshold", desc = "N means close after N-1 clicks")
    @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
    public int chronomatronThreshold = 10;

    @ConfigOption(name = "Close Ultrasequencer", desc = "Close menu at threshold")
    @ConfigEditorBoolean
    public boolean closeOnUltrasequencerThreshold = true;

    @ConfigOption(name = "Ultrasequencer Threshold", desc = "N means close after N-1 clicks")
    @ConfigEditorSlider(minValue = 1, maxValue = 50, minStep = 1)
    public int ultrasequencerThreshold = 7;
}
