package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.task.criteria.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public class Tasks {

    // Starting Tasks
    public static final Task DIRT = TaskRegistry.register("dirt", "Get Dirt", null,
            new ItemObtainCriterion(ItemTags.DIRT, 8));

    public static final Task SAND = TaskRegistry.register("sand", "Get Sand", null,
            new ItemObtainCriterion(ItemTags.SAND, 8));

    public static final Task GRAVEL = TaskRegistry.register("gravel", "Get Gravel", null,
            new ItemObtainCriterion(Tags.Items.GRAVELS, 8));

    public static final Task KILL_BEE = TaskRegistry.register("kill_bee", "Kill a Bee", null,
            new EntitySpecificKillCriterion(EntityType.BEE, 1));

    public static final Task KILL_BAT = TaskRegistry.register("kill_bat", "Kill a Bat", null,
            new EntitySpecificKillCriterion(EntityType.BAT, 1));

    public static final TaskPool KILL_FLYING = TaskPoolRegistry.register("kill_flying", "kill_bee", "kill_bat");

    public static final Task KILL_SHEEP = TaskRegistry.register("kill_sheep", "Kill a Sheep", null,
            new EntitySpecificKillCriterion(EntityType.SHEEP, 1));

    public static final Task KILL_COW = TaskRegistry.register("kill_cow", "Kill a Cow", null,
            new EntitySpecificKillCriterion(EntityType.COW, 1));

    public static final Task KILL_PIG = TaskRegistry.register("kill_pig", "Kill a Pig", null,
            new EntitySpecificKillCriterion(EntityType.PIG, 1));

    public static final Task KILL_CHICKEN = TaskRegistry.register("kill_chicken", "Kill a Chicken", null,
            new EntitySpecificKillCriterion(EntityType.CHICKEN, 1));

    public static final TaskPool PASSIVE_KILL = TaskPoolRegistry.register("passive_kill", "kill_sheep", "kill_cow", "kill_pig", "kill_chicken");


    // Simple Tasks
    public static final Task LOG = TaskRegistry.register("log", "Break a Log", null,
            new BlockBreakCriterion(BlockTags.LOGS, 1));

    public static final Task CRAFTING_TABLE = TaskRegistry.register("crafting_table", "Get a Crafting Table", "log",
            new ItemObtainCriterion(Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES, 1));

    public static final Task WOODEN_PICKAXE = TaskRegistry.register("wooden_pickaxe", "Get a Wooden Pickaxe", "crafting_table",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.WOODEN_PICKAXE), 1));

    public static final Task OBTAIN_LOGS = TaskRegistry.register("obtain_logs", "Obtain Logs", "crafting_table",
            new ItemObtainCriterion(ItemTags.LOGS, 16));

    public static final Task BREAK_STONE = TaskRegistry.register("break_stone", "Mine Stone", "wooden_pickaxe",
            new BlockBreakCriterion(BlockTags.STONE_ORE_REPLACEABLES, 1));

    public static final Task OBTAIN_COBBLESTONE = TaskRegistry.register("obtain_cobblestone", "Get Cobblestone", "break_stone",
            new ItemObtainCriterion(Tags.Items.COBBLESTONES, 8));

    public static final Task STONE_PICKAXE = TaskRegistry.register("stone_pickaxe", "Get a Stone Pickaxe", "break_stone",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.STONE_PICKAXE), 1));

    public static final Task FURNACE = TaskRegistry.register("furnace", "Get a Furnace", "break_stone",
            new ItemObtainCriterion(Tags.Items.PLAYER_WORKSTATIONS_FURNACES, 1));

    public static final Task COAL = TaskRegistry.register("coal", "Get Coal", "break_stone",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.COAL), 8));

    // Advanced Tasks
    public static final Task IRON_ORE = TaskRegistry.register("iron_ore", "Mine Iron Ore", "stone_pickaxe",
            new BlockBreakCriterion(BlockTags.IRON_ORES, 1));

    public static final Task RAW_COPPER = TaskRegistry.register("raw_iron", "Mine Copper Ore", "stone_pickaxe",
            new BlockBreakCriterion(BlockTags.COPPER_ORES, 1));

    public static final Task IRON_INGOT = TaskRegistry.register("iron_ingot", "Smelt an Iron Ingot", "furnace",
            new ItemObtainCriterion(Tags.Items.INGOTS_IRON, 1));

    public static final Task COPPER_INGOT = TaskRegistry.register("copper_ingot", "Smelt an Copper Ingot", "furnace",
            new ItemObtainCriterion(Tags.Items.INGOTS_COPPER, 1));

    public static final Task IRON_PICKAXE = TaskRegistry.register("iron_pickaxe", "Get an Iron Pickaxe", "iron_ingot",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.IRON_PICKAXE), 1));

    public static final Task IRON_SWORD = TaskRegistry.register("iron_sword", "Get an Iron Sword", "iron_ingot",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.IRON_SWORD), 1));

    public static final Task IRON_ARMOR = TaskRegistry.register("iron_armor", "Get a Piece of Iron Armor", "iron_ingot",
            new ItemObtainCriterion(ItemTags.TRIMMABLE_ARMOR, 1));

    public static final Task DIAMOND = TaskRegistry.register("diamond", "Get a Diamond", "iron_pickaxe",
            new ItemObtainCriterion(Tags.Items.GEMS_DIAMOND, 1));

    public static final Task DIAMOND_PICKAXE = TaskRegistry.register("diamond_pickaxe", "Get a Diamond Pickaxe", "diamond",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 1));

    public static final Task OBSIDIAN = TaskRegistry.register("obsidian", "Get Obsidian", "diamond_pickaxe",
            new ItemObtainCriterion(Tags.Items.OBSIDIANS, 1));

    //Nether
    public static final Task NETHERRACK = TaskRegistry.register("netherrack", "Get Netherrack", "obsidian",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.NETHERRACK), 8));

    public static final Task QUARTZ_ORE = TaskRegistry.register("quartz_ore", "Mine Quartz Ore", "netherrack",
            new BlockBreakCriterion(Tags.Blocks.ORES_QUARTZ, 1));

    public static final Task KILL_BLAZE = TaskRegistry.register("kill_blaze", "Kill a Blaze", "obsidian",
            new EntitySpecificKillCriterion(EntityType.BLAZE, 1));

    public static final Task KILL_WITHER_SKELETON = TaskRegistry.register("kill_wither_skeleton", "Kill a Wither Skeleton", "obsidian",
            new EntitySpecificKillCriterion(EntityType.WITHER_SKELETON, 1));

    public static final Task ENDER_EYE = TaskRegistry.register("ender_eye", "Get an Ender Eye", "kill_blaze",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.ENDER_EYE), 1));

    public static final Task MELON_SLICE = TaskRegistry.register("melon_slice", "Get a Melon Slice", null,
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.MELON_SLICE), 1));

    public static final Task PUMPKIN = TaskRegistry.register("pumpkin", "Get a Pumpkin Slice", null,
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.MELON_SLICE), 1));

    public static final Task SUGAR_CANE = TaskRegistry.register("sugar_cane", "Get Sugar Canes", null,
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.MELON_SLICE), 8));

    public static final TaskPool PLANT_BLOCK = TaskPoolRegistry.register("plant_block", "melon_slice", "pumpkin", "sugar_cane");

    public static final Task VILLAGE = TaskRegistry.register("village", "Find a Village", null,
            new StructureLocateCriterion(List.of(
                    BuiltinStructures.VILLAGE_PLAINS,
                    BuiltinStructures.VILLAGE_DESERT,
                    BuiltinStructures.VILLAGE_SAVANNA,
                    BuiltinStructures.VILLAGE_TAIGA,
                    BuiltinStructures.VILLAGE_SNOWY
            ), 1));

    //Combat
    public static final Task KILL_ZOMBIE = TaskRegistry.register("kill_zombie", "Kill a Zombie", null,
            new EntitySpecificKillCriterion(EntityType.ZOMBIE, 1));

    public static final Task KILL_SKELETON = TaskRegistry.register("kill_skeleton", "Kill a Skeleton", null,
            new EntitySpecificKillCriterion(EntityType.SKELETON, 1));

    public static final Task KILL_SPIDER = TaskRegistry.register("kill_spider", "Kill a Spider", null,
            new EntitySpecificKillCriterion(EntityType.SPIDER, 1));

    public static final TaskPool UNDEAD_KILL = TaskPoolRegistry.register("undead_kill", "kill_zombie", "kill_skeleton", "kill_spider");



    //Completion Task
    public static final Task KILL_ENDER_DRAGON = TaskRegistry.register("kill_ender_dragon", "Kill The Ender Dragon", "ender_eye",
            new EntitySpecificKillCriterion(EntityType.ENDER_DRAGON, 1));

    public static final Task KILL_WITHER = TaskRegistry.register("kill_wither", "Kill The Wither", "ender_eye",
            new EntitySpecificKillCriterion(EntityType.WITHER, 1));

    public static final Task KILL_WARDEN = TaskRegistry.register("kill_warden", "Kill The Warden", "ender_eye",
            new EntitySpecificKillCriterion(EntityType.WARDEN, 1));

    public static final TaskPool FINAL_TASK = TaskPoolRegistry.register("final_kill", "kill_ender_dragon", "kill_wither", "kill_warden");


    public static void init() {
    }
}