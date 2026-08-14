package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.config.ServerConfig;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.util.SpeedrunManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class TaskManager {

    public static void handle(TaskEvent event) {
        ServerPlayer player = event.player();
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        Task finishingTask = null;

        for (Task task : TaskRegistry.all().values()) {
            if (!progressData.isInGrid(task.id())) continue;
            if (progressData.isComplete(task.id())) continue;
            if (!canAttempt(player, task)) continue;

            int amount = task.criterion().amountFor(event);
            if (amount <= 0) continue;

            progressData = progressData.withProgress(task.id(), amount, task.criterion().target());

            if (progressData.progressOf(task.id()) >= task.criterion().target()) {
                progressData = progressData.withCompleted(task.id());
                onTaskCompleted(player, task);
                if (finishingTask == null && progressData.isGridComplete()) {
                    finishingTask = task;
                }
            }
        }

        player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(progressData));

        if (finishingTask != null) {
            SpeedrunManager.onSpeedrunFinished(player, finishingTask);
        }
    }

    public static void checkInventoryTasks(ServerPlayer player) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        boolean changed = false;
        Task finishingTask = null;

        for (Task task : TaskRegistry.all().values()) {
            if (!progressData.isInGrid(task.id())) continue;
            if (progressData.isComplete(task.id())) continue;
            if (!canAttempt(player, task)) continue;

            int inventoryCount = task.criterion().countInInventory(player);
            if (inventoryCount < 0) continue;

            TaskProgressData before = progressData;
            progressData = progressData.withProgressSet(task.id(), inventoryCount, task.criterion().target());

            if (progressData.progressOf(task.id()) >= task.criterion().target()) {
                progressData = progressData.withCompleted(task.id());
                onTaskCompleted(player, task);
                if (finishingTask == null && progressData.isGridComplete()) {
                    finishingTask = task;
                }
            }

            if (progressData != before) changed = true;
        }

        if (changed) {
            player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
            PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(progressData));
        }

        if (finishingTask != null) {
            SpeedrunManager.onSpeedrunFinished(player, finishingTask);
        }
    }

    private static boolean canAttempt(ServerPlayer player, Task task) {
        if (!task.hasRequiredAbilities()) return true;
        for (Identifier abilityId : task.requiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }
        return true;
    }

    public static void ensureGridAssigned(ServerPlayer player, int gridSize, Set<Identifier> startingAbilities) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (!data.gridTaskIds().isEmpty()) return;

        List<String> grid = buildSolvableGrid(gridSize * gridSize, startingAbilities, player.getRandom());

        TaskProgressData updated = data.withGridTaskIds(grid);
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }

    private static List<String> buildSolvableGrid(int targetCount, Set<Identifier> startingAbilities, RandomSource random) {
        Set<Identifier> unlocked = new HashSet<>(startingAbilities);
        Random rng = new Random(random.nextLong());

        List<Task> allTasks = new ArrayList<>(TaskRegistry.all().values());
        Collections.shuffle(allTasks, rng);

        List<Task> immediatePool = new ArrayList<>();
        List<Task> gatedPool = new ArrayList<>();
        for (Task task : allTasks) {
            if (startingAbilities.containsAll(task.requiredAbilities())) {
                immediatePool.add(task);
            } else {
                gatedPool.add(task);
            }
        }

        List<String> selected = new ArrayList<>();
        int immediatePercent = ServerConfig.immediateTaskPercentage.get();
        int immediateTarget = Math.max(0, Math.min(targetCount, Math.round(targetCount * (immediatePercent / 100f))));

        Iterator<Task> immediateIter = immediatePool.iterator();
        while (selected.size() < immediateTarget && immediateIter.hasNext()) {
            Task task = immediateIter.next();
            immediateIter.remove();
            selected.add(task.id());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        while (selected.size() < targetCount) {
            Task next = pickReachable(gatedPool, unlocked);
            if (next == null) next = pickReachable(immediatePool, unlocked);
            if (next == null) break;

            gatedPool.remove(next);
            immediatePool.remove(next);
            selected.add(next.id());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        return selected;
    }

    private static @Nullable Task pickReachable(List<Task> pool, Set<Identifier> unlocked) {
        for (Task candidate : pool) {
            if (unlocked.containsAll(candidate.requiredAbilities())) {
                return candidate;
            }
        }
        return null;
    }

    private static void simulateAbilityGrant(Set<Identifier> unlocked, List<Task> pendingA, List<Task> pendingB, Random rng) {
        List<Identifier> eligible = new ArrayList<>();
        for (Identifier abilityId : AbilityLoader.DATA.keySet()) {
            if (unlocked.contains(abilityId)) continue;

            AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
            if (!unlocked.containsAll(abilityData.parents())) continue;

            eligible.add(abilityId);
        }
        if (eligible.isEmpty()) return;

        Set<Identifier> relevant = new HashSet<>();
        for (Task task : pendingA) {
            for (Identifier abilityId : task.requiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }
        for (Task task : pendingB) {
            for (Identifier abilityId : task.requiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }

        List<Identifier> preferred = new ArrayList<>();
        for (Identifier abilityId : eligible) {
            if (relevant.contains(abilityId)) preferred.add(abilityId);
        }

        List<Identifier> pool = preferred.isEmpty() ? eligible : preferred;
        unlocked.add(pool.get(rng.nextInt(pool.size())));
    }

    private static void onTaskCompleted(ServerPlayer player, Task task) {
        AbilityChecker.grantRandomEligible(player).ifPresent(id -> {
            AbilityData data = AbilityLoader.DATA.get(id);
            String name = data != null ? data.displayName() : id.toString();
            player.sendSystemMessage(Component.literal("Locked ability unlocked: " + name));
        });
    }

    public static void forceComplete(ServerPlayer player, Task task) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (progressData.isComplete(task.id())) return;

        progressData = progressData.withCompleted(task.id());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
        onTaskCompleted(player, task);

        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(progressData));

        if (progressData.isGridComplete()) {
            SpeedrunManager.onSpeedrunFinished(player, task);
        }
    }

    public static void resetAll(ServerPlayer player) {
        TaskProgressData current = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        TaskProgressData blank = new TaskProgressData(new HashMap<>(), new HashSet<>(), current.gridTaskIds());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, blank);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(blank));
    }

    public static void resetOne(ServerPlayer player, String taskId) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        Map<String, Integer> newProgress = new HashMap<>(data.progress());
        newProgress.remove(taskId);

        Set<String> newCompleted = new HashSet<>(data.completed());
        newCompleted.remove(taskId);

        TaskProgressData updated = new TaskProgressData(newProgress, newCompleted, data.gridTaskIds());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }
}