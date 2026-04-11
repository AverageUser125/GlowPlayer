package com.somefrills.misc;

import com.somefrills.Main;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.somefrills.Main.mc;

/**
 * Utility class for retrieving financial data from all players in the current lobby.
 * Supports both batch fetching and streaming progressive results as data arrives.
 */
public class LobbyFinancialUtils {

    /**
     * Gets all player UUIDs currently visible in the player list
     *
     * @return List of UUIDs of players in the lobby
     */
    public static List<UUID> getLobbyPlayerUuids() {
        List<UUID> uuids = new ArrayList<>();

        if (mc.inGameHud == null) {
            Main.LOGGER.info("[LobbyFinancialUtils] inGameHud is null");
            return uuids;
        }

        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        if (playerListHud == null) {
            Main.LOGGER.info("[LobbyFinancialUtils] playerListHud is null");
            return uuids;
        }

        for (PlayerListEntry entry : playerListHud.collectPlayerEntries()) {
            if (entry != null && entry.getProfile() != null) {
                uuids.add(entry.getProfile().id());
            }
        }
        // TODO: filter mc.player.getUuid() out of the list
        Main.LOGGER.info("[LobbyFinancialUtils] Found {} players in lobby", uuids.size());
        return uuids;
    }

    /**
     * Gets all player UUIDs and names currently visible in the player list
     *
     * @return Map of UUID to player name
     */
    public static Map<UUID, String> getLobbyPlayersWithNames() {
        Map<UUID, String> players = new HashMap<>();

        if (mc.inGameHud == null) return players;

        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        if (playerListHud == null) return players;

        for (PlayerListEntry entry : playerListHud.collectPlayerEntries()) {
            if (entry != null && entry.getProfile() != null) {
                players.put(entry.getProfile().id(), entry.getProfile().name());
            }
        }

        return players;
    }

     /**
     * Gets individual futures for each lobby player's financial data.
     * Results arrive progressively as each request completes, allowing real-time updates.
     *
     * @return List of CompletableFutures, each containing a player's financial data
     */
    public static List<CompletableFuture<SkyblockProfile>> streamLobbyFinancials() {
        Main.LOGGER.info("[LobbyFinancialUtils] Starting to stream lobby financials");
        List<UUID> lobbyUuids = getLobbyPlayerUuids();
        Map<UUID, String> playerNames = getLobbyPlayersWithNames();

        Main.LOGGER.info("[LobbyFinancialUtils] Creating futures for {} players", lobbyUuids.size());
        return lobbyUuids.stream()
                .map(uuid -> HypixelApiClient.fetchPlayerFinancials(uuid)
                        .thenApply(data -> {
                            if (data == null) {
                                Main.LOGGER.warn("[LobbyFinancialUtils] Failed to fetch data for UUID: {}", uuid);
                                return null;
                            }
                            String name = playerNames.getOrDefault(uuid, data.playerName);
                            Main.LOGGER.info("[LobbyFinancialUtils] Received profile for: {}", name);
                            return data;
                        })
                        .exceptionally(ex -> {
                            Main.LOGGER.warn("[LobbyFinancialUtils] Exception fetching data for UUID {}: {}", uuid, ex.getMessage());
                            return null;
                        })
                )
                .collect(Collectors.toList());
    }

     /**
     * Fetches lobby financials with a callback for each result as it arrives.
     * Allows you to process results progressively without waiting for all to complete.
     *
     * @param onResult   Consumer called with each completed PlayerFinancials
     * @param onComplete Runnable called when all requests are finished
     * @return CompletableFuture that completes when all processing is done
     */
    public static CompletableFuture<Void> streamLobbyFinancialsWithCallback(
            Consumer<SkyblockProfile> onResult,
            Runnable onComplete) {

        Main.LOGGER.info("[LobbyFinancialUtils] Setting up streaming with callbacks");
        List<CompletableFuture<SkyblockProfile>> futures = streamLobbyFinancials();
        Main.LOGGER.info("[LobbyFinancialUtils] Created {} futures", futures.size());

        // Attach callbacks to each future as it completes
        futures.forEach(future ->
                future.thenAccept(data -> {
                    if (data != null) {
                        Main.LOGGER.info("[LobbyFinancialUtils] Processing result for: {}", data.playerName);
                        onResult.accept(data);
                    }
                })
        );

        // Return a future that completes when all are done
        @SuppressWarnings("unchecked")
        CompletableFuture<Void> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    Main.LOGGER.info("[LobbyFinancialUtils] All financials streaming completed");
                    onComplete.run();
                });
        return result;
    }

     /**
     * Similar to streamLobbyFinancialsWithCallback but without requiring a completion callback.
     *
     * @param onResult Consumer called with each completed PlayerFinancials
     * @return CompletableFuture that completes when all requests are finished
     */
    public static CompletableFuture<Void> streamLobbyFinancials(
            Consumer<SkyblockProfile> onResult) {
        Main.LOGGER.info("[LobbyFinancialUtils] Starting streaming lobby financials");
        return streamLobbyFinancialsWithCallback(onResult, () ->
            Main.LOGGER.info("[LobbyFinancialUtils] Streaming completed"));
    }

     /**
     * Fetches financial data for all players in the lobby
     *
     * @return CompletableFuture containing map of UUID to SkyblockProfile
     */
    public static CompletableFuture<Map<UUID, SkyblockProfile>> fetchAllLobbyFinancials() {
        List<UUID> lobbyUuids = getLobbyPlayerUuids();

        // Fetch all financials
        return HypixelApiClient.fetchLobbyFinancials(lobbyUuids);
    }
}




