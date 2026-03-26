package com.somefrills.events;

import net.minecraft.network.protocol.Packet;

public class ReceivePacketEvent extends Cancellable {
    public Packet<?> packet;

    public ReceivePacketEvent(Packet<?> packet) {
        this.setCancelled(false);
        this.packet = packet;
    }
}
