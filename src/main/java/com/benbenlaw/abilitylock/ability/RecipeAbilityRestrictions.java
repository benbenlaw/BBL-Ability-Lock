package com.benbenlaw.abilitylock.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecipeAbilityRestrictions {
 
    private static final Map<String, Set<String>> RESTRICTIONS = new HashMap<>();
 
    public static void register(String recipeId, String... abilityIds) {
        RESTRICTIONS.put(recipeId, Set.of(abilityIds));
    }
 
    public static Set<String> requiredAbilities(String recipeId) {
        return RESTRICTIONS.getOrDefault(recipeId, Set.of());
    }
}