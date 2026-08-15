package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityTypes;
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

public class AbilityProvider implements DataProvider {

    public static final String INTERACTIONS = "interactions";
    public static final String BREAKING = "breaking";
    public static final String DIMENSIONS = "dimensions";
    public static final String COMBAT = "combat";
    public static final String CRAFTING = "crafting";
    public static final String MOVEMENT = "movement";

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public AbilityProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "ability");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.lookupProvider.thenCompose(registries -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            HolderGetter<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

            //Block Interactions
            save(futures, cachedOutput, INTERACTIONS, "crafting_table",
                    AbilityBuilder.ability("Crafting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("inventory")
                            .targetTag(Identifier.parse("c:player_workstations/crafting_tables")));

            save(futures, cachedOutput, INTERACTIONS, "chests",
                    AbilityBuilder.ability("Chests")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .targetTag(Identifier.parse("c:chests")));

            save(futures, cachedOutput, INTERACTIONS, "furnace",
                    AbilityBuilder.ability("Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .targetTag(Identifier.parse("c:player_workstations/furnaces")));

            save(futures, cachedOutput, INTERACTIONS, "anvil",
                    AbilityBuilder.ability("Anvil")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .targets("minecraft:anvil", "minecraft:chipped_anvil"));

            save(futures, cachedOutput, INTERACTIONS, "smoker",
                    AbilityBuilder.ability("Smoker")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "furnace"))
                            .target("minecraft:smoker"));

            save(futures, cachedOutput, INTERACTIONS, "blast_furnace",
                    AbilityBuilder.ability("Blast Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "furnace"))
                            .target("minecraft:blast_furnace"));

            save(futures, cachedOutput, INTERACTIONS, "enchanting_table",
                    AbilityBuilder.ability("Enchanting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .target("minecraft:enchanting_table"));

            // FIXED: parent was blank (""), target was copy-pasted from
            // enchanting_table above ("minecraft:enchanting_table") -
            // corrected to smithing_table's own block.
            save(futures, cachedOutput, INTERACTIONS, "smithing_table",
                    AbilityBuilder.ability("Smithing")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .target("minecraft:smithing_table"));

            //Block Breaking
            save(futures, cachedOutput, BREAKING, "break_logs",
                    AbilityBuilder.ability("Breaking Logs")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .targetTag(Identifier.parse("minecraft:logs")));

            // FIXED: parent was "breaking_logs" (typo, extra "ing" - no
            // such id exists) - corrected to the real id, break_logs.
            save(futures, cachedOutput, BREAKING, "break_stone",
                    AbilityBuilder.ability("Breaking Stone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_logs"))
                            .targets("minecraft:stone", "minecraft:cobblestone"));

            save(futures, cachedOutput, BREAKING, "break_andesite",
                    AbilityBuilder.ability("Breaking Andesite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_stone"))
                            .target("minecraft:andesite"));

            save(futures, cachedOutput, BREAKING, "break_diorite",
                    AbilityBuilder.ability("Breaking Diorite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_stone"))
                            .target("minecraft:diorite"));

            save(futures, cachedOutput, BREAKING, "break_granite",
                    AbilityBuilder.ability("Breaking Granite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_stone"))
                            .target("minecraft:granite"));

            save(futures, cachedOutput, BREAKING, "break_coal",
                    AbilityBuilder.ability("Breaking Coal")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_stone"))
                            .targetTag(Identifier.parse("c:ores/coal")));

            save(futures, cachedOutput, BREAKING, "break_copper",
                    AbilityBuilder.ability("Breaking Copper")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_coal"))
                            .targetTag(Identifier.parse("c:ores/copper")));

            save(futures, cachedOutput, BREAKING, "break_iron",
                    AbilityBuilder.ability("Breaking Iron")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_coal"))
                            .targetTag(Identifier.parse("c:ores/iron")));

            save(futures, cachedOutput, BREAKING, "break_lapis",
                    AbilityBuilder.ability("Breaking Lapis Lazuli")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_copper"))
                            .targetTag(Identifier.parse("c:ores/lapis")));

            save(futures, cachedOutput, BREAKING, "break_redstone",
                    AbilityBuilder.ability("Breaking Redstone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_lapis"))
                            .targetTag(Identifier.parse("c:ores/redstone")));

            save(futures, cachedOutput, BREAKING, "break_gold",
                    AbilityBuilder.ability("Breaking Gold")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_iron"))
                            .targetTag(Identifier.parse("c:ores/gold")));

            save(futures, cachedOutput, BREAKING, "break_diamond",
                    AbilityBuilder.ability("Breaking Diamond")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_gold"))
                            .targetTag(Identifier.parse("c:ores/diamond")));

            save(futures, cachedOutput, BREAKING, "break_emerald",
                    AbilityBuilder.ability("Breaking Emerald")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_gold"))
                            .targetTag(Identifier.parse("c:ores/emerald")));

            save(futures, cachedOutput, BREAKING, "break_obsidian",
                    AbilityBuilder.ability("Breaking Obsidian")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(BREAKING, "break_diamond"))
                            .targets("minecraft:obsidian", "minecraft:crying_obsidian"));

            save(futures, cachedOutput, BREAKING, "break_netherrack",
                    AbilityBuilder.ability("Breaking Netherrack")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent(path(DIMENSIONS, "nether_travel"))
                            .targets("minecraft:netherrack", "minecraft:warped_nylium", "minecraft:crimson_nylium"));

            //Dimensions
            save(futures, cachedOutput, DIMENSIONS, "nether_travel",
                    AbilityBuilder.ability("Nether Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent(path(BREAKING, "break_obsidian"))
                            .targets("minecraft:the_nether"));

            save(futures, cachedOutput, DIMENSIONS, "end_travel",
                    AbilityBuilder.ability("End Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent(path(DIMENSIONS, "nether_travel"))
                            .targets("minecraft:the_end"));

            //Entity Hurting
            save(futures, cachedOutput, COMBAT, "passive_mob_damage",
                    AbilityBuilder.ability("Passive Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent(path(CRAFTING, "stone_tools"))
                            .targets("minecraft:cow", "minecraft:sheep", "minecraft:chicken", "minecraft:pig", "minecraft:bat", "minecraft:bee", "minecraft:fox", "minecraft:wolf"));

            save(futures, cachedOutput, COMBAT, "passive_aquatic_mob_damage",
                    AbilityBuilder.ability("Passive Aquatic Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent(path(COMBAT, "passive_mob_damage"))
                            .targets("minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish", "minecraft:dolphin", "minecraft:turtle", "minecraft:squid", "minecraft:glow_squid"));

            save(futures, cachedOutput, COMBAT, "hostile_overworld_mob_damage",
                    AbilityBuilder.ability("Hostile Overworld Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent(path(COMBAT, "passive_mob_damage"))
                            .targetTag(Identifier.parse("minecraft:zombies"))
                            .targetTag(Identifier.parse("minecraft:skeletons"))
                            .targets("minecraft:creeper", "minecraft:enderman", "minecraft:spider", "minecraft:witch"));

            save(futures, cachedOutput, COMBAT, "nether_mob_damage",
                    AbilityBuilder.ability("Nether Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent(path(DIMENSIONS, "nether_travel"))
                            .targets("minecraft:magma_cube", "minecraft:zombified_piglin", "minecraft:wither_skeleton", "minecraft:strider", "minecraft:blaze"));

            //Recipe
            save(futures, cachedOutput, CRAFTING, "wooden_tools",
                    AbilityBuilder.ability("Wooden Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(INTERACTIONS, "crafting_table"))
                            .targets("minecraft:wooden_pickaxe", "minecraft:wooden_axe", "minecraft:wooden_shovel", "minecraft:wooden_hoe", "minecraft:wooden_sword"));

            save(futures, cachedOutput, CRAFTING, "stone_tools",
                    AbilityBuilder.ability("Stone Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "wooden_tools"))
                            .targets("minecraft:stone_pickaxe", "minecraft:stone_axe", "minecraft:stone_shovel", "minecraft:stone_hoe", "minecraft:stone_sword"));

            save(futures, cachedOutput, CRAFTING, "copper_tools",
                    AbilityBuilder.ability("Copper Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "stone_tools"))
                            .targets("minecraft:copper_pickaxe", "minecraft:copper_axe", "minecraft:copper_shovel", "minecraft:copper_hoe", "minecraft:copper_sword"));

            save(futures, cachedOutput, CRAFTING, "iron_tools",
                    AbilityBuilder.ability("Iron Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "copper_tools"))
                            .targets("minecraft:iron_pickaxe", "minecraft:iron_axe", "minecraft:iron_shovel", "minecraft:iron_hoe", "minecraft:iron_sword"));

            save(futures, cachedOutput, CRAFTING, "gold_tools",
                    AbilityBuilder.ability("Gold Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "copper_tools"))
                            .targets("minecraft:golden_pickaxe", "minecraft:golden_axe", "minecraft:golden_shovel", "minecraft:golden_hoe", "minecraft:golden_sword"));

            save(futures, cachedOutput, CRAFTING, "diamond_tools",
                    AbilityBuilder.ability("Diamond Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "gold_tools"))
                            .targets("minecraft:diamond_pickaxe", "minecraft:diamond_axe", "minecraft:diamond_shovel", "minecraft:diamond_hoe", "minecraft:diamond_sword"));

            save(futures, cachedOutput, CRAFTING, "netherite_tools",
                    AbilityBuilder.ability("Netherite Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent(path(CRAFTING, "diamond_tools"))
                            .targets("minecraft:netherite_pickaxe", "minecraft:netherite_axe", "minecraft:netherite_shovel", "minecraft:netherite_hoe", "minecraft:netherite_sword"));

            //Movement
            save(futures, cachedOutput, MOVEMENT, "forward",
                    AbilityBuilder.ability("Forward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("forward"));

            save(futures, cachedOutput, MOVEMENT, "backward",
                    AbilityBuilder.ability("Backward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .parent(path(MOVEMENT, "forward"))
                            .targets("backward"));

            save(futures, cachedOutput, MOVEMENT, "left",
                    AbilityBuilder.ability("Left")
                            .parent(path(MOVEMENT, "backward"))
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("left"));

            save(futures, cachedOutput, MOVEMENT, "right",
                    AbilityBuilder.ability("Right")
                            .parent(path(MOVEMENT, "backward"))
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("right"));

            save(futures, cachedOutput, MOVEMENT, "jump",
                    AbilityBuilder.ability("Jump")
                            .parent(path(MOVEMENT, "backward"))
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("jump"));

            save(futures, cachedOutput, MOVEMENT, "crouch",
                    AbilityBuilder.ability("Crouch")
                            .parent(path(MOVEMENT, "jump"))
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("crouch"));

            save(futures, cachedOutput, MOVEMENT, "sprint",
                    AbilityBuilder.ability("Sprint")
                            .parent(path(MOVEMENT, "jump"))
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("sprint"));


            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private static String path(String folder, String name) {
        return folder + "/" + name;
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String folder, String name, AbilityBuilder builder) {
        Identifier id = AbilityLock.identifier(path(folder, name));
        Path filePath = pathProvider.json(id);
        AbilityData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, AbilityData.CODEC, data, filePath));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Abilities"; }
}