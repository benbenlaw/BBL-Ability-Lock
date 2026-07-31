package com.benbenlaw.abilitylock.task;

import java.util.*;

public class TaskRegistry {
    private static final Map<String, Task> TASKS = new LinkedHashMap<>();

    public static Task register(String id, String displayName, String parent, TaskCriterion criterion) {
        Task task = new Task(id, displayName, parent, criterion);
        TASKS.put(id, task);
        return task;
    }

    public static Map<String, Task> all() {
        return Collections.unmodifiableMap(TASKS);
    }

    public static Optional<Task> get(String id) {
        return Optional.ofNullable(TASKS.get(id));
    }

    public static void validate() {
        for (Task task : TASKS.values()) {
            if (task.hasParent() && !TASKS.containsKey(task.parent())) {
                throw new IllegalStateException("Task '" + task.id()
                        + "' declares parent '" + task.parent() + "' which does not exist.");
            }
        }
    }
}