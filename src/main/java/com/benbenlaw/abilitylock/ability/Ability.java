package com.benbenlaw.abilitylock.ability;

import javax.annotation.Nullable;

public record Ability(String id, String displayName, @Nullable String parent) {

    public boolean hasParent() {
        return parent != null;
    }
}