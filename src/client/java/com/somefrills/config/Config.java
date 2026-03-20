package com.somefrills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.somefrills.misc.Utils;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.somefrills.Main.LOGGER;

public class Config {
    private static final Path folderPath = FabricLoader.getInstance().getConfigDir().resolve("SomeFrills");
    private static final Path filePath = folderPath.resolve("Configuration.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject data = new JsonObject();
    private static int hash = 0;

    public static Path getFolderPath() {
        return folderPath;
    }

    public static void load() {
        if (Files.exists(filePath)) {
            try {
                data = JsonParser.parseString(Files.readString(filePath)).getAsJsonObject();
            } catch (Exception exception) {
                LOGGER.error("Unable to load SomeFrills config file!", exception);
            }
        } else {
            save();
        }
        computeHash();
    }

    public static void save() {
        try {
            int currentHash = data.hashCode();

            // kip saving if nothing changed
            if (currentHash == hash) {
                return;
            }

            Utils.atomicWrite(filePath, GSON.toJson(data));

            hash = currentHash;

        } catch (Exception exception) {
            LOGGER.error("Unable to save SomeFrills config file!", exception);
        }
    }

    public static void saveAsync() {
        Thread.startVirtualThread(Config::save);
    }

    public static int getHash() {
        return hash;
    }

    public static void computeHash() {
        hash = data.hashCode();
    }

    public static JsonObject get() {
        return data;
    }
}