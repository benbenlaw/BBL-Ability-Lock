package com.benbenlaw.abilitylock.data;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.data.custom.AbilityProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class ALDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new ALLangProvider(packOutput));

        //Custom
        generator.addProvider(true, new AbilityProvider(packOutput, lookupProvider));

    }
}
