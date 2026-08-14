package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.old.AbilityChecker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class BlockInteract extends Ability {
 
    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event, AbilityData data) {
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!matchesTarget(block, data.targets())) return;
 
        Player player = event.getEntity();
        if (player.isCreative()) return;
 
        if (!AbilityChecker.isUnlocked(player, getId().toString())) {
            player.sendSystemMessage(Component.translatable("message.abilitylock.cant_interact_with_block", getDisplayName()));
            event.setCanceled(true);
        }
    }
 
    private boolean matchesTarget(Block block, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(target.substring(1)));
                if (block.builtInRegistryHolder().is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.BLOCK.getKey(block))) return true;
            }
        }
        return false;
    }
}