package com.benbenlaw.abilitylock.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskPoolRegistry {

    private static final Map<String, TaskPool> POOLS = new LinkedHashMap<>();

    public static TaskPool register(String id, String... taskIds) {
        TaskPool pool = new TaskPool(id, List.of(taskIds));
        POOLS.put(id, pool);
        return pool;
    }

    public static Optional<TaskPool> get(String id) {
        return Optional.ofNullable(POOLS.get(id));
    }

    public static Map<String, TaskPool> all() {
        return POOLS;
    }

    public static Optional<TaskPool> poolContaining(String taskId) {
        return POOLS.values().stream().filter(p -> p.taskIds().contains(taskId)).findFirst();
    }
}