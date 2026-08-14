package com.benbenlaw.abilitylock.ability.old;

public class Abilities {


    //Movement Locks
    public static final Ability WALK = AbilityRegistry.register("walk", "Walking", null);

    public static final Ability JUMP = AbilityRegistry.register("jump", "Jumping", "log");
    public static final Ability SPRINT = AbilityRegistry.register("sprint", "Sprinting", "log");
    public static final Ability INVENTORY = AbilityRegistry.register("inventory", "Inventory", "log");

    public static final Ability CROUCH = AbilityRegistry.register("crouch", "Crouching", "stone");
    public static final Ability STRAFE_LEFT = AbilityRegistry.register("strafe_left", "Strafing: Left", "copper");
    public static final Ability STRAFE_RIGHT = AbilityRegistry.register("strafe_right", "Strafing: Right", "copper");
    public static final Ability BACKWARDS = AbilityRegistry.register("backwards", "Walking Backwards", "stone");

    //Screens Locks (ALL ADDED TO NEW VERSION ADDED SMITHING TABLE
    public static final Ability CRAFTING = AbilityRegistry.register("crafting", "Crafting", "inventory");
    public static final Ability CHEST = AbilityRegistry.register("chests", "Chests", "inventory");
    public static final Ability FURNACE = AbilityRegistry.register("furnace", "Furnace", "crafting");
    public static final Ability ANVIL = AbilityRegistry.register("anvil", "Anvil", "furnace");
    public static final Ability SMOKER = AbilityRegistry.register("smoker", "Smoker", "furnace");
    public static final Ability BLAST_FURNACE = AbilityRegistry.register("blast_furnace", "Blast Furnace", "furnace");
    public static final Ability ENCHANTING = AbilityRegistry.register("enchanting", "Enchanting", "obsidian");

    //Breaking Locks (ALL ADDED TO NEW VERSION AS WELL AS GRANITE, ANDESITE, DIORITE)
    public static final Ability LOG = AbilityRegistry.register("log", "Breaking Logs", "walk");
    public static final Ability STONE = AbilityRegistry.register("stone", "Breaking Stone", "crafting");
    public static final Ability COAL = AbilityRegistry.register("coal", "Breaking Coal", "stone");
    public static final Ability COPPER = AbilityRegistry.register("copper", "Breaking Copper", "coal");
    public static final Ability IRON = AbilityRegistry.register("iron", "Breaking Iron", "copper");
    public static final Ability GOLD = AbilityRegistry.register("gold", "Breaking Gold", "iron");
    public static final Ability DIAMOND = AbilityRegistry.register("diamond", "Breaking Diamond", "gold");
    public static final Ability OBSIDIAN = AbilityRegistry.register("obsidian", "Breaking Obsidian", "diamond");
    public static final Ability NETHERRACK = AbilityRegistry.register("netherrack", "Breaking Netherrack", "obsidian");
    public static final Ability QUARTZ = AbilityRegistry.register("quartz", "Breaking Quartz Ore", "netherrack");

    //Dimensions (ADDED)
    public static final Ability NETHER = AbilityRegistry.register("nether.json", "Nether", "obsidian");
    //public static final Ability END = AbilityRegistry.register("end", "End", null);

    //Combat Locks (Added with aquatic mobs ect
    public static final Ability PASSIVE_KILL = AbilityRegistry.register("passive_kills", "Passive Mobs", "wooden_tools");
    public static final Ability OVERWORLD_HOSTILE_KILLS = AbilityRegistry.register("overworld_hostile_kills", "Overworld Hostile Kills", "stone_tools");

    //Crafting Locks
    public static final Ability WOODEN_TOOLS = AbilityRegistry.register("wooden_tools", "Wooden Tools", "crafting");
    public static final Ability STONE_TOOLS = AbilityRegistry.register("stone_tools", "Stone Tools", "stone");
    public static final Ability COPPER_TOOLS = AbilityRegistry.register("copper_tools", "Copper Tools", "copper");
    public static final Ability IRON_TOOLS = AbilityRegistry.register("iron_tools", "Iron Tools", "stone");
    public static final Ability GOLD_TOOLS = AbilityRegistry.register("gold_tools", "Gold Tools", "copper");
    public static final Ability DIAMOND_TOOLS = AbilityRegistry.register("diamond_tools", "Diamond Tools", "gold");

    //Nether


    public static void init() {
    }
}