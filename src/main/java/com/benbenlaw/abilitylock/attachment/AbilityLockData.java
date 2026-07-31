package com.benbenlaw.abilitylock.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public record AbilityLockData(Set<String> unlockedAbilities) {

    public static final Codec<AbilityLockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf()
                    .xmap((List<String> list) -> (Set<String>) new HashSet<>(list), (Set<String> set) -> new ArrayList<>(set))
                    .fieldOf("unlocked")
                    .forGetter(AbilityLockData::unlockedAbilities)
    ).apply(instance, AbilityLockData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityLockData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).cast(),
            AbilityLockData::toList,
            AbilityLockData::fromList
    );

    private static List<String> toList(AbilityLockData data) {
        return new ArrayList<>(data.unlockedAbilities());
    }

    private static AbilityLockData fromList(List<String> list) {
        return new AbilityLockData(new HashSet<>(list));
    }

    public AbilityLockData(List<String> list) {
        this(new HashSet<>(list));
    }

    public boolean has(String ability) {
        return unlockedAbilities.contains(ability);
    }

    public AbilityLockData withUnlocked(String ability) {
        if (unlockedAbilities.contains(ability)) return this;
        Set<String> copy = new HashSet<>(unlockedAbilities);
        copy.add(ability);
        return new AbilityLockData(copy);
    }
}