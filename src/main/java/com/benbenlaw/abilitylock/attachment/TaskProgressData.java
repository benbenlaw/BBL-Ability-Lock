package com.benbenlaw.abilitylock.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.*;

public record TaskProgressData(Map<Identifier, Integer> progress, Set<Identifier> completed, List<Identifier> gridTaskIds, int gridWidth) {

    public static final Codec<TaskProgressData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.INT).fieldOf("progress").forGetter(TaskProgressData::progress),
            Identifier.CODEC.listOf()
                    .xmap((List<Identifier> list) -> (Set<Identifier>) new HashSet<>(list), (Set<Identifier> set) -> new ArrayList<>(set))
                    .fieldOf("completed")
                    .forGetter(TaskProgressData::completed),
            Identifier.CODEC.listOf().fieldOf("gridTaskIds").forGetter(TaskProgressData::gridTaskIds),
            Codec.INT.optionalFieldOf("gridWidth", 0).forGetter(TaskProgressData::gridWidth)
    ).apply(instance, TaskProgressData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaskProgressData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.INT),
            TaskProgressData::progress,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).cast(),
            TaskProgressData::completedList,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).cast(),
            TaskProgressData::gridTaskIds,
            ByteBufCodecs.INT,
            TaskProgressData::gridWidth,
            TaskProgressData::fromParts
    );

    private static List<Identifier> completedList(TaskProgressData data) {
        return new ArrayList<>(data.completed());
    }

    private static TaskProgressData fromParts(Map<Identifier, Integer> progress, List<Identifier> completedList, List<Identifier> gridTaskIds, int gridWidth) {
        return new TaskProgressData(new HashMap<>(progress), new HashSet<>(completedList), new ArrayList<>(gridTaskIds), gridWidth);
    }

    public TaskProgressData(Map<Identifier, Integer> progress, Set<Identifier> completed, List<Identifier> gridTaskIds) {
        this(progress, completed, gridTaskIds, 0);
    }

    public int progressOf(Identifier taskId) {
        return progress.getOrDefault(taskId, 0);
    }

    public boolean isComplete(Identifier taskId) {
        return completed.contains(taskId);
    }

    public boolean isInGrid(Identifier taskId) {
        return gridTaskIds.contains(taskId);
    }

    public boolean isGridComplete() {
        return !gridTaskIds.isEmpty() && completed.containsAll(gridTaskIds);
    }

    public TaskProgressData withProgress(Identifier taskId, int amount, int target) {
        int current = progressOf(taskId);
        int updated = Math.min(current + amount, target);
        if (updated == current) return this;

        Map<Identifier, Integer> newProgress = new HashMap<>(progress);
        newProgress.put(taskId, updated);
        return new TaskProgressData(newProgress, completed, gridTaskIds, gridWidth);
    }

    public TaskProgressData withProgressSet(Identifier taskId, int value, int target) {
        int capped = Math.min(Math.max(value, 0), target);
        if (capped == progressOf(taskId)) return this;

        Map<Identifier, Integer> newProgress = new HashMap<>(progress);
        newProgress.put(taskId, capped);
        return new TaskProgressData(newProgress, completed, gridTaskIds, gridWidth);
    }

    public TaskProgressData withCompleted(Identifier taskId) {
        if (completed.contains(taskId)) return this;
        Set<Identifier> newCompleted = new HashSet<>(completed);
        newCompleted.add(taskId);
        return new TaskProgressData(progress, newCompleted, gridTaskIds, gridWidth);
    }

    public TaskProgressData withGrid(List<Identifier> newGridTaskIds, int newGridWidth) {
        return new TaskProgressData(progress, completed, new ArrayList<>(newGridTaskIds), newGridWidth);
    }
}