package com.benbenlaw.abilitylock.task;

import net.minecraft.server.level.ServerPlayer;

public interface TaskCriterion {
    TaskCriterionType type();
    int target();
    int amountFor(TaskEvent event);
    default int countInInventory(ServerPlayer player) {
        return -1;
    }
}