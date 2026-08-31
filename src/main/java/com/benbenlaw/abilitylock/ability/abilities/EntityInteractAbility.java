package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class EntityInteractAbility extends Ability {

    @Override
    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event, AbilityData data) {
        Entity target = event.getTarget();
        if (!matchesTarget(target, data.targets())) return;

        Player player = event.getEntity();
        if (player.isCreative()) return;

        if (!AbilityChecker.isUnlocked(player, getId())) {
            if (player.level().isClientSide()) {
            player.sendSystemMessage(Component.translatable("message.abilitylock.cant_interact_with_entity", getDisplayName()));
            }
            event.setCanceled(true);
        }
    }

    private boolean matchesTarget(Entity entity, List<String> targets) {
        EntityType<?> type = entity.getType();
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(target.substring(1)));
                if (entity.is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type))) return true;
            }
        }
        return false;
    }
}