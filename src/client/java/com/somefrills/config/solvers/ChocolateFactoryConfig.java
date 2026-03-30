package com.somefrills.config.solvers;

import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ChocolateFactoryConfig {
    @ConfigOption(name = "Enabled", desc = "Enable the chocolate factory helper")
    @ConfigEditorBoolean
    public Property<Boolean> enabled;

    @ConfigOption(name = "Claim Stray Rabbits", desc = "Automatically claim stray rabbits in Chocolate Factory menu")
    @ConfigEditorBoolean
    public boolean claimStray = true;

    @ConfigOption(name = "Claim Delay", desc = "Delay between claim attempts in milliseconds")
    public int claimDelay = 100;

}
