package com.benbenlaw.abilitylock.presets;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class PresetLoader extends SimpleJsonResourceReloadListener<PresetData> {

    public static final Map<Identifier, PresetData> DATA = new HashMap<>();

    public PresetLoader() {
        super(PresetData.CODEC, FileToIdConverter.json("preset"));
    }

    @Override
    protected void apply(Map<Identifier, PresetData> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        DATA.putAll(prepared);
        System.out.println("Loaded " + DATA.size() + " AbilityLock world presets");
    }
}