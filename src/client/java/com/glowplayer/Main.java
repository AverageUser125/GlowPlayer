package com.glowplayer;

import com.glowplayer.features.ChocolateFactory;
import com.glowplayer.features.ExperimentIntegration;
import com.glowplayer.features.GlowPlayerCommand;
import com.glowplayer.features.RNGMeterDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class Main implements ClientModInitializer {

    public static MinecraftClient mc;
    private GlowPlayerCommand glowPlayerCommand;
    private ExperimentIntegration experimentIntegration;
    private ChocolateFactory chocolateFactory;
    private RNGMeterDisplay rngMeterDisplay;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        glowPlayerCommand = new GlowPlayerCommand();
        experimentIntegration = new ExperimentIntegration();
        chocolateFactory = new ChocolateFactory();
        rngMeterDisplay = new RNGMeterDisplay();
    }
}
