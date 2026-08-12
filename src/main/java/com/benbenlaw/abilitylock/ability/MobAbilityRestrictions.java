package com.benbenlaw.abilitylock.ability;

import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MobAbilityRestrictions {
 
    private static final Map<EntityType<?>, Set<String>> RESTRICTIONS = new HashMap<>();
 
    public static void register(EntityType<?> entityType, String... abilityIds) {
        RESTRICTIONS.put(entityType, Set.of(abilityIds));
    }
 
    public static Set<String> requiredAbilities(EntityType<?> entityType) {
        return RESTRICTIONS.getOrDefault(entityType, Set.of());
    }
}