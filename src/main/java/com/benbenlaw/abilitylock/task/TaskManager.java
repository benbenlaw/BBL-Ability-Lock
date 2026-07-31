package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityRegistry;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.util.SpeedrunManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class TaskManager {

    public static void handle(TaskEvent event) {
        ServerPlayer player = event.player();
        TaskProgressData progressData = ensurePoolChoicesResolved(player);

        for (Task task : TaskRegistry.all().values()) {
            if (!isActiveForPlayer(task.id(), progressData)) continue;
            if (progressData.isComplete(task.id())) continue;
            if (!isUnlockedForPlayer(task.id(), progressData)) continue;

            int amount = task.criterion().amountFor(event);
            if (amount <= 0) continue;

            progressData = progressData.withProgress(task.id(), amount, task.criterion().target());

            if (progressData.progressOf(task.id()) >= task.criterion().target()) {
                progressData = progressData.withCompleted(task.id());
                onTaskCompleted(player, task);
            }
        }

        player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(progressData));
    }

    public static void checkInventoryTasks(ServerPlayer player) {
        TaskProgressData progressData = ensurePoolChoicesResolved(player);
        boolean changed = false;

        for (Task task : TaskRegistry.all().values()) {
            if (!isActiveForPlayer(task.id(), progressData)) continue;
            if (progressData.isComplete(task.id())) continue;
            if (!isUnlockedForPlayer(task.id(), progressData)) continue;

            int inventoryCount = task.criterion().countInInventory(player);
            if (inventoryCount < 0) continue;

            TaskProgressData before = progressData;
            progressData = progressData.withProgressSet(task.id(), inventoryCount, task.criterion().target());

            if (progressData.progressOf(task.id()) >= task.criterion().target()) {
                progressData = progressData.withCompleted(task.id());
                onTaskCompleted(player, task);
            }

            if (progressData != before) changed = true;
        }

        if (changed) {
            player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
            PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(progressData));
        }
    }

    public static boolean isActiveForPlayer(String taskId, TaskProgressData data) {
        Optional<TaskPool> pool = TaskPoolRegistry.poolContaining(taskId);
        if (pool.isEmpty()) return true;

        return taskId.equals(data.poolChoice(pool.get().id()).orElse(null));
    }

    private static TaskProgressData ensurePoolChoicesResolved(ServerPlayer player) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        boolean changed = false;

        for (TaskPool pool : TaskPoolRegistry.all().values()) {
            if (data.poolChoice(pool.id()).isPresent()) continue;

            List<String> options = pool.taskIds();
            String chosen = options.get(player.getRandom().nextInt(options.size()));
            data = data.withPoolChoice(pool.id(), chosen);
            changed = true;
        }

        if (changed) player.setData(AbilityLockAttachments.TASK_PROGRESS, data);
        return data;
    }

    private static void onTaskCompleted(ServerPlayer player, Task task) {
        AbilityChecker.grantRandomEligible(player)
                .ifPresent(a -> player.sendSystemMessage(Component.literal("Locked ability unlocked: " + a.displayName())));

        Optional<TaskPool> pool = TaskPoolRegistry.get("final_kill");

        if (pool.isPresent()) {
            if (pool.get().contains(task.id())) {
                SpeedrunManager.onSpeedrunFinished(player, task);
            }
        }
    }

    public static boolean isUnlockedForPlayer(String taskId, TaskProgressData data) {
        Optional<Task> task = TaskRegistry.get(taskId);
        if (task.isEmpty()) return false;

        Task t = task.get();
        if (!t.hasParent()) return true;

        return data.isComplete(t.parent()) && isUnlockedForPlayer(t.parent(), data);
    }

    public static void forceComplete(ServerPlayer player, Task task) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (progressData.isComplete(task.id())) return;

        progressData = progressData.withCompleted(task.id());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
        onTaskCompleted(player, task);

        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(player.getData(AbilityLockAttachments.TASK_PROGRESS)));
    }

    public static void resetAll(ServerPlayer player) {
        TaskProgressData blank = new TaskProgressData(new HashMap<>(), new HashSet<>(), new HashMap<>());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, blank);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(blank));
    }

    public static void resetOne(ServerPlayer player, String taskId) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        Map<String, Integer> newProgress = new HashMap<>(data.progress());
        newProgress.remove(taskId);

        Set<String> newCompleted = new HashSet<>(data.completed());
        newCompleted.remove(taskId);

        Map<String, String> newPoolChoices = new HashMap<>(data.poolChoices());
        newPoolChoices.values().removeIf(chosen -> chosen.equals(taskId));

        TaskProgressData updated = new TaskProgressData(newProgress, newCompleted, newPoolChoices);
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }
}