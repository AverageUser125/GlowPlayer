package com.somefrills.config;

public class SettingBool extends SettingGeneric {
    public SettingBool(boolean defaultValue) {
        super(defaultValue);
    }

    public SettingBool(boolean defaultValue, String description) {
        super(defaultValue);
        this.setDescription(description);
    }


    public boolean value() {
        return this.get().getAsBoolean();
    }
}