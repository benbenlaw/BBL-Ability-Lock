package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.List;

public class DimensionTravelAbility extends Ability {

    @Override
    public void onDimensionTravel(EntityTravelToDimensionEvent event, Player player, ResourceKey<Level> targetDimension, AbilityData data) {
        if (!matchesTarget(targetDimension, data.targets())) return;

        if (!AbilityChecker.isUnlocked(player, getId())) {
            player.sendSystemMessage(Component.translatable("message.abilitylock.cant_travel_to_dimension", getDisplayName()));
            event.setCanceled(true);
        }
    }

    private boolean matchesTarget(ResourceKey<Level> dimension, List<String> targets) {
        for (String target : targets) {
            Identifier id = Identifier.parse(target);
            if (id.equals(dimension.identifier())) return true;
        }
        return false;
    }
}
