package com.benbenlaw.abilitylock.presets;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.config.ClientConfig;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.*;

public class PresetLoader extends SimpleJsonResourceReloadListener<PresetData> {

    public static final Map<Identifier, PresetData> DATA = new LinkedHashMap<>();

    public PresetLoader() {
        super(PresetData.CODEC, FileToIdConverter.json("preset"));
    }

    @Override
    protected void apply(Map<Identifier, PresetData> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        DATA.putAll(prepared);

        if (!FMLEnvironment.isProduction()) {
            List<Identifier> abilities = new ArrayList<>();
            abilities.add(AbilityLock.identifier("forward"));
            abilities.add(AbilityLock.identifier("backward"));
            abilities.add(AbilityLock.identifier("jump"));

            List<Identifier> tasks = new ArrayList<>();
            tasks.add(AbilityLock.identifier("mine_dirt"));

            DATA.put(AbilityLock.identifier("wip"), new PresetData(
                    "DEV ONLY",
                    abilities,
                    false,
                    abilities,
                    Optional.of(2),
                    Optional.of(100),
                    100,
                    tasks
            ));
        }

        List<Identifier> ordered = new ArrayList<>(DATA.keySet());
        ordered.sort(Comparator.comparing(Identifier::toString));

        AbilityLock.LOGGER.info("Default preset from config = '{}', available keys = {}",
                ClientConfig.defaultPreset.get(), DATA.keySet());

        try {
            Identifier defaultId = Identifier.parse(ClientConfig.defaultPreset.get());
            if (ordered.remove(defaultId)) {
                ordered.addFirst(defaultId);
            }
        } catch (Exception e) {
            AbilityLock.LOGGER.error("Failed to apply default preset ordering", e);
        }

        Map<Identifier, PresetData> reordered = new LinkedHashMap<>();
        for (Identifier id : ordered) {
            reordered.put(id, DATA.get(id));
        }
        DATA.clear();
        DATA.putAll(reordered);

        System.out.println("Loaded " + DATA.size() + " AbilityLock world presets");
    }
}