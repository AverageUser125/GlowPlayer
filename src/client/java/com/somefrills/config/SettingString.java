package com.somefrills.config;

public class SettingString extends SettingGeneric {
    public SettingString(String defaultValue) {
        super(defaultValue);
    }

    public SettingString(String defaultValue, String description) {
        super(defaultValue);
        this.setDescription(description);
    }


    public String value() {
        return this.get().getAsString();
    }
}