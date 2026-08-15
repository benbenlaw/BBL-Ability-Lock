package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskLoader;
import com.benbenlaw.abilitylock.task.TaskManager;
import com.benbenlaw.abilitylock.task.TaskType;
import com.benbenlaw.abilitylock.task.tasks.StructureLocateTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class TaskEvents {

    private static final int STRUCTURE_CHECK_INTERVAL_TICKS = 100;
    private static final int INVENTORY_CHECK_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;

        TaskManager.handle(new TaskEvent.BlockBreak(serverPlayer, event.getState()));
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)) return;

        TaskManager.handle(new TaskEvent.EntityKill(serverPlayer, event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        if (serverPlayer.tickCount % INVENTORY_CHECK_INTERVAL_TICKS == 0) {
            TaskManager.checkInventoryTasks(serverPlayer);
        }

        if (serverPlayer.level().getGameTime() % STRUCTURE_CHECK_INTERVAL_TICKS == 0) {
            checkStructureLocateTasks(serverPlayer);
        }
    }

    private static void checkStructureLocateTasks(ServerPlayer serverPlayer) {
        ServerLevel serverLevel = serverPlayer.level();
        BlockPos pos = serverPlayer.blockPosition();

        for (TaskType task : TaskLoader.TASKS.values()) {
            if (!(task instanceof StructureLocateTask)) continue;

            for (String target : task.getTargets()) {
                ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, Identifier.parse(target));
                Structure structure = serverLevel.registryAccess()
                        .lookupOrThrow(Registries.STRUCTURE)
                        .getValue(structureKey);
                if (structure == null) continue;

                StructureStart start = serverLevel.structureManager().getStructureAt(pos, structure);
                if (start != StructureStart.INVALID_START) {
                    TaskManager.handle(new TaskEvent.StructureLocate(serverPlayer, structureKey));
                    break;
                }
            }
        }
    }
}