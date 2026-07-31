package com.benbenlaw.abilitylock.events.abilities;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class StanceAbilityHandler {

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (player.isShiftKeyDown() && !AbilityChecker.isUnlocked(player, Abilities.CROUCH.id())) {
            player.setShiftKeyDown(false);
        }

        if (player.isSprinting() && !AbilityChecker.isUnlocked(player, Abilities.SPRINT.id())) {
            player.setSprinting(false);
        }
    }
}