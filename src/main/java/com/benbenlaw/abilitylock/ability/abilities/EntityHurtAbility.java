package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public class EntityHurtAbility extends Ability {
 
    @Override
    public void onEntityDamage(LivingDamageEvent.Pre event, AbilityData data) {
        Entity entity = event.getEntity();
        DamageSource source = event.getSource();

        if (entity instanceof Player player && player.isCreative()) return;

        if (!matchesTarget(entity.getType(), data.targets())) return;

        if (source.getEntity() instanceof Player player) {
            if (!AbilityChecker.isUnlocked(player, getId())) {
                player.sendSystemMessage(Component.translatable("message.abilitylock.cant_damage_entity", getDisplayName()));
                event.setNewDamage(0.0f);
            }
        }
    }
 
    private boolean matchesTarget(EntityType<?> entityType, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(target.substring(1)));
                if (entityType.builtInRegistryHolder().is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) return true;
            }
        }
        return false;
    }
}