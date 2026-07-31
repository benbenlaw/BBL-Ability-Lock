package com.benbenlaw.abilitylock.ability;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AbilityRegistry {

    private static final Map<String, Ability> ABILITIES = new LinkedHashMap<>();

    public static Ability register(String id, String displayName, @javax.annotation.Nullable String parent) {
        Ability ability = new Ability(id, displayName, parent);
        ABILITIES.put(id, ability);
        return ability;
    }

    public static Optional<Ability> get(String id) {
        return Optional.ofNullable(ABILITIES.get(id));
    }

    public static Map<String, Ability> all() {
        return ABILITIES;
    }

    public static void validate() {
        for (Ability ability : ABILITIES.values()) {
            if (ability.hasParent() && !ABILITIES.containsKey(ability.parent())) {
                throw new IllegalStateException("Ability '" + ability.id()
                        + "' declares parent '" + ability.parent() + "' which does not exist.");
            }
        }
    }
}