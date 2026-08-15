package com.benbenlaw.abilitylock.task.tasks;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class EntityKillTask extends TaskType {

    @Override
    public int amountFor(TaskEvent event) {
        if (!(event instanceof TaskEvent.EntityKill(var player, var killed))) return 0;
        return matchesTarget(killed.getType(), getTargets()) ? 1 : 0;
    }

    private boolean matchesTarget(EntityType<?> entityType, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(target.substring(1)));
                if (entityType.builtInRegistryHolder().is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) return true;
            }
        }
        return false;
    }
}