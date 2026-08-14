package com.benbenlaw.abilitylock.task;

import net.minecraft.resources.Identifier;

import java.util.Set;

public record Task(String id, String displayName, TaskCriterion criterion, Set<Identifier> requiredAbilities) {

    public boolean hasRequiredAbilities() {
        return !requiredAbilities.isEmpty();
    }
}