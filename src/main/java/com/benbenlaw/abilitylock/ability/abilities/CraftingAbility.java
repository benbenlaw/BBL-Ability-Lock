package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class CraftingAbility extends Ability {

    public boolean blocksRecipe(Player player, Identifier recipeId, AbilityData data) {
        if (!matchesTarget(recipeId, data.targets())) return false;
        return !AbilityChecker.isUnlocked(player, getId());
    }

    private boolean matchesTarget(Identifier recipeId, List<String> targets) {
        for (String target : targets) {
            if (Identifier.parse(target).equals(recipeId)) return true;
        }
        return false;
    }
}