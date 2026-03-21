package com.somefrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.events.*;
import com.somefrills.misc.SkyblockData;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent instanceof LivingEntity || ent instanceof ItemEntity) {
            if (ent instanceof ArmorStandEntity) {
                for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
                    if (entry.handler().equals(TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) && entry.value() != null) {
                        ((Optional<Text>) entry.value()).ifPresent(value -> eventBus.post(new EntityNamedEvent(ent, value)));
                        break;
                    }
                }
            }
            eventBus.post(new EntityUpdatedEvent(ent));
        }
    }

    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;playSpawnSound(Lnet/minecraft/entity/Entity;)V"))
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityUpdatedEvent(ent));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onUpdateInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            eventBus.post(new SlotUpdateEvent(packet, container, container.getScreenHandler(), packet.getSlot()));
        } else if (mc.currentScreen == null) {
            eventBus.post(new InventoryUpdateEvent(packet, packet.getStack(), packet.getSlot()));
        }
    }

    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new PlaySoundEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new SpawnParticleEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("TAIL"))
    private void onObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        SkyblockData.updateObjective();
    }

    @Inject(method = "onTeam", at = @At("TAIL"))
    private void onScoreUpdate(TeamS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markScoreboardDirty();
    }

    @Inject(method = "onPlayerList", at = @At("TAIL"))
    private void onTabListUpdate(PlayerListS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markTabListDirty();
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoinGame(GameJoinS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ServerJoinEvent());
    }
}