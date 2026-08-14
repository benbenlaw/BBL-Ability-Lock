package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import net.minecraft.world.entity.player.Player;

public class StanceAbility extends Ability {

    public static boolean isAllowed(Player player, String target) {
        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof StanceAbility)) continue;

            AbilityData data = ability.getData();
            if (data == null || !data.targets().contains(target)) continue;

            if (!AbilityChecker.isUnlocked(player, ability.getId())) return false;
        }
        return true;
    }
}