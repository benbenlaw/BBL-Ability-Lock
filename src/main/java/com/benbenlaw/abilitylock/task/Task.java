package com.benbenlaw.abilitylock.task;

import java.util.Set;

public record Task(String id, String displayName, TaskCriterion criterion, Set<String> requiredAbilities) {

    public boolean hasRequiredAbilities() {
        return !requiredAbilities.isEmpty();
    }
}