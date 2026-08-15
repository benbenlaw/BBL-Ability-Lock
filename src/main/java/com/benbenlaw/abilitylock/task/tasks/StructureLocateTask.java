package com.benbenlaw.abilitylock.task.tasks;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public class StructureLocateTask extends TaskType {

    @Override
    public int amountFor(TaskEvent event) {
        if (!(event instanceof TaskEvent.StructureLocate(var player, var found))) return 0;
        return matchesTarget(found, getTargets()) ? 1 : 0;
    }

    private boolean matchesTarget(ResourceKey<Structure> found, List<String> targets) {
        for (String target : targets) {
            if (Identifier.parse(target).equals(found.identifier())) return true;
        }
        return false;
    }
}