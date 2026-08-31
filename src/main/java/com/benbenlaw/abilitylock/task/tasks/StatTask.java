package com.benbenlaw.abilitylock.task.tasks;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

import java.util.List;

public class StatTask extends TaskType {

    @Override
    public int amountFor(TaskEvent event) {
        if (!(event instanceof TaskEvent.StatAward(var player, var stat, var value))) return 0;
        return matchesTarget(stat, getTargets()) ? value : 0;
    }

    private boolean matchesTarget(Stat<?> stat, List<String> targets) {
        StatType type = stat.getType();
        Identifier valueId = type.getRegistry().getKey(stat.getValue());
        if (valueId == null) return false;

        for (String target : targets) {
            Identifier id = Identifier.parse(target);
            if (id.equals(valueId)) return true;
        }
        return false;
    }
}