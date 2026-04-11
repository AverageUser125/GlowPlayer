package com.somefrills.misc;

/**
 * Mob types with associated symbols found in team prefixes.
 * Each type represents a category of mobs that can be highlighted.
 */
public enum MobType {
    AIRBORNE("✈", "Mobs that can fly"),
    ANIMAL("☮", "Mobs with animalistic characteristics"),
    AQUATIC("⚓", "Water creatures or mobs that reside in water"),
    ARCANE("♃", "Mobs that specialize in magic"),
    ARTHROPOD("Ж", "Spiders and other invertebrate-adjacent mobs"),
    CONSTRUCT("⚙", "Summoned or artificially created mobs"),
    CUBIC("⚂", "Mobs with cube-like or blocky appearances"),
    ELUSIVE("♣", "Rare and hard to find mobs"),
    ENDER("⊙", "Mobs related to the End dimension"),
    FROZEN("☃", "Mobs in Jerry's Workshop"),
    GLACIAL("❄", "Mobs in the Glacite Mineshaft"),
    HUMANOID("✰", "Enemies found in the Crystal Hollows"),
    INFERNAL("♨", "Dangerous mobs native to the Crimson Isle"),
    MAGMATIC("♆", "Mobs that spawn in lava or fiery environments"),
    MYTHOLOGICAL("✿", "Mobs appearing during Mythological Ritual Event"),
    PEST("ൠ", "Nuisance mobs, often found in the Garden"),
    SHIELDED("⛨", "Mobs that take only one point of damage per hit"),
    SKELETAL("🦴", "Skeleton-based mobs or those with skeletal traits"),
    SPOOKY("☽", "Mobs appearing during Spooky Festival"),
    SUBTERRANEAN("⛏", "Mobs found in Dwarven Mines"),
    UNDEAD("༕", "Mobs that have risen from their graves"),
    WITHER("☠", "Mobs related to the Wither or Catacombs"),
    WOODLAND("⸙", "Mobs that reside in Galatea");

    public final String symbol;
    public final String description;

    MobType(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    /**
     * Get a MobType by its symbol
     */
    public static MobType fromSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return null;
        }
        for (MobType type : values()) {
            if (type.symbol.equals(symbol)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get a MobType by its name (case-insensitive)
     */
    public static MobType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
