package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityRegistry;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.config.ServerConfig;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.util.SpeedrunManager;
import net.minecraft.network.chat.Component;
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

    /** Defensive check - the actual gating normally happens at the gameplay level (e.g. block-break mixins). */
    private static boolean canAttempt(ServerPlayer player, Task task) {
        if (!task.hasRequiredAbilities()) return true;
        for (String abilityId : task.requiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }
        return true;
    }

    /**
     * Builds and assigns the player's grid on first login. No-op if a grid
     * is already assigned. Unlike a pure random sample, this constructs the
     * grid in two phases:
     *
     *  1. ~25% of the grid is filled from tasks already completable with
     *     ONLY the starting abilities - so there's always immediate stuff
     *     to do.
     *  2. The rest is filled preferring tasks that are NOT yet reachable
     *     with the starting abilities (i.e. actually gated), only falling
     *     back to more unrestricted tasks if nothing gated is currently
     *     reachable. Each pick simulates the one ability grant that
     *     completing it would eventually trigger, using the exact same
     *     demand-aware policy AbilityChecker.grantRandomEligible uses for
     *     real - so this isn't just a best-case simulation. As long as
     *     construction finishes with a full grid, completing whatever's
     *     currently attemptable (in any order) will always finish it, since
     *     a demand-aware grant can never stall while the grid still needs
     *     something.
     */
    public static void ensureGridAssigned(ServerPlayer player, int gridSize, Set<String> startingAbilities) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (!data.gridTaskIds().isEmpty()) return;

        List<String> grid = buildSolvableGrid(gridSize * gridSize, startingAbilities, player.getRandom());

        TaskProgressData updated = data.withGridTaskIds(grid);
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }

    private static List<String> buildSolvableGrid(int targetCount, Set<String> startingAbilities, RandomSource random) {
        Set<String> unlocked = new HashSet<>(startingAbilities);
        Random rng = new Random(random.nextLong());

        List<Task> allTasks = new ArrayList<>(TaskRegistry.all().values());
        Collections.shuffle(allTasks, rng);

        // Split by reachability using ONLY the starting abilities - this
        // split is fixed up front and doesn't change as unlocked grows.
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

        // Phase 1: guaranteed "available straight away" slice.
        Iterator<Task> immediateIter = immediatePool.iterator();
        while (selected.size() < immediateTarget && immediateIter.hasNext()) {
            Task task = immediateIter.next();
            immediateIter.remove();
            selected.add(task.id());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        // Phase 2: fill the rest, preferring gated tasks as they become
        // reachable; fall back to remaining immediate tasks if nothing
        // gated is currently reachable, so we don't stall early.
        while (selected.size() < targetCount) {
            Task next = pickReachable(gatedPool, unlocked);
            if (next == null) next = pickReachable(immediatePool, unlocked);
            if (next == null) break; // nothing left is currently reachable - stop with a smaller grid rather than stall forever

            gatedPool.remove(next);
            immediatePool.remove(next);
            selected.add(next.id());
            simulateAbilityGrant(unlocked, gatedPool, immediatePool, rng);
        }

        return selected;
    }

    private static @Nullable Task pickReachable(List<Task> pool, Set<String> unlocked) {
        for (Task candidate : pool) {
            if (unlocked.containsAll(candidate.requiredAbilities())) {
                return candidate;
            }
        }
        return null;
    }

    /** Mirrors AbilityChecker.grantRandomEligible's demand-aware policy, against a hypothetical unlocked set instead of live player data. */
    private static void simulateAbilityGrant(Set<String> unlocked, List<Task> pendingA, List<Task> pendingB, Random rng) {
        List<Ability> eligible = new ArrayList<>();
        for (Ability ability : AbilityRegistry.all().values()) {
            if (unlocked.contains(ability.id())) continue;
            if (ability.hasParent() && !unlocked.contains(ability.parent())) continue;
            eligible.add(ability);
        }
        if (eligible.isEmpty()) return;

        Set<String> relevant = new HashSet<>();
        for (Task task : pendingA) {
            for (String abilityId : task.requiredAbilities()) {
                relevant.addAll(AbilityRegistry.withAncestors(abilityId));
            }
        }
        for (Task task : pendingB) {
            for (String abilityId : task.requiredAbilities()) {
                relevant.addAll(AbilityRegistry.withAncestors(abilityId));
            }
        }

        List<Ability> preferred = new ArrayList<>();
        for (Ability ability : eligible) {
            if (relevant.contains(ability.id())) preferred.add(ability);
        }

        List<Ability> pool = preferred.isEmpty() ? eligible : preferred;
        unlocked.add(pool.get(rng.nextInt(pool.size())).id());
    }

    private static void onTaskCompleted(ServerPlayer player, Task task) {
        AbilityChecker.grantRandomEligible(player)
                .ifPresent(a -> player.sendSystemMessage(Component.literal("Locked ability unlocked: " + a.displayName())));
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