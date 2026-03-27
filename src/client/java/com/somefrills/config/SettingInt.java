package com.somefrills.config;

public class SettingInt extends SettingGeneric {

    public SettingInt(int defaultValue) {
        super(defaultValue);
    }

    public SettingInt(int defaultValue, String description) {
        super(defaultValue);
        this.setDescription(description);
    }


    public int value() {
        return this.get().getAsInt();
    }
}