package com.benbenlaw.abilitylock.task;

public record Task(String id, String displayName, String parent, TaskCriterion criterion) {
    public boolean hasParent() {
        return parent != null;
    }
}