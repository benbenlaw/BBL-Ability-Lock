package com.benbenlaw.abilitylock.ability;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    public static Set<String> withAncestors(String abilityId) {
        Set<String> result = new LinkedHashSet<>();
        String current = abilityId;
        while (current != null && result.add(current)) {
            Ability ability = ABILITIES.get(current);
            current = ability != null ? ability.parent() : null;
        }
        return result;
    }
}