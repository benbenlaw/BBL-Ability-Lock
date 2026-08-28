package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class StanceAbility extends Ability {

    private static final Identifier HALF_SPRINT_MODIFIER_ID = AbilityLock.identifier("half_sprint");

    private static final double HALF_SPRINT_MODIFIER_VALUE = -0.1154;

    public static boolean isAllowed(Player player, String target) {
        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof StanceAbility)) continue;

            AbilityData data = ability.getData();
            if (data == null || !data.targets().contains(target)) continue;

            if (!AbilityChecker.isUnlocked(player, ability.getId())) return false;
        }
        return true;
    }

    public static void updateHalfSprintModifier(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean shouldHaveHalfSpeed = player.isSprinting()
                && isAllowed(player, "first_half_sprint")
                && !isAllowed(player, "sprint");

        boolean hasModifier = speed.getModifier(HALF_SPRINT_MODIFIER_ID) != null;

        if (shouldHaveHalfSpeed && !hasModifier) {
            speed.addTransientModifier(new AttributeModifier(
                    HALF_SPRINT_MODIFIER_ID, HALF_SPRINT_MODIFIER_VALUE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (!shouldHaveHalfSpeed && hasModifier) {
            speed.removeModifier(HALF_SPRINT_MODIFIER_ID);
        }
    }
}