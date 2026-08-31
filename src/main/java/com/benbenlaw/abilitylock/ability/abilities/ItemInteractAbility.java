package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class ItemInteractAbility extends Ability {

    @Override
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event, AbilityData data) {
        ItemStack heldItem = event.getItemStack();
        if (!matchesTarget(heldItem, data.targets())) return;

        Player player = event.getEntity();
        if (player.isCreative()) return;

        if (!AbilityChecker.isUnlocked(player, getId())) {
            if (player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.abilitylock.cant_interact_with_item", getDisplayName()));
            }
            event.setCanceled(true);
        }
    }

    private boolean matchesTarget(ItemStack itemStack, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
                if (itemStack.is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))) return true;
            }
        }
        return false;
    }
}