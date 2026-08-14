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
            save(futures, cachedOutput, AbilityLock.identifier("crafting_table"),
                    AbilityBuilder.ability("Crafting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("inventory")
                            .targetTag(Identifier.parse("c:player_workstations/crafting_tables")));

            save(futures, cachedOutput, AbilityLock.identifier("chests"),
                    AbilityBuilder.ability("Chests")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targetTag(Identifier.parse("c:chests")));

            save(futures, cachedOutput, AbilityLock.identifier("furnace"),
                    AbilityBuilder.ability("Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targetTag(Identifier.parse("c:player_workstations/furnaces")));

            save(futures, cachedOutput, AbilityLock.identifier("anvil"),
                    AbilityBuilder.ability("Anvil")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targets("minecraft:anvil", "minecraft:chipped_anvil"));

            save(futures, cachedOutput, AbilityLock.identifier("smoker"),
                    AbilityBuilder.ability("Smoker")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("furnace")
                            .target("minecraft:smoker"));

            save(futures, cachedOutput, AbilityLock.identifier("blast_furnace"),
                    AbilityBuilder.ability("Blast Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("furnace")
                            .target("minecraft:blast_furnace"));

            save(futures, cachedOutput, AbilityLock.identifier("enchanting_table"),
                    AbilityBuilder.ability("Enchanting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .target("minecraft:enchanting_table"));

            save(futures, cachedOutput, AbilityLock.identifier("smithing_table"),
                    AbilityBuilder.ability("Smithing")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("")
                            .target("minecraft:enchanting_table"));

            //Block Breaking
            save(futures, cachedOutput, AbilityLock.identifier("break_logs"),
                    AbilityBuilder.ability("Breaking Logs")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("crafting_table")
                            .targetTag(Identifier.parse("minecraft:logs")));

            save(futures, cachedOutput, AbilityLock.identifier("break_stone"),
                    AbilityBuilder.ability("Breaking Stone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("breaking_logs")
                            .targets("minecraft:stone", "minecraft:cobblestone"));

            save(futures, cachedOutput, AbilityLock.identifier("break_andesite"),
                    AbilityBuilder.ability("Breaking Andesite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:andesite"));

            save(futures, cachedOutput, AbilityLock.identifier("break_diorite"),
                    AbilityBuilder.ability("Breaking Diorite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:diorite"));

            save(futures, cachedOutput, AbilityLock.identifier("break_granite"),
                    AbilityBuilder.ability("Breaking Granite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:granite"));

            save(futures, cachedOutput, AbilityLock.identifier("break_coal"),
                    AbilityBuilder.ability("Breaking Coal")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .targetTag(Identifier.parse("c:ores/coal")));

            save(futures, cachedOutput, AbilityLock.identifier("break_copper"),
                    AbilityBuilder.ability("Breaking Copper")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_coal")
                            .targetTag(Identifier.parse("c:ores/copper")));

            save(futures, cachedOutput, AbilityLock.identifier("break_iron"),
                    AbilityBuilder.ability("Breaking Iron")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_coal")
                            .targetTag(Identifier.parse("c:ores/iron")));

            save(futures, cachedOutput, AbilityLock.identifier("break_lapis"),
                    AbilityBuilder.ability("Breaking Lapis Lazuli")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_copper")
                            .targetTag(Identifier.parse("c:ores/lapis")));

            save(futures, cachedOutput, AbilityLock.identifier("break_redstone"),
                    AbilityBuilder.ability("Breaking Redstone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_lapis")
                            .targetTag(Identifier.parse("c:ores/redstone")));

            save(futures, cachedOutput, AbilityLock.identifier("break_gold"),
                    AbilityBuilder.ability("Breaking Gold")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_iron")
                            .targetTag(Identifier.parse("c:ores/gold")));

            save(futures, cachedOutput, AbilityLock.identifier("break_diamond"),
                    AbilityBuilder.ability("Breaking Diamond")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_gold")
                            .targetTag(Identifier.parse("c:ores/diamond")));

            save(futures, cachedOutput, AbilityLock.identifier("break_emerald"),
                    AbilityBuilder.ability("Breaking Emerald")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_gold")
                            .targetTag(Identifier.parse("c:ores/emerald")));

            save(futures, cachedOutput, AbilityLock.identifier("break_obsidian"),
                    AbilityBuilder.ability("Breaking Obsidian")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_diamond")
                            .targets("minecraft:obsidian", "minecraft:crying_obsidian"));

            save(futures, cachedOutput, AbilityLock.identifier("break_netherrack"),
                    AbilityBuilder.ability("Breaking Netherrack")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("nether_travel")
                            .targets("minecraft:netherrack", "minecraft:warped_nylium", "minecraft:crimson_nylium"));

            //Dimensions
            save(futures, cachedOutput, AbilityLock.identifier("nether_travel"),
                    AbilityBuilder.ability("Nether Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent("break_obsidian")
                            .targets("minecraft:the_nether"));

            save(futures, cachedOutput, AbilityLock.identifier("end_travel"),
                    AbilityBuilder.ability("End Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent("nether_travel")
                            .targets("minecraft:the_end"));

            //Entity Hurting
            save(futures, cachedOutput, AbilityLock.identifier("passive_mob_damage"),
                    AbilityBuilder.ability("Passive Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("stone_tools")
                            .targets("minecraft:cow", "minecraft:sheep", "minecraft:chicken", "minecraft:pig", "minecraft:bat", "minecraft:bee", "minecraft:fox", "minecraft:wolf"));

            save(futures, cachedOutput, AbilityLock.identifier("passive_aquatic_mob_damage"),
                    AbilityBuilder.ability("Passive Aquatic Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("passive_mob_damage")
                            .targets("minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish", "minecraft:dolphin", "minecraft:turtle", "minecraft:squid", "minecraft:glow_squid"));

            save(futures, cachedOutput, AbilityLock.identifier("hostile_overworld_mob_damage"),
                    AbilityBuilder.ability("Hostile Overworld Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("passive_mob_damage")
                            .targetTag(Identifier.parse("minecraft:zombies"))
                            .targetTag(Identifier.parse("minecraft:skeletons"))
                            .targets("minecraft:creeper", "minecraft:enderman", "minecraft:spider", "minecraft:witch"));

            save(futures, cachedOutput, AbilityLock.identifier("nether_mob_damage"),
                    AbilityBuilder.ability("Nether Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("nether_travel")
                            .targets("minecraft:magma_cube", "minecraft:zombified_piglin", "minecraft:wither_skeleton", "minecraft:strider", "minecraft:blaze"));

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, Identifier id, AbilityBuilder builder) {
        Path path = pathProvider.json(id);
        AbilityData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, AbilityData.CODEC, data, path));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Abilities"; }
}