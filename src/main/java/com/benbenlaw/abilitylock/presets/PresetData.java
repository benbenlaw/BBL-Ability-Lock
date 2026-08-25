package com.benbenlaw.abilitylock.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record PresetData(
        String displayName,
        List<Identifier> startingAbilities,
        boolean onDeathLoseWorld,
        List<Identifier> unlockableAbilities,
        Optional<Integer> defaultGridSize,
        Optional<Integer> immediateTaskPercentage,
        int bonusAbilityPercentage,
        List<Identifier> validTasks
) {

    public static final Codec<PresetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(PresetData::displayName),
            Identifier.CODEC.listOf().optionalFieldOf("starting_abilities", List.of()).forGetter(PresetData::startingAbilities),
            Codec.BOOL.optionalFieldOf("on_death_lose_world", false).forGetter(PresetData::onDeathLoseWorld),
            Identifier.CODEC.listOf().optionalFieldOf("unlockable_abilities", List.of()).forGetter(PresetData::unlockableAbilities),
            Codec.INT.optionalFieldOf("default_grid_size").forGetter(PresetData::defaultGridSize),
            Codec.intRange(0, 100).optionalFieldOf("immediate_task_percentage").forGetter(PresetData::immediateTaskPercentage),
            Codec.intRange(0, 100).optionalFieldOf("bonus_ability_percentage", 0).forGetter(PresetData::bonusAbilityPercentage),
            Identifier.CODEC.listOf().optionalFieldOf("valid_tasks", List.of()).forGetter(PresetData::validTasks)
    ).apply(instance, PresetData::new));

    public boolean restrictsUnlockable() {
        return !unlockableAbilities.isEmpty();
    }

    public boolean restrictsTasks() {
        return !validTasks.isEmpty();
    }
}