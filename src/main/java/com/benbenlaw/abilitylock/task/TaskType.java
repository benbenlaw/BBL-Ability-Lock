package com.benbenlaw.abilitylock.task;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public abstract class TaskType {

    private Identifier id;
    private TaskData data;

    public abstract int amountFor(TaskEvent event);

    public int countInInventory(ServerPlayer player) {
        return -1;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public void setData(TaskData data) {
        this.data = data;
    }

    public TaskData getData() {
        return data;
    }

    public Component getDisplayName() {
        return data != null ? Component.translatable(data.displayName()) : Component.literal(id.toString());
    }

    public List<Identifier> getParents() {
        return data != null ? data.parents() : List.of();
    }

    public List<Identifier> getRequiredAbilities() {
        return data != null ? data.requiredAbilities() : List.of();
    }

    public boolean hasRequiredAbilities() {
        return !getRequiredAbilities().isEmpty();
    }

    public List<String> getTargets() {
        return data != null ? data.targets() : List.of();
    }

    public int getTarget() {
        return data != null ? data.target() : 1;
    }
}