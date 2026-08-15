package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.task.TaskData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskBuilder {

    private final String displayName;
    private final List<Identifier> parents = new ArrayList<>();
    private Identifier type;
    private final List<Identifier> requiredAbilities = new ArrayList<>();
    private final List<String> targets = new ArrayList<>();
    private int target = 1;

    private TaskBuilder(String displayName) {
        this.displayName = displayName;
    }

    public static TaskBuilder task(String displayName) {
        return new TaskBuilder(displayName);
    }

    public TaskBuilder parent(Identifier parent) {
        this.parents.add(parent);
        return this;
    }

    public TaskBuilder parent(String path) {
        return parent(AbilityLock.identifier(path));
    }

    public TaskBuilder type(Identifier type) {
        this.type = type;
        return this;
    }

    public TaskBuilder requiredAbility(Identifier abilityId) {
        this.requiredAbilities.add(abilityId);
        return this;
    }

    public TaskBuilder requiredAbility(String path) {
        return requiredAbility(AbilityLock.identifier(path));
    }

    public TaskBuilder requiredAbilities(Identifier... abilityIds) {
        for (Identifier id : abilityIds) requiredAbility(id);
        return this;
    }

    public TaskBuilder matches(String target) {
        this.targets.add(target);
        return this;
    }

    public TaskBuilder matches(String... targets) {
        for (String target : targets) matches(target);
        return this;
    }

    public TaskBuilder matchesTag(Identifier tagId) {
        return matches("#" + tagId);
    }

    public TaskBuilder target(int target) {
        this.target = target;
        return this;
    }

    public TaskData build() {
        return new TaskData(displayName, List.copyOf(parents), Optional.ofNullable(type),
                List.copyOf(requiredAbilities), List.copyOf(targets), target);
    }
}