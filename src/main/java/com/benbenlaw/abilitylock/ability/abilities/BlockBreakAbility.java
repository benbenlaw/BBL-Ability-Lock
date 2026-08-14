package com.benbenlaw.abilitylock.ability.abilities;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.List;

public class BlockBreakAbility extends Ability {

    @Override
    public void onBlockBreak(BreakBlockEvent event, AbilityData data) {
        Block block = event.getState().getBlock();
        if (!matchesTarget(block, data.targets())) return;

        Player player = event.getPlayer();
        if (player.isCreative()) return;

        if (!AbilityChecker.isUnlocked(player, getId())) {
            player.sendSystemMessage(Component.translatable("message.abilitylock.cant_break_block", getDisplayName()));
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