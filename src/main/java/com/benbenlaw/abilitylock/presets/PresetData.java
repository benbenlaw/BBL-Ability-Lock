package com.benbenlaw.abilitylock.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
        int bonusAbilityPercentage
) {

    public static final Codec<PresetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(PresetData::displayName),
            Identifier.CODEC.listOf().optionalFieldOf("starting_abilities", List.of()).forGetter(PresetData::startingAbilities),
            Codec.BOOL.optionalFieldOf("on_death_lose_world", false).forGetter(PresetData::onDeathLoseWorld),
            Identifier.CODEC.listOf().optionalFieldOf("unlockable_abilities", List.of()).forGetter(PresetData::unlockableAbilities),
            Codec.INT.optionalFieldOf("default_grid_size").forGetter(PresetData::defaultGridSize),
            Codec.intRange(0, 100).optionalFieldOf("immediate_task_percentage").forGetter(PresetData::immediateTaskPercentage),
            Codec.intRange(0, 100).optionalFieldOf("bonus_ability_percentage", 0).forGetter(PresetData::bonusAbilityPercentage)
    ).apply(instance, PresetData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PresetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PresetData::displayName,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), PresetData::startingAbilities,
            ByteBufCodecs.BOOL, PresetData::onDeathLoseWorld,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), PresetData::unlockableAbilities,
            ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PresetData::defaultGridSize,
            ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PresetData::immediateTaskPercentage,
            ByteBufCodecs.INT, PresetData::bonusAbilityPercentage,
            PresetData::new
    );

    public boolean restrictsUnlockable() {
        return !unlockableAbilities.isEmpty();
    }
}