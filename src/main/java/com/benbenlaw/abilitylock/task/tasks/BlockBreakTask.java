package com.benbenlaw.abilitylock.task.tasks;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockBreakTask extends TaskType {

    @Override
    public int amountFor(TaskEvent event) {
        if (!(event instanceof TaskEvent.BlockBreak(var player, var state))) return 0;
        return matchesTarget(state.getBlock(), getTargets()) ? 1 : 0;
    }

    private boolean matchesTarget(Block block, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(target.substring(1)));
                if (block.builtInRegistryHolder().is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.BLOCK.getKey(block))) return true;
            }
        }
        return false;
    }
}