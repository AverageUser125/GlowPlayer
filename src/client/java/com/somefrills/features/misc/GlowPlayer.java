package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.events.ClientDisconnectEvent;
import com.somefrills.events.EndTickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.somefrills.Main.mc;

public class GlowPlayer {
    public static final Feature instance = new Feature("glowPlayer", true);
    // Store by pure player name (no scoreboard prefixes/titles)
    private static final ConcurrentHashMap<String, Formatting> forcedGlows = new ConcurrentHashMap<>();
    private static final Map<Formatting, Team> TEAMS = new EnumMap<>(Formatting.class);
    private static final ConcurrentHashMap<String, Team> ORIGINAL_TEAMS = new ConcurrentHashMap<>();

    // Username token regex: alphanumeric + underscore, length 1-16 (typical Minecraft username)
    private static final Pattern USERNAME_TOKEN = Pattern.compile("[A-Za-z0-9_]{1,16}");

    /**
     * Normalize an arbitrary string to a likely pure username. Strips formatting codes and attempts
     * to extract a token that matches typical username characters (alphanumeric + underscore).
     * If none is found, returns the stripped full string.
     */
    public static String convertToPureName(String raw) {
        if (raw == null) return null;
        String stripped = Formatting.strip(raw).trim();
        if (stripped.isEmpty()) return null;

        // If the whole stripped string looks like a username, return it
        Matcher whole = USERNAME_TOKEN.matcher(stripped);
        if (whole.matches()) return stripped;

        // Otherwise, try to find a token within the string that looks like a username
        for (String token : stripped.split("\\s+")) {
            Matcher m = USERNAME_TOKEN.matcher(token);
            if (m.matches()) return token;
        }

        // Fallback: return the stripped string (best effort)
        return stripped;
    }

    @EventHandler
    public static void onWorldTick(EndTickEvent event) {
        if (!instance.isActive()) return;
        // Ensure teams exist before assigning
        createTeams();
        // Iterate our current forced list and assign teams
        for (Map.Entry<String, Formatting> e : forcedGlows.entrySet()) {
            String pureName = e.getKey();
            Formatting color = e.getValue();
            try {
                assignToTeam(pureName, color);
            } catch (Throwable ignored) {
                // per user request: even if it fails it's fine, swallow errors
            }
        }
    }

    @EventHandler
    public static void onDisconnect(ClientDisconnectEvent event) {
        if (!instance.isActive()) return;
        clear();
    }

    // API: operate by pure player name (String) only; a color is required to add
    public static boolean addPlayer(String pureName, Formatting color) {
        if (pureName == null || color == null) return false;
        return forcedGlows.put(pureName, color) == null;
    }

    public static boolean removePlayer(String pureName) {
        if (pureName == null) return false;
        // also restore team if we had an original
        removeFromTeam(pureName);
        return forcedGlows.remove(pureName) != null;
    }

    public static boolean hasPlayer(String pureName) {
        if (pureName == null) return false;
        return forcedGlows.containsKey(pureName);
    }

    public static Formatting getColor(String pureName) {
        if (pureName == null) return null;
        return forcedGlows.get(pureName);
    }

    public static void clear() {
        // restore teams for all tracked originals
        for (String name : ORIGINAL_TEAMS.keySet()) {
            removeFromTeam(name);
        }
        forcedGlows.clear();
        ORIGINAL_TEAMS.clear();
    }

    public static java.util.Set<String> getForcedNames() {
        return java.util.Set.copyOf(forcedGlows.keySet());
    }

    private static void createTeams() {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        for (Formatting color : Formatting.values()) {
            if (!color.isColor()) continue;

            String name = "examplemod_glow_" + color.getName();
            Team team = scoreboard.getTeam(name);

            if (team == null) {
                team = scoreboard.addTeam(name);
                team.setColor(color);
                team.setShowFriendlyInvisibles(false);
                team.setFriendlyFireAllowed(true);
            }

            TEAMS.put(color, team);
        }
    }

    private static void assignToTeam(String purePlayerName, Formatting color) {
        if (mc.world == null) return;

        Team team = TEAMS.get(color);
        if (team == null) return;

        // Save original team if not already saved
        Scoreboard scoreboard = mc.world.getScoreboard();
        if (!ORIGINAL_TEAMS.containsKey(purePlayerName)) {
            Team originalTeam = scoreboard.getScoreHolderTeam(purePlayerName);
            if (originalTeam != null && !TEAMS.containsValue(originalTeam)) {
                ORIGINAL_TEAMS.put(purePlayerName, originalTeam);
            }
        }

        scoreboard.addScoreHolderToTeam(purePlayerName, team);
    }

    private static void removeFromTeam(String playerName) {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        // Remove from glow team if in one
        for (Team team : TEAMS.values()) {
            if (scoreboard.getScoreHolderTeam(playerName) == team) {
                scoreboard.removeScoreHolderFromTeam(playerName, team);
                break;
            }
        }

        // Restore original team if one was saved
        Team originalTeam = ORIGINAL_TEAMS.remove(playerName);
        if (originalTeam != null) {
            scoreboard.addScoreHolderToTeam(playerName, originalTeam);
        }
    }

    private static void clearTeams() {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        // Remove all players from glow teams and restore their original teams
        for (String playerName : ORIGINAL_TEAMS.keySet()) {
            removeFromTeam(playerName);
        }

        for (Team team : TEAMS.values()) {
            scoreboard.removeTeam(team);
        }

        TEAMS.clear();
        ORIGINAL_TEAMS.clear();
    }

}
