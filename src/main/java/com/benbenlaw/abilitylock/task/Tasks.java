package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.old.Abilities;
import com.benbenlaw.abilitylock.task.criteria.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.Set;

public class Tasks {

    public static final Task DIRT = TaskRegistry.register("dirt", "Get Dirt",
            new ItemObtainCriterion(ItemTags.DIRT, 8));

    public static final Task SAND = TaskRegistry.register("sand", "Get Sand",
            new ItemObtainCriterion(ItemTags.SAND, 8));

    public static final Task GRAVEL = TaskRegistry.register("gravel", "Get Gravel",
            new ItemObtainCriterion(Tags.Items.GRAVELS, 8));

    public static final Task KILL_BEE = TaskRegistry.register("kill_bee", "Kill a Bee",
            new EntitySpecificKillCriterion(EntityType.BEE, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_BAT = TaskRegistry.register("kill_bat", "Kill a Bat",
            new EntitySpecificKillCriterion(EntityType.BAT, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_SHEEP = TaskRegistry.register("kill_sheep", "Kill a Sheep",
            new EntitySpecificKillCriterion(EntityType.SHEEP, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_COW = TaskRegistry.register("kill_cow", "Kill a Cow",
            new EntitySpecificKillCriterion(EntityType.COW, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_PIG = TaskRegistry.register("kill_pig", "Kill a Pig",
            new EntitySpecificKillCriterion(EntityType.PIG, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_CHICKEN = TaskRegistry.register("kill_chicken", "Kill a Chicken",
            new EntitySpecificKillCriterion(EntityType.CHICKEN, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task KILL_SQUID = TaskRegistry.register("kill_squid", "Kill a Squid",
            new EntitySpecificKillCriterion(EntityType.SQUID, 1), Set.of(Abilities.PASSIVE_KILL.id()));

    public static final Task EGG = TaskRegistry.register("egg", "Get an Egg",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.EGG), 1));

    public static final Task CRAFTING_TABLE = TaskRegistry.register("crafting_table", "Get a Crafting Table",
            new ItemObtainCriterion(Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES, 1), Set.of(Abilities.LOG.id()));

    public static final Task WOODEN_PICKAXE = TaskRegistry.register("wooden_pickaxe", "Get a Wooden Pickaxe",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.WOODEN_PICKAXE), 1), Set.of(Abilities.WOODEN_TOOLS.id(), Abilities.LOG.id(), Abilities.CRAFTING.id()));

    public static final Task OBTAIN_LOGS = TaskRegistry.register("obtain_logs", "Obtain Logs",
            new ItemObtainCriterion(ItemTags.LOGS, 8), Set.of(Abilities.LOG.id()));

    public static final Task BREAK_STONE = TaskRegistry.register("break_stone", "Mine Stone",
            new BlockBreakCriterion(Tags.Blocks.STONES, 1), Set.of(Abilities.STONE.id()));

    //public static final Task BREAK_GRANITE = TaskRegistry.register("break_granite", "Mine Granite",
    //        new BlockBreakCriterion(Tags.Blocks.GRAM, 1), Set.of(Abilities.STONE.id()));
//
    //public static final Task BREAK_STONE = TaskRegistry.register("break_stone", "Mine Stone",
    //        new BlockBreakCriterion(BlockTags.STONE_ORE_REPLACEABLES, 1), Set.of(Abilities.STONE.id()));
//
    //public static final Task BREAK_STONE = TaskRegistry.register("break_stone", "Mine Stone",
    //        new BlockBreakCriterion(BlockTags.STONE_ORE_REPLACEABLES, 1), Set.of(Abilities.STONE.id()));

    public static final Task OBTAIN_COBBLESTONE = TaskRegistry.register("obtain_cobblestone", "Get Cobblestone",
            new ItemObtainCriterion(Tags.Items.COBBLESTONES, 8), Set.of(Abilities.STONE.id()));

    public static final Task STONE_PICKAXE = TaskRegistry.register("stone_pickaxe", "Get a Stone Pickaxe",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.STONE_PICKAXE), 1), Set.of(Abilities.STONE_TOOLS.id(), Abilities.STONE.id(), Abilities.CRAFTING.id()));

    public static final Task FURNACE = TaskRegistry.register("furnace", "Get a Furnace",
            new ItemObtainCriterion(Tags.Items.PLAYER_WORKSTATIONS_FURNACES, 1), Set.of(Abilities.WOODEN_TOOLS.id(), Abilities.STONE.id(), Abilities.CRAFTING.id()));

    public static final Task COAL = TaskRegistry.register("coal", "Get Coal",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.COAL), 8), Set.of(Abilities.COAL.id()));

    public static final Task IRON_ORE = TaskRegistry.register("iron_ore", "Mine Iron Ore",
            new BlockBreakCriterion(BlockTags.IRON_ORES, 1), Set.of(Abilities.IRON.id()));

    public static final Task RAW_COPPER = TaskRegistry.register("raw_copper", "Mine Copper Ore",
            new BlockBreakCriterion(BlockTags.COPPER_ORES, 1), Set.of(Abilities.COPPER.id()));

    public static final Task RAW_GOLD = TaskRegistry.register("raw_gold", "Mine Gold Ore",
            new BlockBreakCriterion(BlockTags.COPPER_ORES, 1), Set.of(Abilities.GOLD.id()));

    public static final Task IRON_INGOT = TaskRegistry.register("iron_ingot", "Obtain Iron Ingot",
            new ItemObtainCriterion(Tags.Items.INGOTS_IRON, 1), Set.of(Abilities.IRON.id()));

    public static final Task COPPER_INGOT = TaskRegistry.register("copper_ingot", "Obtain Copper Ingot",
            new ItemObtainCriterion(Tags.Items.INGOTS_COPPER, 1), Set.of(Abilities.COPPER.id()));

    public static final Task GOLD_INGOT = TaskRegistry.register("gold_ingot", "Obtain Gold Ingot",
            new ItemObtainCriterion(Tags.Items.INGOTS_COPPER, 1), Set.of(Abilities.GOLD.id()));

    public static final Task IRON_PICKAXE = TaskRegistry.register("iron_pickaxe", "Obtain an Iron Pickaxe",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.IRON_PICKAXE), 1), Set.of(Abilities.IRON.id()));

    public static final Task DIAMOND = TaskRegistry.register("diamond", "Get a Diamond",
            new ItemObtainCriterion(Tags.Items.GEMS_DIAMOND, 1), Set.of(Abilities.IRON_TOOLS.id()));

    public static final Task REDSTONE = TaskRegistry.register("redstone", "Get a Redstone",
            new ItemObtainCriterion(Tags.Items.GEMS_DIAMOND, 1), Set.of(Abilities.IRON_TOOLS.id()));

    public static final Task LAPIS = TaskRegistry.register("lapis", "Get a Lapis Lazuli",
            new ItemObtainCriterion(Tags.Items.GEMS_DIAMOND, 1), Set.of(Abilities.IRON_TOOLS.id()));

    public static final Task DIAMOND_PICKAXE = TaskRegistry.register("diamond_pickaxe", "Get a Diamond Pickaxe",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 1), Set.of(Abilities.DIAMOND.id()));

    public static final Task OBSIDIAN = TaskRegistry.register("obsidian", "Get Obsidian",
            new ItemObtainCriterion(Tags.Items.OBSIDIANS, 1), Set.of(Abilities.DIAMOND_TOOLS.id()));

    public static final Task ENCHANTING_TABLE = TaskRegistry.register("enchanting_table", "Obtain a Enchanting Table",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.DIAMOND_PICKAXE), 1), Set.of(Abilities.DIAMOND.id(), Abilities.OBSIDIAN.id()));

    public static final Task NETHERRACK = TaskRegistry.register("netherrack", "Get Netherrack",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.NETHERRACK), 8), Set.of(Abilities.NETHERRACK.id()));

    public static final Task QUARTZ_ORE = TaskRegistry.register("quartz_ore", "Mine Quartz Ore",
            new BlockBreakCriterion(Tags.Blocks.ORES_QUARTZ, 1), Set.of(Abilities.QUARTZ.id()));

    public static final Task KILL_BLAZE = TaskRegistry.register("kill_blaze", "Kill a Blaze",
            new EntitySpecificKillCriterion(EntityType.BLAZE, 1), Set.of(Abilities.NETHER.id()));

    public static final Task KILL_WITHER_SKELETON = TaskRegistry.register("kill_wither_skeleton", "Kill a Wither Skeleton",
            new EntitySpecificKillCriterion(EntityType.WITHER_SKELETON, 1), Set.of(Abilities.NETHER.id()));

    public static final Task ENDER_EYE = TaskRegistry.register("ender_eye", "Get an Ender Eye",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.ENDER_EYE), 1), Set.of(Abilities.NETHER.id()));

    public static final Task WHEAT_SEEDS = TaskRegistry.register("wheat_seeds", "Get Wheat Seeds",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.WHEAT_SEEDS), 8));

    public static final Task PUMPKIN = TaskRegistry.register("pumpkin", "Get a Pumpkin",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.PUMPKIN), 1));

    public static final Task SUGAR_CANE = TaskRegistry.register("sugar_cane", "Get Sugar Canes",
            new ItemStackObtainCriterion(new ItemStackTemplate(Items.SUGAR_CANE), 8));

    //public static final Task VILLAGE = TaskRegistry.register("village", "Find a Village",
    //        new StructureLocateCriterion(List.of(
    //                BuiltinStructures.VILLAGE_PLAINS,
    //                BuiltinStructures.VILLAGE_DESERT,
    //                BuiltinStructures.VILLAGE_SAVANNA,
    //                BuiltinStructures.VILLAGE_TAIGA,
    //                BuiltinStructures.VILLAGE_SNOWY
    //        ), 1));

    public static final Task KILL_ZOMBIE = TaskRegistry.register("kill_zombie", "Kill a Zombie",
            new EntitySpecificKillCriterion(EntityType.ZOMBIE, 1), Set.of(Abilities.OVERWORLD_HOSTILE_KILLS.id()));

    public static final Task KILL_SKELETON = TaskRegistry.register("kill_skeleton", "Kill a Skeleton",
            new EntitySpecificKillCriterion(EntityType.SKELETON, 1), Set.of(Abilities.OVERWORLD_HOSTILE_KILLS.id()));

    public static final Task KILL_SPIDER = TaskRegistry.register("kill_spider", "Kill a Spider",
            new EntitySpecificKillCriterion(EntityType.SPIDER, 1), Set.of(Abilities.OVERWORLD_HOSTILE_KILLS.id()));

    public static final Task KILL_CREEPER = TaskRegistry.register("kill_creeper", "Kill a Creeper",
            new EntitySpecificKillCriterion(EntityType.CREEPER, 1), Set.of(Abilities.OVERWORLD_HOSTILE_KILLS.id()));

    //public static final Task KILL_ENDER_DRAGON = TaskRegistry.register("kill_ender_dragon", "Kill The Ender Dragon",
    //        new EntitySpecificKillCriterion(EntityType.ENDER_DRAGON, 1), Set.of(Abilities.ENCHANTING.id()));
//
    //public static final Task KILL_WITHER = TaskRegistry.register("kill_wither", "Kill The Wither",
    //        new EntitySpecificKillCriterion(EntityType.WITHER, 1), Set.of(Abilities.ENCHANTING.id()));
//
    //public static final Task KILL_WARDEN = TaskRegistry.register("kill_warden", "Kill The Warden",
    //        new EntitySpecificKillCriterion(EntityType.WARDEN, 1), Set.of(Abilities.ENCHANTING.id()));

    public static void init() {
    }
}