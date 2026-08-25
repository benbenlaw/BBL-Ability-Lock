package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.config.ServerConfig;
import com.benbenlaw.abilitylock.network.packet.AbilityUnlockToastPacket;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import com.benbenlaw.abilitylock.util.SpeedrunManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
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
        return canAttempt(player, task, new HashSet<>());
    }

    private static boolean canAttempt(ServerPlayer player, TaskType task, Set<Identifier> visitingTasks) {
        if (!visitingTasks.add(task.getId())) return true; // cycle guard - already verified on this path

        for (Identifier abilityId : task.getRequiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }

        for (Identifier parentTaskId : task.getParents()) {
            TaskType parentTask = TaskLoader.TASKS.get(parentTaskId);
            if (parentTask == null) continue; // dangling parent reference - skip rather than block forever
            if (!canAttempt(player, parentTask, visitingTasks)) return false;
        }

        return true;
    }

    public static Set<Identifier> totalAbilityCost(Collection<Identifier> taskIds) {
        Set<Identifier> total = new HashSet<>();
        for (Identifier taskId : taskIds) {
            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task == null) continue;
            total.addAll(realAbilityCost(task));
        }
        return total;
    }

    private static Set<Identifier> realAbilityCost(TaskType task) {
        Set<Identifier> cost = new HashSet<>();
        collectRealAbilityCost(task, cost, new HashSet<>());
        return cost;
    }

    private static void collectRealAbilityCost(TaskType task, Set<Identifier> out, Set<Identifier> visitingTasks) {
        if (!visitingTasks.add(task.getId())) return; // cycle guard

        for (Identifier abilityId : task.getRequiredAbilities()) {
            out.addAll(AbilityLoader.withAncestors(abilityId));
        }

        for (Identifier parentTaskId : task.getParents()) {
            TaskType parentTask = TaskLoader.TASKS.get(parentTaskId);
            if (parentTask == null) continue;
            collectRealAbilityCost(parentTask, out, visitingTasks);
        }
    }

    public static void ensureGridAssigned(ServerPlayer player, int gridWidth, int gridHeight, Set<Identifier> startingAbilities) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        if (!data.gridTaskIds().isEmpty()) return;

        AbilityLockData abilityData = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        PresetData preset = abilityData.presetId().map(PresetLoader.DATA::get).orElse(null);

        int effectiveWidth = gridWidth;
        int effectiveHeight = gridHeight;
        if (preset != null && preset.defaultGridSize().isPresent()) {
            int locked = preset.defaultGridSize().get();
            effectiveWidth = locked;
            effectiveHeight = locked;
        }

        Set<Identifier> allowedTasks = (preset != null && preset.restrictsTasks()) ? new HashSet<>(preset.validTasks()) : null;
        int immediatePercent = resolveImmediateTaskPercentage(preset);
        List<Identifier> grid = buildSolvableGrid(effectiveWidth * effectiveHeight, startingAbilities, player.getRandom(), immediatePercent, allowedTasks);

        TaskProgressData updated = data.withGrid(grid, effectiveWidth);
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }

    private static int resolveImmediateTaskPercentage(@Nullable PresetData preset) {
        if (preset != null && preset.immediateTaskPercentage().isPresent()) {
            return preset.immediateTaskPercentage().get();
        }

        return ServerConfig.immediateTaskPercentage.get();
    }

    private static List<Identifier> buildSolvableGrid(int targetCount, Set<Identifier> startingAbilities, RandomSource random, int immediatePercent, @Nullable Set<Identifier> allowedTasks) {
        Random rng = new Random(random.nextLong());

        List<TaskType> allTasks = new ArrayList<>();
        for (TaskType task : TaskLoader.TASKS.values()) {
            if (allowedTasks != null && !allowedTasks.contains(task.getId())) continue;
            allTasks.add(task);
        }
        Collections.shuffle(allTasks, rng);

        List<Identifier> selected = new ArrayList<>();
        Set<TaskType> selectedTasks = new HashSet<>();
        Set<Identifier> unionNeeded = new HashSet<>();

        for (TaskType task : allTasks) {
            if (selected.size() >= targetCount) break;

            Set<Identifier> realCost = realAbilityCost(task);
            if (!startingAbilities.containsAll(realCost)) continue;
            if (rng.nextInt(100) >= immediatePercent) continue;

            selected.add(task.getId());
            selectedTasks.add(task);
            unionNeeded.addAll(realCost);
        }
        unionNeeded.removeAll(startingAbilities);

        for (TaskType task : allTasks) {
            if (selected.size() >= targetCount) break;
            if (selectedTasks.contains(task)) continue;

            Set<Identifier> marginal = new HashSet<>(realAbilityCost(task));
            marginal.removeAll(startingAbilities);
            marginal.removeAll(unionNeeded);

            if (unionNeeded.size() + marginal.size() <= targetCount) {
                selected.add(task.getId());
                selectedTasks.add(task);
                unionNeeded.addAll(marginal);
            }
        }

        List<Identifier> best = List.of();
        for (int attempt = 0; attempt < SIMULATION_ATTEMPTS; attempt++) {
            List<Identifier> result = simulateAndTrim(selectedTasks, startingAbilities, random);
            if (result.size() > best.size()) {
                best = result;
            }
            if (best.size() >= selectedTasks.size()) break; // already got everything selected - no point retrying
        }
        return best;
    }

    private static final int SIMULATION_ATTEMPTS = 12;

    private static List<Identifier> simulateAndTrim(Set<TaskType> candidateTasks, Set<Identifier> startingAbilities, RandomSource random) {
        Random rng = new Random(random.nextLong());
        Set<Identifier> unlocked = new HashSet<>(startingAbilities);
        List<TaskType> remaining = new ArrayList<>(candidateTasks);
        List<Identifier> completed = new ArrayList<>();

        boolean progressed = true;
        while (progressed && !remaining.isEmpty()) {
            progressed = false;

            Iterator<TaskType> it = remaining.iterator();
            while (it.hasNext()) {
                TaskType task = it.next();
                if (!unlocked.containsAll(realAbilityCost(task))) continue;

                it.remove();
                completed.add(task.getId());
                progressed = true;

                simulateGrant(unlocked, remaining, rng);
            }
        }

        return completed;
    }

    private static void simulateGrant(Set<Identifier> unlocked, List<TaskType> remainingTasks, Random rng) {
        List<Identifier> eligible = new ArrayList<>();
        for (Identifier abilityId : AbilityLoader.DATA.keySet()) {
            if (unlocked.contains(abilityId)) continue;
            AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
            if (abilityData == null || !unlocked.containsAll(abilityData.parents())) continue;
            eligible.add(abilityId);
        }
        if (eligible.isEmpty()) return;

        Set<Identifier> relevant = relevantAbilitiesFor(remainingTasks);
        Set<Identifier> directNeeds = directNeedsFor(remainingTasks);

        List<Identifier> preferred = new ArrayList<>();
        for (Identifier id : eligible) {
            if (relevant.contains(id)) preferred.add(id);
        }
        List<Identifier> pool = preferred.isEmpty() ? eligible : preferred;

        List<Identifier> directPool = new ArrayList<>();
        for (Identifier id : pool) {
            if (directNeeds.contains(id)) directPool.add(id);
        }
        List<Identifier> finalPool = directPool.isEmpty() ? pool : directPool;

        unlocked.add(finalPool.get(rng.nextInt(finalPool.size())));
    }

    private static void onTaskCompleted(ServerPlayer player, TaskType task, TaskProgressData progressData) {
        grantGridAwareAbility(player, progressData).ifPresent(id -> announceUnlock(player, id, false));
        maybeGrantBonusAbility(player);
    }

    private static void maybeGrantBonusAbility(ServerPlayer player) {
        AbilityLockData abilityData = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        int bonusPercent = resolveBonusAbilityPercentage(abilityData);
        if (bonusPercent <= 0) return;
        if (player.getRandom().nextInt(100) >= bonusPercent) return;

        List<Identifier> eligible = AbilityChecker.getEligibleAbilities(abilityData);
        if (eligible.isEmpty()) return;

        Identifier chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
        AbilityChecker.grantSpecific(player, chosen);
        announceUnlock(player, chosen, true);
    }

    private static int resolveBonusAbilityPercentage(AbilityLockData data) {
        if (data.presetId().isEmpty()) return 0;

        PresetData preset = PresetLoader.DATA.get(data.presetId().get());
        return preset != null ? preset.bonusAbilityPercentage() : 0;
    }

    private static void announceUnlock(ServerPlayer player, Identifier id, boolean bonus) {
        PacketDistributor.sendToPlayer(player, new AbilityUnlockToastPacket(id, bonus));
    }

    private static Optional<Identifier> grantGridAwareAbility(ServerPlayer player, TaskProgressData progressData) {
        AbilityLockData abilityLockData = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Identifier> eligible = AbilityChecker.getEligibleAbilities(abilityLockData);
        if (eligible.isEmpty()) return Optional.empty();

        List<TaskType> remainingTasks = new ArrayList<>();
        for (Identifier taskId : progressData.gridTaskIds()) {
            if (progressData.isComplete(taskId)) continue;
            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task != null) remainingTasks.add(task);
        }
        Set<Identifier> relevant = relevantAbilitiesFor(remainingTasks);
        Set<Identifier> directNeeds = directNeedsFor(remainingTasks);

        List<Identifier> preferred = new ArrayList<>();
        for (Identifier abilityId : eligible) {
            if (relevant.contains(abilityId)) preferred.add(abilityId);
        }
        List<Identifier> pool = preferred.isEmpty() ? eligible : preferred;

        List<Identifier> directPool = new ArrayList<>();
        for (Identifier abilityId : pool) {
            if (directNeeds.contains(abilityId)) directPool.add(abilityId);
        }
        List<Identifier> finalPool = directPool.isEmpty() ? pool : directPool;

        Identifier chosen = finalPool.get(player.getRandom().nextInt(finalPool.size()));

        AbilityChecker.grantSpecific(player, chosen);
        return Optional.of(chosen);
    }

    private static Set<Identifier> directNeedsFor(Collection<TaskType> tasks) {
        Set<Identifier> direct = new HashSet<>();
        for (TaskType task : tasks) {
            collectDirectNeeds(task, direct, new HashSet<>());
        }
        return direct;
    }

    private static void collectDirectNeeds(TaskType task, Set<Identifier> out, Set<Identifier> visitingTasks) {
        if (!visitingTasks.add(task.getId())) return;

        out.addAll(task.getRequiredAbilities());

        for (Identifier parentTaskId : task.getParents()) {
            TaskType parentTask = TaskLoader.TASKS.get(parentTaskId);
            if (parentTask == null) continue;
            collectDirectNeeds(parentTask, out, visitingTasks);
        }
    }

    private static Set<Identifier> relevantAbilitiesFor(Collection<TaskType> tasks) {
        Set<Identifier> relevant = new HashSet<>();
        for (TaskType task : tasks) {
            relevant.addAll(realAbilityCost(task));
        }
        return relevant;
    }

    public static Set<Identifier> getRunRelevantAbilities(Player player) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        List<TaskType> gridTasks = new ArrayList<>();
        for (Identifier taskId : progressData.gridTaskIds()) {
            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task != null) gridTasks.add(task);
        }
        return relevantAbilitiesFor(gridTasks);
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
        TaskProgressData blank = new TaskProgressData(new HashMap<>(), new HashSet<>(), current.gridTaskIds(), current.gridWidth());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, blank);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(blank));
    }

    public static void resetOne(ServerPlayer player, Identifier taskId) {
        TaskProgressData data = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        Map<Identifier, Integer> newProgress = new HashMap<>(data.progress());
        newProgress.remove(taskId);

        Set<Identifier> newCompleted = new HashSet<>(data.completed());
        newCompleted.remove(taskId);

        TaskProgressData updated = new TaskProgressData(newProgress, newCompleted, data.gridTaskIds(), data.gridWidth());
        player.setData(AbilityLockAttachments.TASK_PROGRESS, updated);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(updated));
    }
}