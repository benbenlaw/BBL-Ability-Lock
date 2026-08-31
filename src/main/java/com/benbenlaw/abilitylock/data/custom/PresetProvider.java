package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.presets.PresetData;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PresetProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public PresetProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "preset");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.lookupProvider.thenCompose(registries -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();

            save(futures, cachedOutput, "standard", PresetBuilder
                    .preset("Standard")
                    .startingAbilities("forward", "backward", "climb", "half_sprint", "crouch")
                    .immediateTaskPercentage(5)
                    .bonusAbilityPercentage(15));

            save(futures, cachedOutput, "hardcore", PresetBuilder
                    .preset("Hardcore")
                    .startingAbilities("forward", "backward", "climb", "half_sprint")
                    .onDeathLoseWorld(true)
                    .defaultGridSize(7)
                    .immediateTaskPercentage(5)
                    .bonusAbilityPercentage(5));

            //Season 1
            save(futures, cachedOutput, "season1", PresetBuilder
                    .preset("Season 1")
                    .startingAbilities("forward")
                    .onDeathLoseWorld(true)
                    .defaultGridSize(5)
                    .immediateTaskPercentage(15)
                    .bonusAbilityPercentage(25)
                    .validTasks(
                            "mine_dirt",
                            "mine_sand",
                            "mine_gravel",
                            "mine_stone",
                            "kill_zombie",
                            "kill_skeleton",
                            "kill_creeper",
                            "kill_spider",
                            "kill_pig",
                            "kill_chicken",
                            "kill_cow",
                            "obtain_furnace",
                            "obtain_iron_pickaxe",
                            "obtain_copper_pickaxe",
                            "obtain_stone_pickaxe",
                            "obtain_wooden_pickaxe",
                            "obtain_redstone_block",
                            "obtain_lapis_block",
                            "obtain_granite",
                            "obtain_andesite",
                            "obtain_diorite",
                            "obtain_saddle",
                            "obtain_logs",
                            "obtain_crafting_table",
                            "obtain_cobblestone"
                    )
            );

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, PresetBuilder builder) {
        Identifier id = AbilityLock.identifier(name);
        Path path = pathProvider.json(id);
        PresetData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, PresetData.CODEC, data, path));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Presets"; }
}