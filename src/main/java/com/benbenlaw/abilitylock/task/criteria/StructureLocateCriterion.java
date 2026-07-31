package com.benbenlaw.abilitylock.task.criteria;

import com.benbenlaw.abilitylock.task.TaskCriterion;
import com.benbenlaw.abilitylock.task.TaskCriterionType;
import com.benbenlaw.abilitylock.task.TaskEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public record StructureLocateCriterion(List<ResourceKey<Structure>> structures, int target) implements TaskCriterion {

    public StructureLocateCriterion(ResourceKey<Structure> structure, int target) {
        this(List.of(structure), target);
    }

    @Override
    public TaskCriterionType type() {
        return TaskCriterionType.STRUCTURE_LOCATE;
    }

    @Override
    public int amountFor(TaskEvent event) {
        if (event instanceof TaskEvent.StructureLocate(var player, var found) && structures.contains(found)) {
            return 1;
        }
        return 0;
    }
}