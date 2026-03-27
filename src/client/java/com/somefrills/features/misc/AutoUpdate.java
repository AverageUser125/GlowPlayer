package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingDescription;
import com.somefrills.events.ServerJoinEvent;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.misc.Utils.*;
import static net.fabricmc.loader.impl.FabricLoaderImpl.MOD_ID;

public class AutoUpdate {
    public static final Feature instance = new Feature("autoUpdate", true);

    @SettingDescription("Check for updates when joining a server.")
    public static final SettingBool checkOnJoin = new SettingBool(true);

    private static boolean hasCheckedThisSession = false;

    @EventHandler
    public static void onServerJoin(ServerJoinEvent event) {
        if (checkOnJoin.value()) {
            checkUpdate();
        }
    }

    private static int getVersionNumber(String version) {
        String[] numbers = version.split("\\.");
        if (numbers.length >= 3) {
            return parseInt(numbers[0]).orElse(0) * 1000 + parseInt(numbers[1]).orElse(0) * 100 + parseInt(numbers[2]).orElse(0);
        }
        return 0;
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession || true) return;
        hasCheckedThisSession = true;
        Thread.startVirtualThread(() -> {
            try {
                String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
                InputStream connection = URI.create("https://raw.githubusercontent.com/AverageUser125/SomeFrills/refs/heads/main/gradle.properties").toURL().openStream();
                for (String line : IOUtils.toString(connection, StandardCharsets.UTF_8).split("\n")) {
                    if (line.startsWith("mod_version=")) {
                        String newest = line.replace("mod_version=", "");
                        if (getVersionNumber(newest) > getVersionNumber(version)) {
                            infoLink(format("§a§lNew version available! §aClick here to open the Github releases page. §7Current: {}, Newest: {}", version, newest),
                                    "https://github.com/AverageUser125/SomeFrills/releases");
                            return;
                        }
                    }
                }
            } catch (IOException exception) {
                info("§cAn error occurred while checking for an update. Additional information can be found in the log.");
                LOGGER.error("SomeFrills update check failed.", exception);
            }
        });
    }
}
