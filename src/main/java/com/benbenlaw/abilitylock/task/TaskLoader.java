package com.benbenlaw.abilitylock.task;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TaskLoader extends SimpleJsonResourceReloadListener<TaskData> {

    public static final Map<Identifier, TaskData> DATA = new HashMap<>();
    public static final Map<Identifier, TaskType> TASKS = new HashMap<>();

    public TaskLoader() {
        super(TaskData.CODEC, FileToIdConverter.json("task"));
    }

    @Override
    protected void apply(Map<Identifier, TaskData> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        TASKS.clear();

        for (var entry : prepared.entrySet()) {
            Identifier id = entry.getKey();
            TaskData data = entry.getValue();
            DATA.put(id, data);

            data.type().flatMap(TaskTypes::create).ifPresent(task -> {
                task.setId(id);
                task.setData(data);
                TASKS.put(id, task);
            });
        }

        System.out.println("Loaded " + DATA.size() + " tasks (" + TASKS.size() + " with active enforcement)");
    }

    public static Set<Identifier> withAncestors(Identifier taskId) {
        Set<Identifier> result = new LinkedHashSet<>();
        collectAncestors(taskId, result);
        return result;
    }

    private static void collectAncestors(Identifier taskId, Set<Identifier> out) {
        if (!out.add(taskId)) return;

        TaskData data = DATA.get(taskId);
        if (data == null) return;

        for (Identifier parent : data.parents()) {
            collectAncestors(parent, out);
        }
    }
}