package com.benbenlaw.abilitylock.ability.old;

import net.minecraft.world.entity.EntityType;

public class RestrictionsInit {

    public static void init() {

        //Passive Kill
        MobAbilityRestrictions.register(EntityType.CHICKEN, "passive_kill");
        MobAbilityRestrictions.register(EntityType.COW, "passive_kill");
        MobAbilityRestrictions.register(EntityType.SHEEP, "passive_kill");
        MobAbilityRestrictions.register(EntityType.PIG, "passive_kill");
        MobAbilityRestrictions.register(EntityType.BAT, "passive_kill");
        MobAbilityRestrictions.register(EntityType.BEE, "passive_kill");
        MobAbilityRestrictions.register(EntityType.FOX, "passive_kill");
        MobAbilityRestrictions.register(EntityType.WOLF, "passive_kill");

        //Overworld Hostile Kills
        MobAbilityRestrictions.register(EntityType.ENDERMAN, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.SKELETON, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.SPIDER, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.ZOMBIE, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.DROWNED, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.CREEPER, "overworld_hostile_kills");
        MobAbilityRestrictions.register(EntityType.WITCH, "overworld_hostile_kills");


        //Wooden Tools
        RecipeAbilityRestrictions.register("minecraft:wooden_pickaxe", "wooden_tools");
        RecipeAbilityRestrictions.register("minecraft:wooden_axe", "wooden_tools");
        RecipeAbilityRestrictions.register("minecraft:wooden_shovel", "wooden_tools");
        RecipeAbilityRestrictions.register("minecraft:wooden_hoe", "wooden_tools");
        RecipeAbilityRestrictions.register("minecraft:wooden_sword", "wooden_tools");

        //Stone Tools
        RecipeAbilityRestrictions.register("minecraft:stone_pickaxe", "stone_tools");
        RecipeAbilityRestrictions.register("minecraft:stone_axe", "stone_tools");
        RecipeAbilityRestrictions.register("minecraft:stone_shovel", "stone_tools");
        RecipeAbilityRestrictions.register("minecraft:stone_hoe", "stone_tools");
        RecipeAbilityRestrictions.register("minecraft:stone_sword", "stone_tools");

        //Copper Tools
        RecipeAbilityRestrictions.register("minecraft:copper_pickaxe", "copper_tools");
        RecipeAbilityRestrictions.register("minecraft:copper_axe", "copper_tools");
        RecipeAbilityRestrictions.register("minecraft:copper_shovel", "copper_tools");
        RecipeAbilityRestrictions.register("minecraft:copper_hoe", "copper_tools");
        RecipeAbilityRestrictions.register("minecraft:copper_sword", "copper_tools");

        //Iron Tools
        RecipeAbilityRestrictions.register("minecraft:iron_pickaxe", "iron_tools");
        RecipeAbilityRestrictions.register("minecraft:iron_axe", "iron_tools");
        RecipeAbilityRestrictions.register("minecraft:iron_shovel", "iron_tools");
        RecipeAbilityRestrictions.register("minecraft:iron_hoe", "iron_tools");
        RecipeAbilityRestrictions.register("minecraft:iron_sword", "iron_tools");

        //Gold Tools
        RecipeAbilityRestrictions.register("minecraft:gold_pickaxe", "gold_tools");
        RecipeAbilityRestrictions.register("minecraft:gold_axe", "gold_tools");
        RecipeAbilityRestrictions.register("minecraft:gold_shovel", "gold_tools");
        RecipeAbilityRestrictions.register("minecraft:gold_hoe", "gold_tools");
        RecipeAbilityRestrictions.register("minecraft:gold_sword", "gold_tools");

        //Diamond Tools
        RecipeAbilityRestrictions.register("minecraft:diamond_pickaxe", "diamond_tools");
        RecipeAbilityRestrictions.register("minecraft:diamond_axe", "diamond_tools");
        RecipeAbilityRestrictions.register("minecraft:diamond_shovel", "diamond_tools");
        RecipeAbilityRestrictions.register("minecraft:diamond_hoe", "diamond_tools");
        RecipeAbilityRestrictions.register("minecraft:diamond_sword", "diamond_tools");


    }
}