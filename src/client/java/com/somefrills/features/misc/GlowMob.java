package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.MobType;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity highlighting feature that allows users to highlight entities based on name, type, or mob type symbols.
 * Mob types are identified by symbols in team prefixes.
 */
public class GlowMob extends Feature {

    private static final ConcurrentHashMap<GlowMobRule, RenderColor> ruleColors = new ConcurrentHashMap<>();
    // MobType-to-color mapping for highlighting
    private static final ConcurrentHashMap<MobType, RenderColor> mobTypeColors = new ConcurrentHashMap<>();

    public GlowMob() {
        super(FrillsConfig.instance.misc.glowMob.enabled);
    }


    private static void applyHighlight(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        // 1. Direct rules (highest priority)
        for (Map.Entry<GlowMobRule, RenderColor> entry : ruleColors.entrySet()) {
            GlowMobRule rule = entry.getKey();
            RenderColor color = entry.getValue();
            if (rule.matches(entity)) {
                Utils.setGlowing(entity, true, color);
                return;
            }
        }

        // 2. Check team prefix for mob type symbols
        var team = entity.getScoreboardTeam();
        if (team != null) {
            String prefix = Utils.toPlain(team.getPrefix());
            for (Map.Entry<MobType, RenderColor> entry : mobTypeColors.entrySet()) {
                MobType mobType = entry.getKey();
                RenderColor color = entry.getValue();
                if (prefix.contains(mobType.symbol)) {
                    Utils.setGlowing(entity, true, color);
                    return;
                }
            }
        }
    }

    public static boolean addRule(String name, String type, RenderColor color) {
        GlowMobRule rule = new GlowMobRule(name, type);
        boolean isNew = ruleColors.put(rule, color) == null;

        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }

        return isNew;
    }

    public static boolean removeRule(String name, String type) {
        GlowMobRule rule = new GlowMobRule(name, type);
        RenderColor removed = ruleColors.remove(rule);
        if (removed == null) return false;

        for (Entity entity : Utils.getEntities()) {
            if (rule.matches(entity)) {
                Utils.setGlowing(entity, false, RenderColor.white);
            }
        }

        return true;
    }

    public static void clearRules() {
        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;

            for (GlowMobRule rule : ruleColors.keySet()) {
                if (rule.matches(entity)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                    break;
                }
            }
        }
        ruleColors.clear();
    }

    public static Collection<GlowMobRule> getRules() {
        return List.copyOf(ruleColors.keySet());
    }

    public static RenderColor getRuleColor(GlowMobRule rule) {
        return ruleColors.get(rule);
    }

    public static boolean addSymbol(String symbol, RenderColor color) {
        if (symbol == null || symbol.isEmpty()) {
            return false;
        }
        MobType mobType = MobType.fromSymbol(symbol);
        if (mobType == null) {
            return false;
        }
        return addMobType(mobType, color);
    }

    public static boolean addMobType(MobType mobType, RenderColor color) {
        if (mobType == null) {
            return false;
        }
        boolean isNew = mobTypeColors.put(mobType, color) == null;

        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }

        return isNew;
    }

    public static boolean removeSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return false;
        }
        MobType mobType = MobType.fromSymbol(symbol);
        if (mobType == null) {
            return false;
        }
        return removeMobType(mobType);
    }

    public static boolean removeMobType(MobType mobType) {
        if (mobType == null) {
            return false;
        }
        RenderColor removed = mobTypeColors.remove(mobType);
        if (removed == null) return false;

        for (Entity entity : Utils.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;

            var team = entity.getScoreboardTeam();
            if (team != null) {
                String prefix = Utils.toPlain(team.getPrefix());
                if (prefix.contains(mobType.symbol)) {
                    Utils.setGlowing(entity, false, RenderColor.white);
                }
            }
        }

        return true;
    }

    public static Collection<String> getSymbols() {
        return mobTypeColors.keySet().stream()
                .map(mt -> mt.symbol)
                .toList();
    }

    public static Collection<MobType> getActiveMobTypes() {
        return List.copyOf(mobTypeColors.keySet());
    }

    public static Map<MobType, RenderColor> getSymbolColors() {
        return new ConcurrentHashMap<>(mobTypeColors);
    }


    private static String normalizeRuleName(String name) {
        if (name == null) return null;

        String n = name.trim().toLowerCase();
        return n.isEmpty() ? null : n;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEntityUpdate(EntityUpdatedEvent event) {
        if (!isActive()) return;
        applyHighlight(event.entity);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onServerJoin(ServerJoinEvent ignored) {
        for (Entity entity : Utils.getEntities()) {
            applyHighlight(entity);
        }
    }

    public static class GlowMobRule {
        public String name;
        public String type;

        public GlowMobRule(String name, String type) {
            this.name = normalizeRuleName(name);

            if (type != null && !type.isEmpty()) {
                this.type = Utils.stripPrefix(type, "minecraft:").toLowerCase();
            } else {
                this.type = null;
            }
        }

        public boolean matches(Entity entity) {
            if (this.name != null) {
                var team = entity.getScoreboardTeam();
                String name = Utils.toPlain(entity.getName());
                if(team != null) {
                   name = Utils.toPlain(team.getPrefix()) + name;
                }
                if (!name.toLowerCase().contains(this.name)) {
                    return false;
                }
            }

            if (this.type != null) {
                String typeStr = entity.getType().toString();
                typeStr = Utils.stripPrefix(typeStr, "entity.minecraft.");
                typeStr = typeStr.toLowerCase();
                return typeStr.equals(this.type);
            }

            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GlowMobRule that = (GlowMobRule) o;
            return Objects.equals(name, that.name) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }
}