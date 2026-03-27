package com.somefrills.config;

import com.google.gson.JsonObject;

public class Feature {
    private final boolean defaultEnabled;
    public String key;
    private boolean value = false;
    private String name = "";
    private String description = "";

    // No-arg constructor: no description provided
    public Feature() {
        this(false, "");
    }

    public Feature(String description) {
        this(false, description);
    }

    public Feature(boolean defaultEnabled) {
        this(defaultEnabled, "");
    }

    // Constructors that accept an explicit description
    // Preferred constructor: defaultEnabled first, then optional description
    public Feature(boolean defaultEnabled, String description) {
        this.description = description == null ? "" : description;
        this.defaultEnabled = defaultEnabled;
        this.key = "";
        this.value = this.isActive();
    }

    public String key() {
        return this.key;
    }

    // Allow the registry to set the machine-readable config key
    public void overrideKey(String newKey) {
        this.key = newKey;
    }

    public String name() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public String description() {
        return this.description == null ? "" : this.description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
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