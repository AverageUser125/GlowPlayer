package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.UpdateManager;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.misc.Utils.*;

public class AutoUpdate extends Feature {

    private static boolean hasCheckedThisSession = false;

    public AutoUpdate() {
        super(FrillsConfig.instance.misc.autoUpdate.enabled);
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        checkUpdate();
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession) return;
        hasCheckedThisSession = true;

        if (!FrillsConfig.instance.misc.autoUpdate.autoDownload) {
            LOGGER.debug("Auto download is disabled");
            return;
        }

        // Start the update check asynchronously
        UpdateManager.checkUpdate();

        // Monitor for completion and handle result
        monitorUpdateState();
    }

    private static void monitorUpdateState() {
        new Thread(() -> {
            try {
                // Wait a bit for the update check to complete
                Thread.sleep(1000);

                if (UpdateManager.getUpdateState() == UpdateManager.UpdateState.AVAILABLE) {
                    String nextVersion = UpdateManager.getLatestVersion();

                    // Show chat message if enabled
                    if (FrillsConfig.instance.misc.autoUpdate.showChatMessage && nextVersion != null) {
                        infoLink(
                                "§a§lNew version available! §aCheck the Github releases page. §7Version: " + nextVersion,
                                "https://github.com/AverageUser125/SomeFrills/releases/tag/v" + nextVersion
                        );
                    }

                    // Auto apply if enabled
                    if (FrillsConfig.instance.misc.autoUpdate.autoApply) {
                        LOGGER.info("Auto-applying update...");
                        UpdateManager.queueUpdate();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}