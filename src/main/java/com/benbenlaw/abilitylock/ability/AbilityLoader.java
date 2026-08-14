package com.benbenlaw.abilitylock.ability;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AbilityLoader extends SimpleJsonResourceReloadListener<AbilityData> {

    public static final Map<Identifier, AbilityData> DATA = new HashMap<>();
    public static final Map<Identifier, Ability> ABILITIES = new HashMap<>();

    public AbilityLoader() {
        super(AbilityData.CODEC, FileToIdConverter.json("ability"));
    }

    @Override
    protected void apply(Map<Identifier, AbilityData> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        ABILITIES.clear();

        for (var entry : prepared.entrySet()) {
            Identifier id = entry.getKey();
            AbilityData data = entry.getValue();
            DATA.put(id, data);

            data.type().flatMap(AbilityTypes::create).ifPresent(ability -> {
                ability.setId(id);
                ability.setData(data);
                ABILITIES.put(id, ability);
            });
        }

        System.out.println("Loaded " + DATA.size() + " abilities (" + ABILITIES.size() + " with active enforcement)");
    }

    public static Set<Identifier> withAncestors(Identifier abilityId) {
        Set<Identifier> result = new LinkedHashSet<>();
        collectAncestors(abilityId, result);
        return result;
    }

    private static void collectAncestors(Identifier abilityId, Set<Identifier> out) {
        if (!out.add(abilityId)) return;

        AbilityData data = DATA.get(abilityId);
        if (data == null) return;

        for (Identifier parent : data.parents()) {
            collectAncestors(parent, out);
        }
    }
}