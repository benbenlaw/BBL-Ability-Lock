package com.benbenlaw.abilitylock.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.*;

public record AbilityLockData(Set<Identifier> unlockedAbilities, Optional<Identifier> presetId, boolean eliminated, Optional<Identifier> lastGranted, int bonusPoints) {

    public static final Codec<AbilityLockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf()
                    .xmap((List<Identifier> list) -> (Set<Identifier>) new HashSet<>(list), (Set<Identifier> set) -> new ArrayList<>(set))
                    .fieldOf("unlocked")
                    .forGetter(AbilityLockData::unlockedAbilities),
            Identifier.CODEC.optionalFieldOf("preset").forGetter(AbilityLockData::presetId),
            Codec.BOOL.optionalFieldOf("eliminated", false).forGetter(AbilityLockData::eliminated),
            Identifier.CODEC.optionalFieldOf("last_granted").forGetter(AbilityLockData::lastGranted),
            Codec.INT.optionalFieldOf("bonus_points", 0).forGetter(AbilityLockData::bonusPoints)
    ).apply(instance, AbilityLockData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityLockData> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).cast(),
            AbilityLockData::toList,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional),
            AbilityLockData::presetId,
            ByteBufCodecs.BOOL,
            AbilityLockData::eliminated,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional),
            AbilityLockData::lastGranted,
            ByteBufCodecs.INT,
            AbilityLockData::bonusPoints,
            AbilityLockData::fromParts
    );

    private static List<Identifier> toList(AbilityLockData data) {
        return new ArrayList<>(data.unlockedAbilities());
    }

    private static AbilityLockData fromParts(List<Identifier> list, Optional<Identifier> presetId, boolean eliminated, Optional<Identifier> lastGranted, int bonusPoints) {
        return new AbilityLockData(new HashSet<>(list), presetId, eliminated, lastGranted, bonusPoints);
    }

    public AbilityLockData(Set<Identifier> unlockedAbilities) {
        this(unlockedAbilities, Optional.empty(), false, Optional.empty(), 0);
    }

    public AbilityLockData(Set<Identifier> unlockedAbilities, Optional<Identifier> presetId) {
        this(unlockedAbilities, presetId, false, Optional.empty(), 0);
    }

    public boolean has(Identifier ability) {
        return unlockedAbilities.contains(ability);
    }

    public AbilityLockData withUnlocked(Identifier ability) {
        if (unlockedAbilities.contains(ability)) return this;
        Set<Identifier> copy = new HashSet<>(unlockedAbilities);
        copy.add(ability);
        return new AbilityLockData(copy, presetId, eliminated, Optional.of(ability), bonusPoints);
    }

    public AbilityLockData withPreset(Identifier presetId) {
        return new AbilityLockData(unlockedAbilities, Optional.of(presetId), eliminated, lastGranted, bonusPoints);
    }

    public AbilityLockData withEliminated(boolean eliminated) {
        return new AbilityLockData(unlockedAbilities, presetId, eliminated, lastGranted, bonusPoints);
    }

    public AbilityLockData withBonusPoints(int bonusPoints) {
        return new AbilityLockData(unlockedAbilities, presetId, eliminated, lastGranted, bonusPoints);
    }
}