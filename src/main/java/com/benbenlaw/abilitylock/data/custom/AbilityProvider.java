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
import net.neoforged.neoforge.common.Tags;

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

            //Screen
            save(futures, cachedOutput,"inventory",
                    AbilityBuilder.ability("Inventory")
                            .type(AbilityTypes.SCREEN_ABILITY)
                            .parent("forward")
                            .target("inventory"));

            //Block Interactions
            save(futures, cachedOutput,"crafting_table",
                    AbilityBuilder.ability("Crafting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("break_logs")
                            .targetTag(Identifier.parse("c:player_workstations/crafting_tables")));

            save(futures, cachedOutput, "chests",
                    AbilityBuilder.ability("Chests")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targetTag(Identifier.parse("c:chests")));

            save(futures, cachedOutput, "furnace",
                    AbilityBuilder.ability("Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targetTag(Identifier.parse("c:player_workstations/furnaces")));

            save(futures, cachedOutput,"anvil",
                    AbilityBuilder.ability("Anvil")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("crafting_table")
                            .targets("minecraft:anvil", "minecraft:chipped_anvil"));

            save(futures, cachedOutput,"smoker",
                    AbilityBuilder.ability("Smoker")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("furnace")
                            .target("minecraft:smoker"));

            save(futures, cachedOutput, "blast_furnace",
                    AbilityBuilder.ability("Blast Furnace")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("furnace")
                            .target("minecraft:blast_furnace"));

            save(futures, cachedOutput,"enchanting_table",
                    AbilityBuilder.ability("Enchanting Table")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("break_obsidian")
                            .target("minecraft:enchanting_table"));

            save(futures, cachedOutput,"smithing_table",
                    AbilityBuilder.ability("Smithing")
                            .type(AbilityTypes.BLOCK_INTERACT)
                            .parent("break_iron")
                            .target("minecraft:smithing_table"));

            //Block Breaking
            save(futures, cachedOutput, "break_logs",
                    AbilityBuilder.ability("Breaking Logs")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("inventory")
                            .targetTag(Identifier.parse("minecraft:logs")));

            save(futures, cachedOutput, "break_stone",
                    AbilityBuilder.ability("Breaking Stone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("crafting_wooden_tools")
                            .targets("minecraft:stone", "minecraft:cobblestone"));

            save(futures, cachedOutput,"break_andesite",
                    AbilityBuilder.ability("Breaking Andesite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:andesite"));

            save(futures, cachedOutput,"break_diorite",
                    AbilityBuilder.ability("Breaking Diorite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:diorite"));

            save(futures, cachedOutput, "break_granite",
                    AbilityBuilder.ability("Breaking Granite")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .target("minecraft:granite"));

            save(futures, cachedOutput, "break_coal",
                    AbilityBuilder.ability("Breaking Coal")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_stone")
                            .targetTag(Identifier.parse("c:ores/coal")));

            save(futures, cachedOutput, "break_copper",
                    AbilityBuilder.ability("Breaking Copper")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_coal")
                            .targetTag(Identifier.parse("c:ores/copper")));

            save(futures, cachedOutput, "break_iron",
                    AbilityBuilder.ability("Breaking Iron")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_coal")
                            .targetTag(Identifier.parse("c:ores/iron")));

            save(futures, cachedOutput,"break_lapis",
                    AbilityBuilder.ability("Breaking Lapis Lazuli")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_copper")
                            .targetTag(Identifier.parse("c:ores/lapis")));

            save(futures, cachedOutput, "break_redstone",
                    AbilityBuilder.ability("Breaking Redstone")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_lapis")
                            .targetTag(Identifier.parse("c:ores/redstone")));

            save(futures, cachedOutput, "break_gold",
                    AbilityBuilder.ability("Breaking Gold")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_iron")
                            .targetTag(Identifier.parse("c:ores/gold")));

            save(futures, cachedOutput, "break_diamond",
                    AbilityBuilder.ability("Breaking Diamond")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_gold")
                            .targetTag(Identifier.parse("c:ores/diamond")));

            save(futures, cachedOutput,"break_emerald",
                    AbilityBuilder.ability("Breaking Emerald")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_gold")
                            .targetTag(Identifier.parse("c:ores/emerald")));

            save(futures, cachedOutput, "break_obsidian",
                    AbilityBuilder.ability("Breaking Obsidian")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_diamond")
                            .targets("minecraft:obsidian", "minecraft:crying_obsidian"));

            save(futures, cachedOutput,"break_netherrack",
                    AbilityBuilder.ability("Breaking Netherrack")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("dimension_nether")
                            .targets("minecraft:netherrack", "minecraft:warped_nylium", "minecraft:crimson_nylium"));

            save(futures, cachedOutput,"break_quartz",
                    AbilityBuilder.ability("Breaking Quartz")
                            .type(AbilityTypes.BLOCK_BREAK)
                            .parent("break_netherrack")
                            .targets("minecraft:nether_quartz_ore", "minecraft:quartz_block"));

            //Dimensions
            save(futures, cachedOutput, "dimension_nether",
                    AbilityBuilder.ability("Nether Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent("break_obsidian")
                            .targets("minecraft:the_nether"));

            save(futures, cachedOutput, "dimension_end",
                    AbilityBuilder.ability("End Travel")
                            .type(AbilityTypes.DIMENSION_TRAVEL)
                            .parent("dimension_nether")
                            .targets("minecraft:the_end"));

            //Entity Hurting
            save(futures, cachedOutput,"passive_mob_damage",
                    AbilityBuilder.ability("Passive Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("crafting_stone_tools")
                            .targets("minecraft:cow", "minecraft:sheep", "minecraft:chicken", "minecraft:pig", "minecraft:bat", "minecraft:bee", "minecraft:fox", "minecraft:wolf"));

            save(futures, cachedOutput,"passive_aquatic_mob_damage",
                    AbilityBuilder.ability("Passive Aquatic Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("item_interact_food")
                            .targets("minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish", "minecraft:dolphin", "minecraft:turtle", "minecraft:squid", "minecraft:glow_squid"));

            save(futures, cachedOutput, "hostile_overworld_mob_damage",
                    AbilityBuilder.ability("Hostile Overworld Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("item_interact_food")
                            .targetTag(Identifier.parse("minecraft:zombies"))
                            .targetTag(Identifier.parse("minecraft:skeletons"))
                            .targets("minecraft:creeper", "minecraft:enderman", "minecraft:spider", "minecraft:witch"));

            save(futures, cachedOutput, "nether_mob_damage",
                    AbilityBuilder.ability("Nether Mob Damage")
                            .type(AbilityTypes.ENTITY_HURT)
                            .parent("hostile_overworld_mob_damage")
                            .targets("minecraft:magma_cube", "minecraft:zombified_piglin", "minecraft:wither_skeleton", "minecraft:strider", "minecraft:blaze"));

            //Recipe
            save(futures, cachedOutput,"crafting_wooden_tools",
                    AbilityBuilder.ability("Wooden Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_table")
                            .targets("minecraft:wooden_pickaxe", "minecraft:wooden_axe", "minecraft:wooden_shovel", "minecraft:wooden_hoe", "minecraft:wooden_sword"));

            save(futures, cachedOutput,"crafting_stone_tools",
                    AbilityBuilder.ability("Stone Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_wooden_tools")
                            .targets("minecraft:stone_pickaxe", "minecraft:stone_axe", "minecraft:stone_shovel", "minecraft:stone_hoe", "minecraft:stone_sword"));

            save(futures, cachedOutput,"crafting_copper_tools",
                    AbilityBuilder.ability("Copper Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_stone_tools")
                            .targets("minecraft:copper_pickaxe", "minecraft:copper_axe", "minecraft:copper_shovel", "minecraft:copper_hoe", "minecraft:copper_sword"));

            save(futures, cachedOutput, "crafting_iron_tools",
                    AbilityBuilder.ability("Iron Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_stone_tools")
                            .targets("minecraft:iron_pickaxe", "minecraft:iron_axe", "minecraft:iron_shovel", "minecraft:iron_hoe", "minecraft:iron_sword", "minecraft:saddle", "minecraft:shield"));

            save(futures, cachedOutput, "crafting_gold_tools",
                    AbilityBuilder.ability("Gold Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_copper_tools")
                            .targets("minecraft:golden_pickaxe", "minecraft:golden_axe", "minecraft:golden_shovel", "minecraft:golden_hoe", "minecraft:golden_sword"));

            save(futures, cachedOutput, "crafting_diamond_tools",
                    AbilityBuilder.ability("Diamond Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_gold_tools")
                            .targets("minecraft:diamond_pickaxe", "minecraft:diamond_axe", "minecraft:diamond_shovel", "minecraft:diamond_hoe", "minecraft:diamond_sword"));

            save(futures, cachedOutput, "crafting_netherite_tools",
                    AbilityBuilder.ability("Netherite Tools")
                            .type(AbilityTypes.CRAFTING_ABILITY)
                            .parent("crafting_diamond_tools")
                            .targets("minecraft:netherite_pickaxe", "minecraft:netherite_axe", "minecraft:netherite_shovel", "minecraft:netherite_hoe", "minecraft:netherite_sword"));

            //Movement
            save(futures, cachedOutput,"forward",
                    AbilityBuilder.ability("Forward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("forward"));

            save(futures, cachedOutput,"backward",
                    AbilityBuilder.ability("Backward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .parent("forward")
                            .targets("backward"));

            save(futures, cachedOutput, "left",
                    AbilityBuilder.ability("Left")
                            .parent("backward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("left"));

            save(futures, cachedOutput, "right",
                    AbilityBuilder.ability("Right")
                            .parent("backward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("right"));

            save(futures, cachedOutput, "jump",
                    AbilityBuilder.ability("Jump")
                            .parent("climb")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("jump"));

            save(futures, cachedOutput, "double_jump",
                    AbilityBuilder.ability("Double Jump")
                            .parent("jump")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("double_jump"));

            save(futures, cachedOutput, "swim",
                    AbilityBuilder.ability("Swim")
                            .parent("crouch")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("swim"));

            save(futures, cachedOutput, "climb",
                    AbilityBuilder.ability("Climb")
                            .parent("backward")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("climb"));

            save(futures, cachedOutput, "crouch",
                    AbilityBuilder.ability("Crouch")
                            .parent("climb")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("crouch"));

            save(futures, cachedOutput, "half_sprint",
                    AbilityBuilder.ability("First Half Sprint")
                            .parent("climb")
                            .type(AbilityTypes.MOVEMENT_ABILITY)
                            .targets("half_sprint"));

            extracted(cachedOutput, futures);

            //Entity Interactions
            save(futures, cachedOutput, "entity_interact_villager",
                    AbilityBuilder.ability("Trading")
                            .parent("item_interact_food")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:villager"));

            save(futures, cachedOutput, "entity_interact_pig",
                    AbilityBuilder.ability("Pig Riding")
                            .parent("crafting_iron_tools")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:pig"));

            save(futures, cachedOutput, "entity_interact_horse",
                    AbilityBuilder.ability("Horse Riding")
                            .parent("crafting_iron_tools")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama"));

            save(futures, cachedOutput, "entity_interact_piglin",
                    AbilityBuilder.ability("Piglin Bartering")
                            .parent("crafting_gold_tools")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:piglin"));

            save(futures, cachedOutput, "entity_interact_strider",
                    AbilityBuilder.ability("Striding Riding")
                            .parent("dimension_nether")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:strider"));

            save(futures, cachedOutput, "entity_interact_happy_ghast",
                    AbilityBuilder.ability("Happy Ghast Riding")
                            .parent("dimension_nether")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:happy_ghast"));

            save(futures, cachedOutput, "entity_interact_sheep",
                    AbilityBuilder.ability("Shearing")
                            .parent("crafting_iron_tools")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:sheep","minecraft:mooshroom"));

            save(futures, cachedOutput, "entity_interact_cow",
                    AbilityBuilder.ability("Milking")
                            .parent("crafting_iron_tools")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:cow", "minecraft:goat"));

            save(futures, cachedOutput, "entity_interact_boat",
                    AbilityBuilder.ability("Sailing")
                            .parent("crouch")
                            .type(AbilityTypes.ENTITY_INTERACT)
                            .targets("minecraft:boat"));

            //Item Locks
            save(futures, cachedOutput, "item_interact_bow",
                    AbilityBuilder.ability("Using Bows")
                            .parent("item_interact_food")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:bow"));

            save(futures, cachedOutput, "item_interact_food",
                    AbilityBuilder.ability("Eating Food")
                            .parent("passive_mob_damage")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targetTag(Tags.Items.FOODS.location()));

            save(futures, cachedOutput, "item_interact_shield",
                    AbilityBuilder.ability("Using Shields")
                            .parent("crafting_iron_tools")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:shield"));

            save(futures, cachedOutput, "item_interact_crossbow",
                    AbilityBuilder.ability("Using Crossbows")
                            .parent("item_interact_bow")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:crossbow"));

            save(futures, cachedOutput, "item_interact_fishing_rod",
                    AbilityBuilder.ability("Using Fishing Rods")
                            .parent("item_interact_food")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:fishing_rod"));

            save(futures, cachedOutput, "item_interact_ender_pearl",
                    AbilityBuilder.ability("Using Ender Pearls")
                            .parent("hostile_overworld_mob_damage")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:ender_pearl"));

            save(futures, cachedOutput, "item_interact_eye_of_ender",
                    AbilityBuilder.ability("Using Eye of Enders")
                            .parent("item_interact_ender_pearl")
                            .type(AbilityTypes.ITEM_INTERACT)
                            .targets("minecraft:ender_eye"));

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void extracted(CachedOutput cachedOutput, List<CompletableFuture<?>> futures) {
        save(futures, cachedOutput, "sprint",
                AbilityBuilder.ability("Sprint")
                        .parent("half_sprint")
                        .type(AbilityTypes.MOVEMENT_ABILITY)
                        .targets("sprint"));
    }


    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, AbilityBuilder builder) {
        Identifier id = AbilityLock.identifier(name);
        Path filePath = pathProvider.json(id);
        AbilityData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, AbilityData.CODEC, data, filePath));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Abilities"; }
}