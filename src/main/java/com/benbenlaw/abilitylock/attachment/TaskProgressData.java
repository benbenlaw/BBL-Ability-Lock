package com.benbenlaw.abilitylock.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public record TaskProgressData(Map<String, Integer> progress, Set<String> completed, Map<String, String> poolChoices) {

    public static final Codec<TaskProgressData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("progress").forGetter(TaskProgressData::progress),
            Codec.STRING.listOf()
                    .xmap((List<String> list) -> (Set<String>) new HashSet<>(list), (Set<String> set) -> new ArrayList<>(set))
                    .fieldOf("completed")
                    .forGetter(TaskProgressData::completed),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("poolChoices").forGetter(TaskProgressData::poolChoices)
    ).apply(instance, TaskProgressData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaskProgressData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT),
            TaskProgressData::progress,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).cast(),
            TaskProgressData::completedList,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            TaskProgressData::poolChoices,
            TaskProgressData::fromParts
    );

    private static List<String> completedList(TaskProgressData data) {
        return new ArrayList<>(data.completed());
    }

    private static TaskProgressData fromParts(Map<String, Integer> progress, List<String> completedList, Map<String, String> poolChoices) {
        return new TaskProgressData(new HashMap<>(progress), new HashSet<>(completedList), new HashMap<>(poolChoices));
    }

    public int progressOf(String taskId) {
        return progress.getOrDefault(taskId, 0);
    }

    public boolean isComplete(String taskId) {
        return completed.contains(taskId);
    }

    public Optional<String> poolChoice(String poolId) {
        return Optional.ofNullable(poolChoices.get(poolId));
    }

    public TaskProgressData withProgress(String taskId, int amount, int target) {
        int current = progressOf(taskId);
        int updated = Math.min(current + amount, target);
        if (updated == current) return this;

        Map<String, Integer> newProgress = new HashMap<>(progress);
        newProgress.put(taskId, updated);
        return new TaskProgressData(newProgress, completed, poolChoices);
    }

    public TaskProgressData withProgressSet(String taskId, int value, int target) {
        int capped = Math.min(Math.max(value, 0), target);
        if (capped == progressOf(taskId)) return this;

        Map<String, Integer> newProgress = new HashMap<>(progress);
        newProgress.put(taskId, capped);
        return new TaskProgressData(newProgress, completed, poolChoices);
    }

    public TaskProgressData withCompleted(String taskId) {
        if (completed.contains(taskId)) return this;
        Set<String> newCompleted = new HashSet<>(completed);
        newCompleted.add(taskId);
        return new TaskProgressData(progress, newCompleted, poolChoices);
    }

    public TaskProgressData withPoolChoice(String poolId, String taskId) {
        if (taskId.equals(poolChoices.get(poolId))) return this;
        Map<String, String> newChoices = new HashMap<>(poolChoices);
        newChoices.put(poolId, taskId);
        return new TaskProgressData(progress, completed, newChoices);
    }
}