package com.benbenlaw.abilitylock.mixin;

import com.benbenlaw.abilitylock.ability.old.AbilityChecker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Redirect(
            method = "slotChangedCraftingGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private static void abilityLock$blockRestrictedResult(
            ResultContainer resultSlots, int slot, ItemStack result,
            AbstractContainerMenu menu, ServerLevel level, Player player, CraftingContainer container,
            ResultContainer resultSlotsParam, @Nullable RecipeHolder<CraftingRecipe> recipeHint) {

        if (!result.isEmpty()) {
            Optional<RecipeHolder<CraftingRecipe>> maybeRecipe = level.getServer().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, container.asCraftInput(), level, recipeHint);

            if (maybeRecipe.isPresent()) {
                String recipeId = maybeRecipe.get().id().identifier().toString();
                if (!AbilityChecker.canCraft(player, recipeId)) {
                    result = ItemStack.EMPTY;
                }
            }
        }

        resultSlots.setItem(slot, result);
    }
}