package com.somefrills.config;

import com.google.gson.JsonObject;
import com.somefrills.Main;

public class Feature {
    public String key;
    private boolean value = false;
    private final boolean defaultEnabled;

    public Feature(String key) {
        this(key, false);
    }

    public Feature(String key, boolean defaultEnabled) {
        this.key = key;
        this.defaultEnabled = defaultEnabled;
        this.value = this.isActive();
    }

    public String key() {
        return this.key;
    }

    /**
     * Refresh the feature's value from the current config. If the config does not contain the
     * feature key, the feature's default is used.
     */
    public void update() {
        if (Config.get().has(this.key)) {
            JsonObject data = Config.get().getAsJsonObject(this.key);
            this.value = data.has("enabled") && data.get("enabled").getAsBoolean();
        } else {
            this.value = this.defaultEnabled;
        }
    }

    public boolean isActive() {
        this.update();
        return this.value;
    }

    public void setActive(boolean toggle) {
        if (!Config.get().has(this.key)) {
            Config.get().add(this.key, new JsonObject());
        }
        this.value = toggle;
        Config.get().get(this.key).getAsJsonObject().addProperty("enabled", this.value);
    }
}