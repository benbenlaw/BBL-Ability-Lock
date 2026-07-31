package com.benbenlaw.abilitylock.task.criteria;

import com.benbenlaw.abilitylock.task.TaskCriterion;
import com.benbenlaw.abilitylock.task.TaskCriterionType;
import com.benbenlaw.abilitylock.task.TaskEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemStackObtainCriterion(ItemStackTemplate template, int target) implements TaskCriterion {
    @Override
    public TaskCriterionType type() {
        return TaskCriterionType.ITEM_STACK_OBTAIN;
    }

    @Override
    public int amountFor(TaskEvent event) {
        return 0;
    }

    @Override
    public int countInInventory(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (template.create().is(stack.getItem())) count += stack.getCount();
        }
        return count;
    }
}