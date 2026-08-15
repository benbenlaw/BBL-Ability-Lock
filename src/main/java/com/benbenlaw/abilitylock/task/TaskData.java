package com.benbenlaw.abilitylock.task;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record TaskData(
        String displayName,
        List<Identifier> parents,
        Optional<Identifier> type,
        List<Identifier> requiredAbilities,
        List<String> targets,
        int target
) {

    public static final Codec<TaskData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(TaskData::displayName),
            Identifier.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(TaskData::parents),
            Identifier.CODEC.optionalFieldOf("type").forGetter(TaskData::type),
            Identifier.CODEC.listOf().optionalFieldOf("required_abilities", List.of()).forGetter(TaskData::requiredAbilities),
            Codec.STRING.listOf().optionalFieldOf("targets", List.of()).forGetter(TaskData::targets),
            Codec.INT.optionalFieldOf("target", 1).forGetter(TaskData::target)
    ).apply(instance, TaskData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaskData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TaskData::displayName,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), TaskData::parents,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), TaskData::type,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), TaskData::requiredAbilities,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), TaskData::targets,
            ByteBufCodecs.INT, TaskData::target,
            TaskData::new
    );
}