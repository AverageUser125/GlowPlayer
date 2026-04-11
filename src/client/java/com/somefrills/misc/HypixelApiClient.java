package com.somefrills.misc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.somefrills.Main;
import net.minecraft.util.Util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Handles API calls to fetch SkyBlock player profile data from Hypixel API
 */
public class HypixelApiClient {
    private static final String API_BASE_URL = "https://api.hypixel.net/v2/skyblock";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static volatile boolean fatalError = false;
    private static volatile String fatalErrorMessage = "";

    /**
     * Fetches financial data for all players in the lobby
     *
     * @param playerUuids List of player UUIDs to fetch data for
     * @return CompletableFuture containing a map of UUID to SkyblockProfile
     */
    public static CompletableFuture<Map<UUID, SkyblockProfile>> fetchLobbyFinancials(List<UUID> playerUuids) {
        List<CompletableFuture<SkyblockProfile>> futures = playerUuids.stream()
                .map(HypixelApiClient::fetchPlayerFinancials)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(a -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(f -> f.uuid, f -> f))
                );
    }

    /**
     * Fetches financial data for a single player (streaming from the JSON, no full profile parsed)
     * Individual components are async futures that complete as the stream is parsed.
     *
     * @param uuid Player UUID
     * @return CompletableFuture containing the player's financial data with async component futures
     */
    public static CompletableFuture<SkyblockProfile> fetchPlayerFinancials(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if fatal error already occurred
            if (fatalError) {
                Main.LOGGER.info("[HypixelApiClient] Skipping request for UUID {} - Fatal error: {}", uuid, fatalErrorMessage);
                return null;
            }

            String apiKey = KeyManager.getKey("hypixel");
            if (apiKey == null || apiKey.isEmpty()) {
                Main.LOGGER.warn("[HypixelApiClient] Hypixel API key not set for UUID: {}", uuid);
                return null;
            }

            try {
                String uuidString = uuid.toString().replace("-", "");
                String url = String.format("%s/profiles?uuid=%s", API_BASE_URL, uuidString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("API-KEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    // Check for fatal errors
                    if (response.statusCode() == 404) {
                        fatalError = true;
                        fatalErrorMessage = "API endpoint deprecated (404)";
                        Main.LOGGER.error("[HypixelApiClient] FATAL ERROR: {}", fatalErrorMessage);
                        Main.LOGGER.error("[HypixelApiClient] Stopping all requests. Please update the mod or use a different API.");
                        Utils.infoFormat("§c[SomeFrills] FATAL ERROR: API endpoint deprecated. The Hypixel API endpoint is no longer available.");
                        return null;
                    }

                    Main.LOGGER.warn("[HypixelApiClient] HTTP {} response for UUID: {}", response.statusCode(), uuid);
                    return null;
                }

                // Create futures for each component
                CompletableFuture<Long> purseFuture = new CompletableFuture<>();
                CompletableFuture<Long> totalBankFuture = new CompletableFuture<>();

                // Create profile with async futures
                SkyblockProfile profile = new SkyblockProfile(uuid, uuid.toString(), purseFuture, totalBankFuture);

                // Stream parse JSON to extract ONLY the financials we need, nothing else
                try (InputStream is = response.body();
                     JsonReader reader = new JsonReader(new InputStreamReader(is))) {

                    long purse = 0;
                    long totalBank = 0;
                    boolean foundPurse = false;
                    boolean foundBank = false;
                    boolean foundBalance = false;

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if ("success".equals(name)) {
                            boolean success = reader.nextBoolean();
                            if (!success) {
                                Main.LOGGER.warn("[HypixelApiClient] API returned success: false for UUID: {}", uuid);
                                reader.endObject();
                                return null;
                            }
                        } else if ("profiles".equals(name)) {
                            reader.beginArray();
                            if (reader.hasNext()) {
                                // Parse first profile to get financials
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String fieldName = reader.nextName();

                                    if ("members".equals(fieldName)) {
                                        // Parse members object
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            String memberUuid = reader.nextName();
                                            // Found our member, extract financials
                                            if (memberUuid.equals(uuidString)) {
                                                reader.beginObject();
                                                while (reader.hasNext()) {
                                                    String memberField = reader.nextName();
                                                    if ("currencies".equals(memberField)) {
                                                        reader.beginObject();
                                                        while (reader.hasNext()) {
                                                            String currencyField = reader.nextName();
                                                            if ("coin_purse".equals(currencyField)) {
                                                                // Parse numeric value (can be long or double)
                                                                purse = (long) reader.nextDouble();
                                                                foundPurse = true;
                                                            } else {
                                                                reader.skipValue();
                                                            }
                                                        }
                                                        reader.endObject();
                                                    } else if ("profile".equals(memberField)) {
                                                        // Get solo bank
                                                        reader.beginObject();
                                                        while (reader.hasNext()) {
                                                            String profileField = reader.nextName();
                                                            if ("bank_account".equals(profileField)) {
                                                                // Parse numeric value (can be long or double)
                                                                totalBank += (long) reader.nextDouble();
                                                                foundBank = true;
                                                            } else {
                                                                reader.skipValue();
                                                            }
                                                        }
                                                        reader.endObject();
                                                    } else {
                                                        reader.skipValue();
                                                    }
                                                }
                                                reader.endObject();
                                            } else {
                                                reader.skipValue();
                                            }
                                        }
                                        reader.endObject();
                                    } else if ("banking".equals(fieldName)) {
                                        // Get shared bank
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            String bankField = reader.nextName();
                                            if ("balance".equals(bankField)) {
                                                // Parse numeric value (can be long or double)
                                                totalBank += (long) reader.nextDouble();
                                                foundBalance = true;
                                            } else {
                                                reader.skipValue();
                                            }
                                        }
                                        reader.endObject();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();

                                // Check if we have all values we need and can exit early
                                if (foundPurse && foundBank && foundBalance) {
                                    break;
                                }
                            } else {
                                Main.LOGGER.info("[HypixelApiClient] No profiles found for UUID: {} (private or no profile)", uuid);
                            }
                            break;
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    // Complete the futures with parsed values
                    purseFuture.complete(purse);
                    totalBankFuture.complete(totalBank);

                    return profile;
                }
            } catch (Exception e) {
                Main.LOGGER.warn("[HypixelApiClient] Exception fetching financials for UUID {}: {}", uuid, e.getMessage());
                return null;
            }
        }, Util.getIoWorkerExecutor());
    }

    /**
     * Fetches a player's SkyBlock profile data asynchronously
     *
     * @param uuid      The player's UUID (without dashes)
     * @param profileId The specific profile ID to fetch (optional, can be null for active profile)
     * @return CompletableFuture containing the profile JSON, or empty if API call fails
     */
    public static CompletableFuture<JsonObject> fetchPlayerProfile(UUID uuid, String profileId) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if fatal error already occurred
            if (fatalError) {
                Main.LOGGER.info("[HypixelApiClient] Skipping request for UUID {} - Fatal error: {}", uuid, fatalErrorMessage);
                return new JsonObject();
            }

            String apiKey = KeyManager.getKey("hypixel");
            if (apiKey == null || apiKey.isEmpty()) {
                Main.LOGGER.warn("[HypixelApiClient] Hypixel API key not set for UUID: {}", uuid);
                return new JsonObject();
            }

            try {
                String uuidString = uuid.toString().replace("-", "");
                String url = String.format("%s/profiles?uuid=%s", API_BASE_URL, uuidString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("API-KEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    // Check for fatal errors
                    if (response.statusCode() == 404) {
                        fatalError = true;
                        fatalErrorMessage = "API endpoint deprecated (404)";
                        Main.LOGGER.error("[HypixelApiClient] FATAL ERROR: {}", fatalErrorMessage);
                        Main.LOGGER.error("[HypixelApiClient] Stopping all requests. Please update the mod or use a different API.");
                        Utils.infoFormat("§c[SomeFrills] FATAL ERROR: API endpoint deprecated. The Hypixel API endpoint is no longer available.");
                        return new JsonObject();
                    }

                    Main.LOGGER.warn("[HypixelApiClient] HTTP {} response for UUID: {}", response.statusCode(), uuid);
                    return new JsonObject();
                }

                // Stream parse JSON to reduce memory usage
                JsonObject profile = new JsonObject();
                try (InputStream is = response.body();
                     JsonReader reader = new JsonReader(new InputStreamReader(is))) {

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if ("success".equals(name)) {
                            boolean success = reader.nextBoolean();
                            if (!success) {
                                Main.LOGGER.warn("[HypixelApiClient] API returned success: false for UUID: {}", uuid);
                                reader.endObject();
                                return new JsonObject();
                            }
                        } else if ("profiles".equals(name)) {
                            reader.beginArray();
                            if (reader.hasNext()) {
                                // Only read the first profile
                                profile = GSON.fromJson(reader, JsonObject.class);

                                // If looking for specific profileId, search through remaining
                                if (profileId != null && !profileId.isEmpty()) {
                                    if (!profile.has("profile_id") || !profile.get("profile_id").getAsString().equals(profileId)) {
                                        while (reader.hasNext()) {
                                            JsonObject p = GSON.fromJson(reader, JsonObject.class);
                                            if (p.has("profile_id") && p.get("profile_id").getAsString().equals(profileId)) {
                                                profile = p;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                Main.LOGGER.info("[HypixelApiClient] No profiles found for UUID: {} (private or no profile)", uuid);
                            }
                            reader.endArray();
                            break;  // We got what we need, stop reading
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                }

                return profile;
            } catch (Exception e) {
                Main.LOGGER.warn("[HypixelApiClient] Exception fetching profile for UUID {}: {}", uuid, e.getMessage());
                return new JsonObject();
            }
        }, Util.getIoWorkerExecutor());
    }

    /**
     * Check if a fatal error has occurred
     */
    public static boolean hasFatalError() {
        return fatalError;
    }

    /**
     * Get the fatal error message
     */
    public static String getFatalErrorMessage() {
        return fatalErrorMessage;
    }

    /**
     * Reset fatal error state (for testing or manual reset)
     */
    public static void resetFatalError() {
        fatalError = false;
        fatalErrorMessage = "";
        Main.LOGGER.info("[HypixelApiClient] Fatal error state reset");
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<JsonObject>[] fetchPlayerProfiles(UUID... uuids) {
        CompletableFuture<JsonObject>[] futures = new CompletableFuture[uuids.length];

        for (int i = 0; i < uuids.length; i++) {
            futures[i] = fetchPlayerProfile(uuids[i], null);
        }

        return futures;
    }

    public static CompletableFuture<JsonObject> fetchPlayerProfile(UUID first) {
        return fetchPlayerProfile(first, null);
    }
}
