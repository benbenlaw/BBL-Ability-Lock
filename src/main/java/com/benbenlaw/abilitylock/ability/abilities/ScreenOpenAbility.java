package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class ScreenOpenAbility extends Ability {

    public boolean blocksScreen(LocalPlayer player, Screen screen, AbilityData data) {
        if (player.isCreative() || player.isSpectator()) return false;
        if (!matchesTarget(screen, data.targets())) return false;
        return !AbilityChecker.isUnlocked(player, getId());
    }

    private boolean matchesTarget(Screen screen, List<String> targets) {
        for (String target : targets) {
            if (target.equals("inventory") && screen instanceof InventoryScreen) return true;
        }
        return false;
    }
}