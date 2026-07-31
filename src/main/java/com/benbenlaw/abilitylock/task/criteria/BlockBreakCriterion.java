package com.benbenlaw.abilitylock.task.criteria;

import com.benbenlaw.abilitylock.task.TaskCriterion;
import com.benbenlaw.abilitylock.task.TaskCriterionType;
import com.benbenlaw.abilitylock.task.TaskEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public record BlockBreakCriterion(TagKey<Block> tag, int target) implements TaskCriterion {
    @Override
    public TaskCriterionType type() {
        return TaskCriterionType.BLOCK_BREAK;
    }

    @Override
    public int amountFor(TaskEvent event) {
        if (event instanceof TaskEvent.BlockBreak(var player, var state) && state.is(tag)) {
            return 1;
        }
        return 0;
    }
}