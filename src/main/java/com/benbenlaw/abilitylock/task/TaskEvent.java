package com.benbenlaw.abilitylock.task;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

public sealed interface TaskEvent permits
        TaskEvent.BlockBreak, TaskEvent.ItemObtain, TaskEvent.EntityKill, TaskEvent.StructureLocate, TaskEvent.StatAward {

    ServerPlayer player();

    record BlockBreak(ServerPlayer player, BlockState state) implements TaskEvent {}
    record ItemObtain(ServerPlayer player, ItemStack stack, int amount) implements TaskEvent {}
    record EntityKill(ServerPlayer player, Entity killed) implements TaskEvent {}
    record StructureLocate(ServerPlayer player, ResourceKey<Structure> structure) implements TaskEvent {}
    record StatAward(ServerPlayer player, Stat<?> stat, int value) implements TaskEvent {}
}