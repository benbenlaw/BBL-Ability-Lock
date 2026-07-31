package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class InventoryAbilityHandler {

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isCreative()) return;

        if (event.getNewScreen() instanceof InventoryScreen
                && !AbilityChecker.isUnlocked(player, Abilities.INVENTORY.id())) {
            event.setCanceled(true);
            return;
        }

        if (event.getNewScreen() instanceof CraftingScreen
                && !AbilityChecker.isUnlocked(player, Abilities.CRAFTING.id())) {
            event.setCanceled(true);
        }

        if (event.getNewScreen() instanceof FurnaceScreen
                && !AbilityChecker.isUnlocked(player, Abilities.FURNACE.id())) {
            event.setCanceled(true);
        }

        if (event.getNewScreen() instanceof BlastFurnaceScreen
                && !AbilityChecker.isUnlocked(player, Abilities.BLAST_FURNACE.id())) {
            event.setCanceled(true);
        }

        if (event.getNewScreen() instanceof SmokerScreen
                && !AbilityChecker.isUnlocked(player, Abilities.SMOKER.id())) {
            event.setCanceled(true);
        }

        if (event.getNewScreen() instanceof AnvilScreen
                && !AbilityChecker.isUnlocked(player, Abilities.ANVIL.id())) {
            event.setCanceled(true);
        }

        if (event.getNewScreen() instanceof EnchantmentScreen
                && !AbilityChecker.isUnlocked(player, Abilities.ENCHANTING.id())) {
            event.setCanceled(true);
        }
    }
}