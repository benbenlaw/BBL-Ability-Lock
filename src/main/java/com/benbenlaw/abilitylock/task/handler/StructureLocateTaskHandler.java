package com.benbenlaw.abilitylock.task.handler;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskManager;
import com.benbenlaw.abilitylock.task.TaskRegistry;
import com.benbenlaw.abilitylock.task.criteria.StructureLocateCriterion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber
public class StructureLocateTaskHandler {

    private static final int CHECK_INTERVAL_TICKS = 100; // every 5s

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().getGameTime() % CHECK_INTERVAL_TICKS != 0) return;
        ServerLevel serverLevel = serverPlayer.level();

        BlockPos pos = serverPlayer.blockPosition();

        for (var task : TaskRegistry.all().values()) {
            if (!(task.criterion() instanceof StructureLocateCriterion(List<ResourceKey<Structure>> structures, int target))) continue;

            for (ResourceKey<Structure> structureKey : structures) {
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