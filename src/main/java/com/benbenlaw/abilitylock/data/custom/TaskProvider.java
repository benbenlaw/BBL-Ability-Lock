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

    public static final String MINING = "mining";
    public static final String OBTAIN_ITEM = "obtain_item";

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
            save(futures, cachedOutput, MINING, "mine_dirt",
                    TaskBuilder.task("Mine Dirt")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matches("minecraft:grass_block")
                            .matchesTag(Identifier.parse("minecraft:dirt")));

            save(futures, cachedOutput, MINING, "mine_sand",
                    TaskBuilder.task("Mine Sand")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matchesTag(Identifier.parse("c:sands")));

            save(futures, cachedOutput, MINING, "mine_gravel",
                    TaskBuilder.task("Mine Gravel")
                            .type(TaskTypes.BLOCK_BREAK)
                            .target(8)
                            .matchesTag(Identifier.parse("c:gravels")));

            save(futures, cachedOutput, MINING, "mine_stone",
                 TaskBuilder.task("Mine Stone")
                         .type(TaskTypes.BLOCK_BREAK)
                         .requiredAbility(path(AbilityProvider.BREAKING, "break_stone"))
                         .parent(path(MINING, "mine_dirt"))
                         .target(8)
                         .matchesTag(Identifier.parse("c:stones")));


            //Obtain Item
            save(futures, cachedOutput, MINING, "obtain_egg",
                    TaskBuilder.task("Obtain Chicken Egg")
                            .type(TaskTypes.ITEM_OBTAIN)
                            .target(1)
                            .matchesTag(Identifier.parse("minecraft:eggs")));


            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private static String path(String folder, String name) {
        return folder + "/" + name;
    }

    private void save(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String folder, String name, TaskBuilder builder) {
        Identifier id = AbilityLock.identifier(path(folder, name));
        Path filePath = pathProvider.json(id);
        TaskData data = builder.build();
        futures.add(DataProvider.saveStable(cachedOutput, TaskData.CODEC, data, filePath));
    }

    @Override
    public String getName() { return AbilityLock.MOD_ID + " Tasks"; }
}