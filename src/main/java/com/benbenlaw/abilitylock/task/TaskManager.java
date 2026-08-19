package com.benbenlaw.abilitylock.task;

import com.benbenlaw.abilitylock.AbilityLock;
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
        if (!task.hasRequiredAbilities()) return true;
        for (Identifier abilityId : task.getRequiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }
        return true;
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

        int immediatePercent = resolveImmediateTaskPercentage(preset);
        List<Identifier> grid = buildSolvableGrid(effectiveWidth * effectiveHeight, startingAbilities, immediatePercent, preset, player.getRandom());

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

    private static List<Identifier> buildSolvableGrid(
            int targetCount, Set<Identifier> startingAbilities, int immediatePercent, @Nullable PresetData preset, RandomSource random
    ) {
        Map<Identifier, Integer> depthCache = new HashMap<>();

        List<Identifier> best = null;
        int bestSolvedCount = -1;
        int bestDepth = -1;

        for (int attempt = 0; attempt < 20; attempt++) {
            List<Identifier> candidate = generateCandidateGrid(targetCount, startingAbilities, immediatePercent, preset, random);
            Set<Identifier> solved = simulateGridOnly(candidate, startingAbilities, preset, random);

            int depth = 0;
            for (Identifier id : solved) {
                TaskType task = TaskLoader.TASKS.get(id);
                if (task != null) depth = Math.max(depth, taskDepth(task, depthCache));
            }

            boolean better = solved.size() > bestSolvedCount
                    || (solved.size() == bestSolvedCount && depth > bestDepth);

            if (better) {
                bestSolvedCount = solved.size();
                bestDepth = depth;
                best = candidate;
            }
        }

        if (bestSolvedCount < targetCount) {
            AbilityLock.LOGGER.warn(
                    "Grid generation: best candidate over 20 attempts only fully solves {}/{} grid tasks (max depth reached: {}).",
                    bestSolvedCount, targetCount, bestDepth
            );
        }
        return best;
    }

    private static int abilityDepth(Identifier abilityId, Map<Identifier, Integer> cache, Set<Identifier> visiting) {
        if (cache.containsKey(abilityId)) return cache.get(abilityId);
        if (!visiting.add(abilityId)) return 0; // cycle guard

        AbilityData data = AbilityLoader.DATA.get(abilityId);
        int depth = 0;
        if (data != null) {
            for (Identifier parent : data.parents()) {
                depth = Math.max(depth, abilityDepth(parent, cache, visiting) + 1);
            }
        }
        cache.put(abilityId, depth);
        return depth;
    }

    private static int taskDepth(TaskType task, Map<Identifier, Integer> cache) {
        int max = 0;
        for (Identifier abilityId : task.getRequiredAbilities()) {
            max = Math.max(max, abilityDepth(abilityId, cache, new HashSet<>()));
        }
        return max;
    }

    private static List<Identifier> generateCandidateGrid(
            int targetCount, Set<Identifier> startingAbilities, int immediatePercent, @Nullable PresetData preset, RandomSource random
    ) {
        Set<Identifier> unlocked = new HashSet<>(startingAbilities);
        Set<Identifier> simCompleted = new HashSet<>();
        Random rng = new Random(random.nextLong());

        List<TaskType> pool = new ArrayList<>(TaskLoader.TASKS.values());
        Collections.shuffle(pool, rng);

        Map<Identifier, Integer> waveOf = new HashMap<>();
        int wave = 0;
        boolean progressMade = true;

        while (progressMade) {
            progressMade = false;
            wave++;

            for (TaskType task : pool) {
                Identifier taskId = task.getId();
                if (simCompleted.contains(taskId)) continue;
                if (!simCompleted.containsAll(task.getParents())) continue;
                if (!unlocked.containsAll(task.getRequiredAbilities())) continue;

                simCompleted.add(taskId);
                waveOf.put(taskId, wave);
                progressMade = true;

                List<Identifier> eligible = eligibleAbilitiesForSim(unlocked, preset);
                if (!eligible.isEmpty()) {
                    Set<Identifier> relevant = relevantAbilitiesForIncomplete(pool, simCompleted);
                    Identifier grant = AbilityChecker.pickPreferredAbility(eligible, relevant, random);
                    unlocked.add(grant);
                }
            }
        }

        if (waveOf.size() < pool.size()) {
            Map<Identifier, TaskType> byId = new HashMap<>();
            for (TaskType task : pool) byId.put(task.getId(), task);

            List<String> details = new ArrayList<>();
            for (TaskType task : pool) {
                Identifier taskId = task.getId();
                if (waveOf.containsKey(taskId)) continue;

                List<Identifier> missingParents = new ArrayList<>();
                for (Identifier parentId : task.getParents()) {
                    if (!byId.containsKey(parentId) || !waveOf.containsKey(parentId)) {
                        missingParents.add(parentId);
                    }
                }

                List<Identifier> missingAbilities = new ArrayList<>();
                for (Identifier abilityId : task.getRequiredAbilities()) {
                    if (!unlocked.contains(abilityId)) missingAbilities.add(abilityId);
                }

                details.add(taskId + " [missing parents=" + missingParents + ", missing abilities=" + missingAbilities + "]");
            }

            AbilityLock.LOGGER.warn(
                    "Grid simulation: {} of {} tasks never became reachable. Check parent/ability references below:\n{}",
                    pool.size() - waveOf.size(), pool.size(), String.join("\n", details)
            );
        }

        List<Identifier> immediate = new ArrayList<>();
        Map<Integer, List<Identifier>> progressionByWave = new TreeMap<>();

        for (TaskType task : pool) {
            Identifier taskId = task.getId();
            if (!waveOf.containsKey(taskId)) continue;

            if (startingAbilities.containsAll(task.getRequiredAbilities())) {
                immediate.add(taskId);
            } else {
                progressionByWave.computeIfAbsent(waveOf.get(taskId), w -> new ArrayList<>()).add(taskId);
            }
        }
        Collections.shuffle(immediate, rng);
        for (List<Identifier> list : progressionByWave.values()) Collections.shuffle(list, rng);

        int immediateCount = Math.max(1, Math.round(targetCount * (immediatePercent / 100f)));
        immediateCount = Math.min(immediateCount, targetCount);

        List<Identifier> selected = new ArrayList<>();

        Iterator<Identifier> immIter = immediate.iterator();
        while (selected.size() < immediateCount && immIter.hasNext()) {
            selected.add(immIter.next());
        }

        List<Integer> waveKeysDescending = new ArrayList<>(progressionByWave.keySet());
        waveKeysDescending.sort(Collections.reverseOrder());

        boolean any = true;
        while (selected.size() < targetCount && any) {
            any = false;
            for (Integer waveKey : waveKeysDescending) {
                if (selected.size() >= targetCount) break;
                List<Identifier> waveList = progressionByWave.get(waveKey);
                if (waveList.isEmpty()) continue;
                selected.add(waveList.remove(waveList.size() - 1));
                any = true;
            }
        }

        while (selected.size() < targetCount && immIter.hasNext()) {
            selected.add(immIter.next());
        }
        for (Integer waveKey : waveKeysDescending) {
            List<Identifier> waveList = progressionByWave.get(waveKey);
            while (selected.size() < targetCount && !waveList.isEmpty()) {
                selected.add(waveList.remove(waveList.size() - 1));
            }
        }

        if (selected.size() < targetCount) {
            List<Identifier> leftover = new ArrayList<>();
            Set<Identifier> selectedSet = new HashSet<>(selected);
            for (TaskType task : pool) {
                if (!selectedSet.contains(task.getId())) leftover.add(task.getId());
            }
            Collections.shuffle(leftover, rng);
            Iterator<Identifier> leftoverIter = leftover.iterator();
            while (selected.size() < targetCount && leftoverIter.hasNext()) {
                selected.add(leftoverIter.next());
            }
        }

        Collections.shuffle(selected, rng);
        return selected;
    }

    private static Set<Identifier> simulateGridOnly(List<Identifier> gridTaskIds, Set<Identifier> startingAbilities, @Nullable PresetData preset, RandomSource random) {
        Set<Identifier> unlocked = new HashSet<>(startingAbilities);
        Set<Identifier> completed = new HashSet<>();

        List<TaskType> gridTasks = new ArrayList<>();
        for (Identifier id : gridTaskIds) {
            TaskType task = TaskLoader.TASKS.get(id);
            if (task != null) gridTasks.add(task);
        }

        boolean progressMade = true;
        while (progressMade) {
            progressMade = false;
            for (TaskType task : gridTasks) {
                Identifier taskId = task.getId();
                if (completed.contains(taskId)) continue;
                if (!unlocked.containsAll(task.getRequiredAbilities())) continue;

                completed.add(taskId);
                progressMade = true;

                List<Identifier> eligible = eligibleAbilitiesForSim(unlocked, preset);
                if (!eligible.isEmpty()) {
                    Set<Identifier> relevant = relevantAbilitiesForIncomplete(gridTasks, completed);
                    Identifier grant = AbilityChecker.pickPreferredAbility(eligible, relevant, random);
                    unlocked.add(grant);
                }
            }
        }

        return completed;
    }

    private static List<Identifier> eligibleAbilitiesForSim(Set<Identifier> unlocked, @Nullable PresetData preset) {
        Set<Identifier> allowedByPreset = (preset != null && preset.restrictsUnlockable())
                ? new HashSet<>(preset.unlockableAbilities())
                : null;

        List<Identifier> eligible = new ArrayList<>();
        for (Identifier abilityId : AbilityLoader.DATA.keySet()) {
            if (unlocked.contains(abilityId)) continue;
            if (allowedByPreset != null && !allowedByPreset.contains(abilityId)) continue;

            AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
            if (!unlocked.containsAll(abilityData.parents())) continue;

            eligible.add(abilityId);
        }
        return eligible;
    }

    private static void onTaskCompleted(ServerPlayer player, TaskType task, TaskProgressData progressData) {
        grantPreferredAbility(player).ifPresent(id -> announceUnlock(player, id, false));
        maybeGrantBonusAbility(player);
    }

    private static Optional<Identifier> grantPreferredAbility(ServerPlayer player) {
        AbilityLockData abilityLockData = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Identifier> eligible = AbilityChecker.getEligibleAbilities(abilityLockData);
        if (eligible.isEmpty()) return Optional.empty();

        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);

        List<TaskType> gridTasks = new ArrayList<>();
        for (Identifier taskId : progressData.gridTaskIds()) {
            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task != null) gridTasks.add(task);
        }
        Set<Identifier> relevant = relevantAbilitiesForIncomplete(gridTasks, progressData.completed());

        Identifier chosen = AbilityChecker.pickPreferredAbility(eligible, relevant, player.getRandom());
        AbilityChecker.grantSpecific(player, chosen);
        return Optional.of(chosen);
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

    private static Set<Identifier> relevantAbilitiesForIncomplete(Collection<TaskType> pool, Set<Identifier> completedIds) {
        Set<Identifier> relevant = new HashSet<>();
        for (TaskType task : pool) {
            if (completedIds.contains(task.getId())) continue;
            for (Identifier abilityId : task.getRequiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }
        return relevant;
    }

    public static Set<Identifier> getRunRelevantAbilities(Player player) {
        TaskProgressData progressData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        Set<Identifier> relevant = new HashSet<>();
        for (Identifier taskId : progressData.gridTaskIds()) {
            TaskType task = TaskLoader.TASKS.get(taskId);
            if (task == null) continue;
            for (Identifier abilityId : task.getRequiredAbilities()) {
                relevant.addAll(AbilityLoader.withAncestors(abilityId));
            }
        }
        return relevant;
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