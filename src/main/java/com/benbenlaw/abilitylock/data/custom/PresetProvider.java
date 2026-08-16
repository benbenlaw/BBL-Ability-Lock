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
                    .startingAbilities("forward")
                    .bonusAbilityPercentage(15));

            save(futures, cachedOutput, "hardcore", PresetBuilder
                    .preset("Hardcore")
                    .startingAbilities("forward")
                    .unlockableAbilities("backward", "left", "right")
                    .onDeathLoseWorld(true)
                    .defaultGridSize(5)
                    .immediateTaskPercentage(15)
                    .bonusAbilityPercentage(10));


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