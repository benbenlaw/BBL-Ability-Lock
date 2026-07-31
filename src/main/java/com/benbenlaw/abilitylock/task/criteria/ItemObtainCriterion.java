package com.benbenlaw.abilitylock.task.criteria;

import com.benbenlaw.abilitylock.task.TaskCriterion;
import com.benbenlaw.abilitylock.task.TaskCriterionType;
import com.benbenlaw.abilitylock.task.TaskEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemObtainCriterion(TagKey<Item> tag, int target) implements TaskCriterion {
    @Override
    public TaskCriterionType type() {
        return TaskCriterionType.ITEM_OBTAIN;
    }

    @Override
    public int amountFor(TaskEvent event) {
        return 0;
    }

    @Override
    public int countInInventory(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(tag)) count += stack.getCount();
        }
        return count;
    }
}