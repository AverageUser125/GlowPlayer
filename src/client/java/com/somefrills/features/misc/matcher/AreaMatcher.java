package com.somefrills.features.misc.matcher;

import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Matches entities based on the current area they're in (from the scoreboard tab list).
 * Example: AREA=Private Island
 * Priority: 20
 */
public class AreaMatcher implements Matcher {
    private final String area;

    public AreaMatcher(String area) {
        this.area = area.trim();
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> Utils.isInArea(this.area);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public String toString() {
        return "AREA=" + area;
    }
}



