package com.benbenlaw.abilitylock.events.abilities;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class InteractionAbilityHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isCreative()) return;

        var state = event.getLevel().getBlockState(event.getPos());
        var block = state.getBlock();

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof Container) {
            if (!AbilityChecker.isUnlocked(event.getEntity(), Abilities.CHEST.id())) {
                event.setCanceled(true);
            }
        }
    }
}