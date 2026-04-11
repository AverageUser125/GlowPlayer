package com.somefrills.misc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents aggregated financial data for a player with async futures for each component.
 * Each component future completes as data is parsed from the stream.
 */
public class SkyblockProfile {
    public final UUID uuid;
    public final String playerName;

    private final CompletableFuture<Long> purseFuture;
    private final CompletableFuture<Long> totalBankFuture;

    /**
     * Creates a profile with component futures.
     * Futures should be completed by the API client as they are parsed.
     *
     * @param uuid The player's UUID
     * @param playerName The player's name
     * @param purseFuture Future that will complete with the purse amount
     * @param totalBankFuture Future that will complete with the total bank amount
     */
    public SkyblockProfile(UUID uuid, String playerName,
                          CompletableFuture<Long> purseFuture,
                          CompletableFuture<Long> totalBankFuture) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.purseFuture = purseFuture;
        this.totalBankFuture = totalBankFuture;
    }

    /**
     * Gets the player's coin purse amount as a future.
     * Completes as it is parsed from the API response.
     *
     * @return CompletableFuture that completes with purse amount
     */
    public CompletableFuture<Long> getPurse() {
        return purseFuture;
    }

    /**
     * Gets the player's total bank amount as a future.
     * Completes as it is parsed from the API response.
     *
     * @return CompletableFuture that completes with total bank amount
     */
    public CompletableFuture<Long> getTotalBank() {
        return totalBankFuture;
    }

    /**
     * Gets the player's total wealth (purse + bank) as a future.
     * Completes when both purse and bank futures resolve.
     *
     * @return CompletableFuture that completes with total wealth
     */
    public CompletableFuture<Long> getTotalWealth() {
        return CompletableFuture.allOf(purseFuture, totalBankFuture)
                .thenApply(v -> {
                    Long purse = purseFuture.getNow(0L);
                    Long bank = totalBankFuture.getNow(0L);
                    return purse + bank;
                });
    }

    @Override
    public String toString() {
        Long purse = purseFuture.getNow(null);
        Long bank = totalBankFuture.getNow(null);

        if (purse == null || bank == null) {
            return String.format("%s: Purse=%s, Bank=%s, Total=%s",
                    playerName,
                    purse == null ? "loading" : purse,
                    bank == null ? "loading" : bank,
                    (purse == null || bank == null) ? "loading" : (purse + bank));
        }

        return String.format("%s: Purse=%,d, Bank=%,d, Total=%,d",
                playerName, purse, bank, purse + bank);
    }
}

