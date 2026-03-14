package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class ExampleModClient implements ClientModInitializer {

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
