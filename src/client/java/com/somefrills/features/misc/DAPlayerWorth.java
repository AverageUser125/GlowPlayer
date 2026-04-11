package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.misc.HypixelApiClient;
import com.somefrills.misc.SkyblockProfile;
import com.somefrills.misc.Utils;

import static com.somefrills.Main.mc;

public class DAPlayerWorth extends Feature {
    public DAPlayerWorth() {
        super(FrillsConfig.instance.misc.daPlayerWorth.enabled);
    }
    private static void onProfile(SkyblockProfile profile) {
        if (profile == null) {
            Utils.sendMessage("Failed to fetch your financial data.");
            return;
        }

        profile.getTotalWealth().thenAccept(wealth -> {
            if (wealth == null || wealth <= 0) {
                Utils.sendMessage("Failed to fetch your financial data.");
                return;
            }

            profile.getPurse().thenAccept(purse -> {
                profile.getTotalBank().thenAccept(bank -> {
                    String message = String.format("Your worth: Purse=%d, Bank=%d, Total=%d",
                            purse != null ? purse : 0,
                            bank != null ? bank : 0,
                            wealth);
                    Utils.sendMessage(message);
                });
            });
        }).exceptionally(ex -> {
            Utils.sendMessage("Failed to fetch your financial data.");
            return null;
        });
    }
    public static void startFetching() {
        if (mc.player == null) return;
        var profileFuture = HypixelApiClient.fetchPlayerFinancials(mc.player.getUuid());

        profileFuture.thenAccept(DAPlayerWorth::onProfile)
        .exceptionally(ex -> null);
    }
}
