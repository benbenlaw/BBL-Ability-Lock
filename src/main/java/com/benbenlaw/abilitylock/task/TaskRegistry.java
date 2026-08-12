package com.benbenlaw.abilitylock.task;

import java.util.*;

public class TaskRegistry {
    private static final Map<String, Task> TASKS = new LinkedHashMap<>();

    public static Task register(String id, String displayName, TaskCriterion criterion) {
        return register(id, displayName, criterion, Set.of());
    }

    public static Task register(String id, String displayName, TaskCriterion criterion, Set<String> requiredAbilities) {
        Task task = new Task(id, displayName, criterion, requiredAbilities);
        TASKS.put(id, task);
        return task;
    }

    public static Map<String, Task> all() {
        return Collections.unmodifiableMap(TASKS);
    }

    public static Optional<Task> get(String id) {
        return Optional.ofNullable(TASKS.get(id));
    }
}