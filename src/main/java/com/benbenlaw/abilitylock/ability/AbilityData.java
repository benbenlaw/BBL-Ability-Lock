package com.benbenlaw.abilitylock.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record AbilityData(
        String displayName,
        List<Identifier> parents,
        Optional<Identifier> type,
        List<String> targets
) {

    public static final Codec<AbilityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(AbilityData::displayName),
            Identifier.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(AbilityData::parents),
            Identifier.CODEC.optionalFieldOf("type").forGetter(AbilityData::type),
            Codec.STRING.listOf().optionalFieldOf("targets", List.of()).forGetter(AbilityData::targets)
    ).apply(instance, AbilityData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AbilityData::displayName,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), AbilityData::parents,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), AbilityData::type,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AbilityData::targets,
            AbilityData::new
    );
}