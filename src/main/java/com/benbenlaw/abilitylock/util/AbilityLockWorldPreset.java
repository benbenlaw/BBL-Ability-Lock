package com.benbenlaw.abilitylock.util;

import com.benbenlaw.abilitylock.ability.Abilities;
import net.minecraft.network.chat.Component;

import java.util.Set;

public enum AbilityLockWorldPreset {

    STANDARD("Standard", null, false),
    EASIER("Easier", Set.of(Abilities.WALK.id(), Abilities.SPRINT.id(), Abilities.JUMP.id()), false),
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

    public Set<String> startingAbilities() {
        return startingAbilities;
    }

    public boolean isOneLife() {
        return oneLife;
    }
}