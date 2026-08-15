package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
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

        TaskType finishingTask = null;

        for (TaskType task : TaskLoader.TASKS.values()) {
            Identifier taskId = task.getId();
            if (!progressData.isInGrid(taskId)) continue;
            if (progressData.isComplete(taskId)) continue;
            if (!canAttempt(player, task)) continue;

            int amount = task.amountFor(event);
            if (amount <= 0) continue;

            progressData = progressData.withProgress(taskId, amount, task.getTarget());

            if (progressData.progressOf(taskId) >= task.getTarget()) {
                progressData = progressData.withCompleted(taskId);
                onTaskCompleted(player, task, progressData);
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
        TaskType finishingTask = null;

        for (TaskType task : TaskLoader.TASKS.values()) {
            Identifier taskId = task.getId();
            if (!progressData.isInGrid(taskId)) continue;
            if (progressData.isComplete(taskId)) continue;
            if (!canAttempt(player, task)) continue;

            int inventoryCount = task.countInInventory(player);
            if (inventoryCount < 0) continue;

            TaskProgressData before = progressData;
            progressData = progressData.withProgressSet(taskId, inventoryCount, task.getTarget());

            if (progressData.progressOf(taskId) >= task.getTarget()) {
                progressData = progressData.withCompleted(taskId);
                onTaskCompleted(player, task, progressData);
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

    private static boolean canAttempt(ServerPlayer player, TaskType task) {
        if (!task.hasRequiredAbilities()) return true;
        for (Identifier abilityId : task.getRequiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }
        return true;
    }

    public static void ensureGridAssigned(ServerPlayer player, int gridSize, Set<Identifier> startingAbilities) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (!data.gridTaskIds().isEmpty()) return;

        List<Identifier> grid = buildSolvableGrid(gridSize * gridSize, startingAbilities, player.getRandom());

        TaskProgressData updated = data.withGridTaskIds(grid);
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }

    private static List<Identifier> buildSolvableGrid(int targetCount, Set<Identifier> startingAbilities, RandomSource random) {
        Set<Identifier> unlocked = new HashSet<>(startingAbilities);
        Random rng = new Random(random.nextLong());

        List<TaskType> allTasks = new ArrayList<>(TaskLoader.TASKS.values());
        Collections.shuffle(allTasks, rng);

        List<TaskType> immediatePool = new ArrayList<>();
        List<TaskType> gatedPool = new ArrayList<>();
        for (TaskType task : allTasks) {
            if (startingAbilities.containsAll(task.getRequiredAbilities())) {
                immediatePool.add(task);
            } else {
                gatedPool.add(task);
            }
        }

        List<Identifier> selected = new ArrayList<>();
        int immediatePercent = ServerConfig.immediateTaskPercentage.get();
        int immediateTarget = Math.max(0, Math.min(targetCount, Math.round(targetCount * (immediatePercent / 100f))));

        Iterator<TaskType> immediateIter = immediatePool.iterator();
        while (selected.size() < immediateTarget && immediateIter.hasNext()) {
            TaskType task = immediateIter.next();
            immediateIter.remove();
            selected.add(task.getId());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        while (selected.size() < targetCount) {
            TaskType next = pickReachable(gatedPool, unlocked);
            if (next == null) next = pickReachable(immediatePool, unlocked);
            if (next == null) break;

            gatedPool.remove(next);
            immediatePool.remove(next);
            selected.add(next.getId());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        return selected;
    }

    private static @Nullable TaskType pickReachable(List<TaskType> pool, Set<Identifier> unlocked) {
        for (TaskType candidate : pool) {
            if (unlocked.containsAll(candidate.getRequiredAbilities())) {
                return candidate;
            }
        }
        return null;
    }

    private static void simulateAbilityGrant(Set<Identifier> unlocked, List<TaskType> pendingA, List<TaskType> pendingB, Random rng) {
        List<Identifier> eligible = new ArrayList<>();
        for (Identifier abilityId : AbilityLoader.DATA.keySet()) {
            if (unlocked.contains(abilityId)) continue;

            AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
            if (!unlocked.containsAll(abilityData.parents())) continue;

            eligible.add(abilityId);
        }
        if (eligible.isEmpty()) return;

        Set<Identifier> relevant = new HashSet<>();
        for (TaskType task : pendingA) {
            for (Identifier abilityId : task.getRequiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }
        for (TaskType task : pendingB) {
            for (Identifier abilityId : task.getRequiredAbilities()) {
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

    private static void onTaskCompleted(ServerPlayer player, TaskType task, TaskProgressData progressData) {
        grantGridAwareAbility(player, progressData).ifPresent(id -> {
            AbilityData data = AbilityLoader.DATA.get(id);
            String name = data != null ? data.displayName() : id.toString();
            player.sendSystemMessage(Component.literal("Locked ability unlocked: " + name));
        });
    }

    private static Optional<Identifier> grantGridAwareAbility(ServerPlayer player, TaskProgressData progressData) {
        AbilityLockData abilityLockData = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Identifier> eligible = AbilityChecker.getEligibleAbilities(abilityLockData);
        if (eligible.isEmpty()) return Optional.empty();

        Set<Identifier> relevant = new HashSet<>();
        for (Identifier taskId : progressData.gridTaskIds()) {
            if (progressData.isComplete(taskId)) continue;

            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task == null) continue;

            for (Identifier abilityId : task.getRequiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }

        List<Identifier> preferred = new ArrayList<>();
        for (Identifier abilityId : eligible) {
            if (relevant.contains(abilityId)) preferred.add(abilityId);
        }

        List<Identifier> pool = preferred.isEmpty() ? eligible : preferred;
        Identifier chosen = pool.get(player.getRandom().nextInt(pool.size()));

        AbilityChecker.grantSpecific(player, chosen);
        return Optional.of(chosen);
    }

    public static void forceComplete(ServerPlayer player, TaskType task) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (progressData.isComplete(task.getId())) return;

        progressData = progressData.withCompleted(task.getId());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, progressData);
        onTaskCompleted(player, task, progressData);

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

    public static void resetOne(ServerPlayer player, Identifier taskId) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        Map<Identifier, Integer> newProgress = new HashMap<>(data.progress());
        newProgress.remove(taskId);

        Set<Identifier> newCompleted = new HashSet<>(data.completed());
        newCompleted.remove(taskId);

        TaskProgressData updated = new TaskProgressData(newProgress, newCompleted, data.gridTaskIds());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }
}