package com.benbenlaw.abilitylock.task;

import java.util.List;

public record TaskPool(String id, List<String> taskIds) {

    public boolean contains(String id) {
        return taskIds.contains(id);
    }

}