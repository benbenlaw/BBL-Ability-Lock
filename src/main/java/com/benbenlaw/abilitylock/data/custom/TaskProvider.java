package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.task.TaskData;
import com.benbenlaw.abilitylock.task.TaskTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TaskProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public TaskProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "task");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.lookupProvider.thenCompose(registries -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            HolderGetter<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

            //Mining
            save(futures, cachedOutput, "mine_dirt",
                    TaskBuilder.task("Mine Dirt")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matches("minecraft:grass_block")
                            .matchesTag(Identifier.parse("minecraft:dirt")));

            save(futures, cachedOutput, "mine_sand",
                    TaskBuilder.task("Mine Sand")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matchesTag(Identifier.parse("c:sands")));

            save(futures, cachedOutput, "mine_gravel",
                    TaskBuilder.task("Mine Gravel")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matchesTag(Identifier.parse("c:gravels")));

            save(futures, cachedOutput, "mine_stone",
                    TaskBuilder.task("Mine Stone")
                            .type(TaskTypes.BLOCK_BREAK)
                            .requiredAbility("break_stone").requiredAbility("crafting_wooden_tools")
                            .parent("mine_dirt")
                            .target(8)
                            .matchesTag(Identifier.parse("c:stones")));


            //Obtain Item
            save(futures, cachedOutput, "obtain_egg",
                    TaskBuilder.task("Obtain Chicken Egg")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .target(1)
                            .matchesTag(Identifier.parse("minecraft:eggs")));

            save(futures, cachedOutput,  "obtain_crafting_table",
                    TaskBuilder.task("Obtain a Crafting Table")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("inventory").requiredAbility("break_logs")
                            .target(1)
                            .matches("minecraft:crafting_table"));

            save(futures, cachedOutput, "obtain_wooden_pickaxe",
                    TaskBuilder.task("Obtain a Wooden Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_wooden_tools")
                            .target(1)
                            .matches("minecraft:wooden_pickaxe"));

            save(futures, cachedOutput, "obtain_stone_pickaxe",
                    TaskBuilder.task("Obtain a Stone Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_stone_tools")
                            .parent("obtain_wooden_pickaxe").requiredAbility("break_stone")
                            .target(1)
                            .matches("minecraft:stone_pickaxe"));

            save(futures, cachedOutput, "obtain_copper_pickaxe",
                    TaskBuilder.task("Obtain a Copper Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_copper_tools").requiredAbility("break_copper").requiredAbility("furnace")
                            .parent("obtain_stone_pickaxe")
                            .target(1)
                            .matches("minecraft:copper_pickaxe"));

            save(futures, cachedOutput, "obtain_iron_pickaxe",
                    TaskBuilder.task("Obtain an Iron Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_iron_tools").requiredAbility("break_iron").requiredAbility("furnace")
                            .parent("obtain_copper_pickaxe")
                            .target(1)
                            .matches("minecraft:iron_pickaxe"));

            save(futures, cachedOutput, "obtain_gold_pickaxe",
                    TaskBuilder.task("Obtain an Gold Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_gold_tools").requiredAbility("break_gold").requiredAbility("furnace")
                            .parent("obtain_copper_pickaxe")
                            .target(1)
                            .matches("minecraft:golden_pickaxe"));

            save(futures, cachedOutput, "obtain_diamond_pickaxe",
                    TaskBuilder.task("Obtain a Diamond Pickaxe")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("crafting_diamond_tools").requiredAbility("break_diamond")
                            .parent("obtain_gold_pickaxe")
                            .target(1)
                            .matches("minecraft:diamond_pickaxe"));

            save(futures, cachedOutput, "obtain_logs",
                    TaskBuilder.task("Obtain Logs")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_logs")
                            .target(8)
                            .matchesTag(Identifier.parse("minecraft:logs")));

            save(futures, cachedOutput, "obtain_cobblestone",
                    TaskBuilder.task("Obtain Cobblestone")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_stone")
                            .parent("mine_stone")
                            .target(8)
                            .matches("minecraft:cobblestone"));

            save(futures, cachedOutput, "obtain_granite",
                    TaskBuilder.task("Obtain Granite")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_granite")
                            .parent("obtain_cobblestone")
                            .target(8)
                            .matches("minecraft:granite"));

            save(futures, cachedOutput, "obtain_diorite",
                    TaskBuilder.task("Obtain Diorite")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_diorite")
                            .parent("obtain_cobblestone")
                            .target(8)
                            .matches("minecraft:diorite"));

            save(futures, cachedOutput, "obtain_andesite",
                    TaskBuilder.task("Obtain Andesite")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_andesite")
                            .parent("obtain_cobblestone")
                            .target(8)
                            .matches("minecraft:andesite"));

            save(futures, cachedOutput, "obtain_furnace",
                    TaskBuilder.task("Obtain Furnace")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_stone")
                            .parent("obtain_cobblestone")
                            .target(1)
                            .matches("minecraft:furnace"));

            save(futures, cachedOutput, "obtain_sugar_cane",
                    TaskBuilder.task("Obtain Sugar Canes")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .target(8)
                            .matches("minecraft:sugar_cane"));

            save(futures, cachedOutput, "obtain_pumpkin",
                    TaskBuilder.task("Obtain Pumpkin")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .target(1)
                            .matches("minecraft:pumpkin"));

            save(futures, cachedOutput, "obtain_wheat_seeds",
                    TaskBuilder.task("Obtain Wheat Seeds")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .target(8)
                            .matches("minecraft:wheat_seeds"));

            save(futures, cachedOutput, "obtain_eye_of_ender",
                    TaskBuilder.task("Obtain Eye of Ender")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("dimension_nether")
                            .parent("kill_blaze")
                            .target(1)
                            .matches("minecraft:ender_eye"));

            save(futures, cachedOutput, "obtain_golden_apple",
                    TaskBuilder.task("Obtain Golden Apple")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_gold")
                            .target(1)
                            .matches("minecraft:golden_apple"));

            save(futures, cachedOutput, "obtain_lapis_block",
                    TaskBuilder.task("Obtain Lapis Block")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_lapis")
                            .target(1)
                            .matches("minecraft:lapis_block"));

            save(futures, cachedOutput, "obtain_redstone_block",
                    TaskBuilder.task("Obtain Redstone Block")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_redstone")
                            .target(1)
                            .matches("minecraft:redstone_block"));

            save(futures, cachedOutput, "obtain_quartz_block",
                    TaskBuilder.task("Obtain Quartz Block")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("dimension_nether").requiredAbility("break_quartz")
                            .target(1)
                            .matches("minecraft:quartz_block"));

            save(futures, cachedOutput, "obtain_enchantment_table",
                    TaskBuilder.task("Obtain Enchantment Table")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("break_obsidian")
                            .target(1)
                            .matches("minecraft:enchanting_table"));

            save(futures, cachedOutput, "obtain_leather",
                    TaskBuilder.task("Obtain Leather")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:leather"));

            save(futures, cachedOutput, "obtain_feather",
                    TaskBuilder.task("Obtain Feather")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:feather"));

            save(futures, cachedOutput, "obtain_saddle",
                    TaskBuilder.task("Obtain Saddle")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("passive_mob_damage").requiredAbility("break_iron")
                            .parent("obtain_leather")
                            .target(1)
                            .matches("minecraft:saddle"));

            save(futures, cachedOutput, "obtain_name_tag",
                    TaskBuilder.task("Obtain Name Tag")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .requiredAbility("passive_mob_damage").requiredAbility("break_copper")
                            .parent("obtain_sugar_cane")
                            .target(1)
                            .matches("minecraft:name_tag"));

            //Kill Entity
            save(futures, cachedOutput, "kill_bee",
                    TaskBuilder.task("Kill a Bee")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage").requiredAbility("jump")
                            .target(1)
                            .matches("minecraft:bee"));

            save(futures, cachedOutput, "kill_bat",
                    TaskBuilder.task("Kill a Bat")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage").requiredAbility("jump")
                            .target(1)
                            .matches("minecraft:bat"));

            save(futures, cachedOutput, "kill_sheep",
                    TaskBuilder.task("Kill a Sheep")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:sheep"));

            save(futures, cachedOutput, "kill_cow",
                    TaskBuilder.task("Kill a Cow")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:cow"));

            save(futures, cachedOutput, "kill_pig",
                    TaskBuilder.task("Kill a Pig")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:pig"));

            save(futures, cachedOutput, "kill_chicken",
                    TaskBuilder.task("Kill a Chicken")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_mob_damage")
                            .target(1)
                            .matches("minecraft:chicken"));

            save(futures, cachedOutput, "kill_squid",
                    TaskBuilder.task("Kill a Squid")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_aquatic_mob_damage").requiredAbility("jump")
                            .target(1)
                            .matches("minecraft:squid"));

            save(futures, cachedOutput, "kill_fish",
                    TaskBuilder.task("Kill a Fish")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("passive_aquatic_mob_damage").requiredAbility("jump")
                            .target(1)
                            .matches("minecraft:salmon", "minecraft:cod", "minecraft:tropical_fish", "minecraft:pufferfish"));

            save(futures, cachedOutput, "kill_zombie",
                    TaskBuilder.task("Kill a Zombie")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("hostile_overworld_mob_damage")
                            .target(1)
                            .matchesTag(Identifier.parse("minecraft:zombies")));

            save(futures, cachedOutput, "kill_skeleton",
                    TaskBuilder.task("Kill a Skeleton")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("hostile_overworld_mob_damage")
                            .target(1)
                            .matchesTag(Identifier.parse("minecraft:skeletons")));

            save(futures, cachedOutput, "kill_spider",
                    TaskBuilder.task("Kill a Spider")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("hostile_overworld_mob_damage")
                            .target(1)
                            .matches("minecraft:spider", "minecraft:cave_spider"));

            save(futures, cachedOutput, "kill_creeper",
                    TaskBuilder.task("Kill a Creeper")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("hostile_overworld_mob_damage")
                            .target(1)
                            .matches("minecraft:creeper"));

            save(futures, cachedOutput, "kill_blaze",
                    TaskBuilder.task("Kill a Blaze")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("nether_mob_damage").requiredAbility("dimension_nether")
                            .target(1)
                            .matches("minecraft:blaze"));

            save(futures, cachedOutput, "kill_wither_skeleton",
                    TaskBuilder.task("Kill a Wither Skeleton")
                            .type(TaskTypes.ENTITY_KILL)
                            .requiredAbility("nether_mob_damage").requiredAbility("dimension_nether")
                            .target(1)
                            .matches("minecraft:wither_skeleton"));



            //BOSSES
            //WIP


            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, TaskBuilder builder) {
        Identifier id = AbilityLock.identifier(name);
        Path filePath = pathProvider.json(id);
        TaskData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, TaskData.CODEC, data, filePath));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Tasks"; }
}