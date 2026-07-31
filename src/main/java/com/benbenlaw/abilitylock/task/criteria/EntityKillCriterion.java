package com.benbenlaw.abilitylock.task.criteria;

import com.benbenlaw.abilitylock.task.TaskCriterion;
import com.benbenlaw.abilitylock.task.TaskCriterionType;
import com.benbenlaw.abilitylock.task.TaskEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityKillCriterion(TagKey<EntityType<?>> tag, int target) implements TaskCriterion {
    @Override
    public TaskCriterionType type() {
        return TaskCriterionType.ENTITY_KILL;
    }

    @Override
    public int amountFor(TaskEvent event) {
        if (event instanceof TaskEvent.EntityKill(var player, var killed) && killed.is(tag)) {
            return 1;
        }
        return 0;
    }
}