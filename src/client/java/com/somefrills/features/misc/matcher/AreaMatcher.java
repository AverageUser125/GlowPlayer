package com.somefrills.features.misc.matcher;

import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Matches entities based on the current area they're in (from the scoreboard tab list).
 * Uses the Area enum for type safety.
 * Example: AREA=Private Island
 * Priority: 20
 */
public class AreaMatcher implements Matcher {
    private final Area area;

    public AreaMatcher(Area area) {
        this.area = area;
    }

    public AreaMatcher(String areaString) throws IllegalArgumentException {
        this.area = Area.fromString(areaString)
                .orElseThrow(() -> new IllegalArgumentException("Unknown area: " + areaString));
    }

    @Override
    public String toString() {
        return "AREA=" + area.getDisplayName();
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> Utils.isInArea(this.area);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    public Area getArea() {
        return area;
    }

}



