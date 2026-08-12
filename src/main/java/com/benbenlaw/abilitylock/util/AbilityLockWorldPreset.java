package com.benbenlaw.abilitylock.util;

import com.benbenlaw.abilitylock.ability.Abilities;
import net.minecraft.network.chat.Component;

import java.util.Set;

public enum AbilityLockWorldPreset {

    /** Falls back to the existing random-eligible-ability grant. */
    STANDARD("Standard", null, false),

    /** Start with core movement abilities already unlocked - an easier start. */
    EASIER("Easier", Set.of(Abilities.WALK.id(), Abilities.SPRINT.id(), Abilities.JUMP.id()), false),

    /** One life - death ends the run (vanilla hardcore: spectator on death, no respawn). Nothing unlocked at the start either. */
    HARDCORE("Hardcore", Set.of(), true);

    private final String label;
    private final Set<String> startingAbilities;
    private final boolean oneLife;

    AbilityLockWorldPreset(String label, Set<String> startingAbilities, boolean oneLife) {
        this.label = label;
        this.startingAbilities = startingAbilities;
        this.oneLife = oneLife;
    }

    public Component displayName() {
        return Component.literal(label);
    }

    /**
     * Null means "use the existing default behavior" (random eligible
     * ability grant). Non-null (including empty) means grant exactly
     * these abilities and skip the random pick.
     */
    public Set<String> startingAbilities() {
        return startingAbilities;
    }

    /** True if the world should be created with vanilla hardcore - one life, no respawn. */
    public boolean isOneLife() {
        return oneLife;
    }
}