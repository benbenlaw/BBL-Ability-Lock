package com.benbenlaw.abilitylock.events.abilities;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber
public class ToolAbilityHandler {

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getPlayer().isCreative()) return;
        var held = event.getPlayer().getMainHandItem();

        //if (!AbilityChecker.isUnlocked(event.getPlayer(), Abilities.USE_TOOLS.id())) {
        //    event.setCanceled(true);
        //}
    }

    @SubscribeEvent
    public static void onRightClickToolAction(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isCreative()) return;
        var held = event.getItemStack();

        //if (!AbilityChecker.isUnlocked(event.getEntity(), Abilities.USE_TOOLS.id())) {
        //    event.setCanceled(true);
        //}
    }
}