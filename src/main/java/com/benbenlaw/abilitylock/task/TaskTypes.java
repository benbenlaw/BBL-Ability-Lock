package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.task.tasks.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TaskTypes {

    public static final Identifier BLOCK_BREAK = AbilityLock.identifier("block_break_task");
    public static final Identifier ITEM_OBTAIN = AbilityLock.identifier("item_obtain_task");
    public static final Identifier ENTITY_KILL = AbilityLock.identifier("entity_kill_task");
    public static final Identifier STRUCTURE_LOCATE = AbilityLock.identifier("structure_locate_task");
    public static final Identifier STAT_TASK = AbilityLock.identifier("stat_task");

    private static final Map<Identifier, Supplier<TaskType>> FACTORIES = new HashMap<>();

    public static void register(Identifier typeId, Supplier<TaskType> factory) {
        FACTORIES.put(typeId, factory);
    }

    public static Optional<TaskType> create(Identifier typeId) {
        Supplier<TaskType> factory = FACTORIES.get(typeId);
        return factory == null ? Optional.empty() : Optional.of(factory.get());
    }

    public static void init() {
        register(BLOCK_BREAK, BlockBreakTask::new);
        register(ITEM_OBTAIN, ItemObtainTask::new);
        register(ENTITY_KILL, EntityKillTask::new);
        register(STRUCTURE_LOCATE, StructureLocateTask::new);
        register(STAT_TASK, StatTask::new);
    }
}