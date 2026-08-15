package com.benbenlaw.abilitylock.task.tasks;

import com.benbenlaw.abilitylock.task.TaskEvent;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemObtainTask extends TaskType {

    @Override
    public int amountFor(TaskEvent event) {
        return 0;
    }

    @Override
    public int countInInventory(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (matchesTarget(stack.getItem(), getTargets())) count += stack.getCount();
        }
        return count;
    }

    private boolean matchesTarget(Item item, List<String> targets) {
        for (String target : targets) {
            if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
                if (item.builtInRegistryHolder().is(tag)) return true;
            } else {
                Identifier id = Identifier.parse(target);
                if (id.equals(BuiltInRegistries.ITEM.getKey(item))) return true;
            }
        }
        return false;
    }
}