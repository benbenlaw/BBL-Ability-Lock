package com.benbenlaw.abilitylock.ability;

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

    //Screens Locks
    public static final Ability CRAFTING = AbilityRegistry.register("crafting", "Crafting", "inventory");
    public static final Ability CHEST = AbilityRegistry.register("chests", "Chests", "inventory");
    public static final Ability FURNACE = AbilityRegistry.register("furnace", "Furnace", "crafting");
    public static final Ability ANVIL = AbilityRegistry.register("anvil", "Anvil", "furnace");
    public static final Ability SMOKER = AbilityRegistry.register("smoker", "Smoker", "furnace");
    public static final Ability BLAST_FURNACE = AbilityRegistry.register("blast_furnace", "Blast Furnace", "furnace");
    public static final Ability ENCHANTING = AbilityRegistry.register("enchanting", "Enchanting", "obsidian");

    //Breaking Locks
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

    //Dimensions
    public static final Ability NETHER = AbilityRegistry.register("nether", "Nether", "obsidian");
    //public static final Ability END = AbilityRegistry.register("end", "End", null);

    //Nether


    public static void init() {
    }
}