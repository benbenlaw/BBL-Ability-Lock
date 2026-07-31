package com.benbenlaw.abilitylock.events.abilities;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber
public class DimensionAbilityHandler {

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getDimension().equals(Level.NETHER)) {
            if (!AbilityChecker.isUnlocked(player, Abilities.NETHER.id())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("Locked!"));
            }
        }

        //if (event.getDimension().equals(Level.END)) {
        //    if (!AbilityChecker.isUnlocked(player, Abilities.END.id())) {
        //        event.setCanceled(true);
        //        player.sendSystemMessage(Component.literal("Locked!"));
        //    }
        //}
    }
}