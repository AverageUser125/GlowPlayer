package com.somefrills.features.update;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.LOGGER;

public class AutoUpdate extends Feature {

    private static boolean hasCheckedThisSession = false;

    public AutoUpdate() {
        super(FrillsConfig.instance.about.checkForUpdates);
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        checkUpdate();
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession) return;
        hasCheckedThisSession = true;

        if (!FrillsConfig.instance.about.checkForUpdates.get()) {
            LOGGER.debug("Check for updates is disabled");
            return;
        }

        LOGGER.debug("Performing automatic update check");
        UpdateManager.checkUpdate(FrillsConfig.instance.about.fullAutoUpdates);
    }
}