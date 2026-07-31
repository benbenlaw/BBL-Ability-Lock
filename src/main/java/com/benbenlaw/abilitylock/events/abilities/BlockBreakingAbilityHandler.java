package com.benbenlaw.abilitylock.events.abilities;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.Map;

@EventBusSubscriber
public class BlockBreakingAbilityHandler {

    private static final Map<TagKey<Block>, String> ABILITY_TAGS = Map.ofEntries(
            Map.entry(Tags.Blocks.OVERWORLD_NATURAL_LOGS, Abilities.LOG.id()),
            Map.entry(Tags.Blocks.STONES, Abilities.STONE.id()),
            Map.entry(Tags.Blocks.ORES_COAL, Abilities.COAL.id()),
            Map.entry(Tags.Blocks.ORES_COPPER, Abilities.COPPER.id()),
            Map.entry(Tags.Blocks.ORES_IRON, Abilities.IRON.id()),
            Map.entry(Tags.Blocks.ORES_GOLD, Abilities.GOLD.id()),
            Map.entry(Tags.Blocks.ORES_DIAMOND, Abilities.DIAMOND.id()),
            Map.entry(Tags.Blocks.OBSIDIANS, Abilities.OBSIDIAN.id()),
            Map.entry(Tags.Blocks.NETHERRACKS, Abilities.NETHERRACK.id()),
            Map.entry(Tags.Blocks.ORES_QUARTZ, Abilities.QUARTZ.id()),

            Map.entry(Tags.Blocks.CHESTS, Abilities.CHEST.id())
    );

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        BlockState blockState = event.getLevel().getBlockState(event.getPos());

        for (Map.Entry<TagKey<Block>, String> entry : ABILITY_TAGS.entrySet()) {
            if (blockState.is(entry.getKey()) && !AbilityChecker.isUnlocked(event.getPlayer(), entry.getValue())) {
                event.setCanceled(true);
                return;
            }
        }
    }
}