package com.benbenlaw.abilitylock.command;

import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import com.benbenlaw.abilitylock.task.TaskLoader;
import com.benbenlaw.abilitylock.task.TaskManager;
import com.benbenlaw.abilitylock.task.TaskType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AbilityLockCommand {

    private static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    AbilityLoader.DATA.keySet().stream().map(Identifier::toString).toList(), builder);

    private static final SuggestionProvider<CommandSourceStack> TASK_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    TaskLoader.TASKS.keySet().stream().map(Identifier::toString).toList(), builder);

    private static final SuggestionProvider<CommandSourceStack> PRESET_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    PresetLoader.DATA.keySet().stream().map(Identifier::toString).toList(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("abilitylock")
                        .then(Commands.literal("ability")
                                .then(Commands.literal("unlock")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .executes(AbilityLockCommand::unlockRandom)
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::unlockSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::unlockOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("lock")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::lockSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::lockOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("list")
                                        .executes(AbilityLockCommand::listSelf)
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                                .executes(AbilityLockCommand::listOther)
                                        )
                                )
                                .then(Commands.literal("check")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::checkAbilitySelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::checkAbilityOther)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("task")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("task", StringArgumentType.greedyString())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::completeTaskSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("task", StringArgumentType.greedyString())
                                                        .suggests(TASK_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::completeTaskOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.literal("all")
                                                .executes(AbilityLockCommand::resetAllSelf)
                                                .then(Commands.argument("target", StringArgumentType.word())
                                                        .executes(AbilityLockCommand::resetAllOther)
                                                )
                                        )
                                        .then(Commands.argument("task", StringArgumentType.greedyString())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::resetOneSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("task", StringArgumentType.greedyString())
                                                        .suggests(TASK_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::resetOneOther)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("validate")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(AbilityLockCommand::validateData)
                                .then(Commands.literal("preset")
                                        .then(Commands.argument("preset", StringArgumentType.greedyString())
                                                .suggests(PRESET_SUGGESTIONS)
                                                .executes(AbilityLockCommand::validatePreset)
                                        )
                                )
                                .then(Commands.literal("ability")
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::validateAbility)
                                        )
                                )
                                .then(Commands.literal("task")
                                        .then(Commands.argument("task", StringArgumentType.greedyString())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::validateTask)
                                        )
                                )
                        )
                        .then(Commands.literal("simulate")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                                        .executes(AbilityLockCommand::simulateSelf)
                                        .then(Commands.literal("preset")
                                                .then(Commands.argument("preset", StringArgumentType.greedyString())
                                                        .suggests(PRESET_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::simulatePreset)
                                                )
                                        )
                                )
                        )
        );
    }

    private static int validateAbility(CommandContext<CommandSourceStack> ctx) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        if (!AbilityLoader.DATA.containsKey(abilityId)) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        Set<Identifier> closure = new HashSet<>(AbilityLoader.withAncestors(abilityId));
        closure.remove(abilityId);

        String targetName = displayNameOf(abilityId);

        if (closure.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + targetName + "' (" + abilityId + ") has no prerequisites - it's a root ability."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "'" + targetName + "' (" + abilityId + ") requires " + closure.size() + " prerequisite ability(ies):"), false);

        closure.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .forEach(id -> {
                    boolean exists = AbilityLoader.DATA.containsKey(id);
                    String label = exists ? displayNameOf(id) : (id + " (MISSING - dangling reference!)");
                    ctx.getSource().sendSuccess(() -> Component.literal(" - " + label), false);
                });

        return 1;
    }

    private static int validateTask(CommandContext<CommandSourceStack> ctx) {
        Identifier taskId = getTaskId(ctx);
        if (taskId == null) return 0;

        TaskType task = TaskLoader.TASKS.get(taskId);
        if (task == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        String targetName = task.getData().displayName();

        List<Identifier> parentTasks = task.getParents();
        if (parentTasks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + targetName + "' (" + taskId + ") has no parent tasks."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + targetName + "' (" + taskId + ") has " + parentTasks.size() + " parent task(s):"), false);
            for (Identifier parentTaskId : parentTasks) {
                TaskType parentTask = TaskLoader.TASKS.get(parentTaskId);
                String label = parentTask != null
                        ? parentTask.getData().displayName() + " (" + parentTaskId + ")"
                        : parentTaskId + " (MISSING - dangling reference!)";
                ctx.getSource().sendSuccess(() -> Component.literal(" - " + label), false);
            }
        }

        Set<Identifier> closure = TaskManager.totalAbilityCost(List.of(taskId));

        if (closure.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + targetName + "' (" + taskId + ") requires no abilities."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "'" + targetName + "' (" + taskId + ") requires " + closure.size() + " ability(ies) in total (including parent task chain):"), false);

        closure.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .forEach(id -> {
                    boolean exists = AbilityLoader.DATA.containsKey(id);
                    String label = exists ? displayNameOf(id) : (id + " (MISSING - dangling reference!)");
                    ctx.getSource().sendSuccess(() -> Component.literal(" - " + label), false);
                });

        return 1;
    }

    private static int simulateSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (data.presetId().isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(player.getName().getString() + " has no preset assigned - use '/abilitylock simulate <amount> preset <id>' instead."));
            return 0;
        }

        PresetData preset = PresetLoader.DATA.get(data.presetId().get());
        if (preset == null) {
            ctx.getSource().sendFailure(Component.literal("Assigned preset '" + data.presetId().get() + "' no longer exists."));
            return 0;
        }

        return runSimulation(ctx, data.presetId().get(), preset);
    }

    private static int simulatePreset(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "preset");
        Identifier presetId;
        try {
            presetId = Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid preset id: " + raw));
            return 0;
        }

        PresetData preset = PresetLoader.DATA.get(presetId);
        if (preset == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown preset: " + presetId));
            return 0;
        }

        return runSimulation(ctx, presetId, preset);
    }

    private static int runSimulation(CommandContext<CommandSourceStack> ctx, Identifier presetId, PresetData preset) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int gridSize = preset.defaultGridSize().orElse(5);

        Set<Identifier> startingAbilities = new HashSet<>(preset.startingAbilities());
        Set<Identifier> allowedTasks = preset.restrictsTasks() ? new HashSet<>(preset.validTasks()) : null;
        int immediatePercent = TaskManager.resolveImmediateTaskPercentage(preset);

        TaskManager.SimulationResult result = TaskManager.simulateUnlockOrders(amount, gridSize, gridSize, startingAbilities, immediatePercent, allowedTasks);
        List<List<Identifier>> sequences = result.unlockSequences();

        if (sequences.isEmpty() || sequences.get(0).isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Simulation produced no ability grants - check the preset's grid actually contains completable tasks."));
            return 0;
        }

        Map<Identifier, List<Integer>> positionsByAbility = new HashMap<>();
        for (List<Identifier> seq : sequences) {
            for (int i = 0; i < seq.size(); i++) {
                positionsByAbility.computeIfAbsent(seq.get(i), k -> new ArrayList<>()).add(i + 1);
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Simulated " + amount + " completion order(s) for '" + presetId + "' (" + gridSize + "x" + gridSize + ", " + result.gridTaskIds().size() + " tasks):"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Position range each ability was unlocked at (sorted by average position):"), false);

        List<Map.Entry<Identifier, List<Integer>>> entries = new ArrayList<>(positionsByAbility.entrySet());
        entries.sort(Comparator.comparingDouble(e -> average(e.getValue())));

        for (Map.Entry<Identifier, List<Integer>> entry : entries) {
            List<Integer> positions = entry.getValue();
            int min = Collections.min(positions);
            int max = Collections.max(positions);
            double avg = average(positions);
            int seenCount = positions.size();
            String name = displayNameOf(entry.getKey());
            String rangeText = (min == max) ? ("pos " + min) : ("pos " + min + "-" + max);

            ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                    " - %s: %s (avg %.1f, seen %d/%d)", name, rangeText, avg, seenCount, amount)), false);
        }

        return 1;
    }

    private static double average(List<Integer> values) {
        double sum = 0;
        for (int v : values) sum += v;
        return sum / values.size();
    }

    private static int validatePreset(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "preset");
        Identifier presetId;
        try {
            presetId = Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid preset id: " + raw));
            return 0;
        }

        PresetData preset = PresetLoader.DATA.get(presetId);
        if (preset == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown preset: " + presetId));
            return 0;
        }

        List<String> issues = new ArrayList<>();

        for (Identifier abilityId : preset.startingAbilities()) {
            if (!AbilityLoader.DATA.containsKey(abilityId)) {
                issues.add("Starting ability '" + abilityId + "' does not exist");
            }
        }
        for (Identifier abilityId : preset.unlockableAbilities()) {
            if (!AbilityLoader.DATA.containsKey(abilityId)) {
                issues.add("Unlockable ability '" + abilityId + "' does not exist");
            }
        }
        for (Identifier taskId : preset.validTasks()) {
            if (!TaskLoader.TASKS.containsKey(taskId)) {
                issues.add("valid_tasks references unknown task '" + taskId + "'");
            }
        }

        if (!issues.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Preset '" + presetId + "' has " + issues.size() + " issue(s):"));
            for (String issue : issues) {
                ctx.getSource().sendFailure(Component.literal(" - " + issue));
            }
            return 0;
        }

        if (!preset.restrictsTasks()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Preset '" + presetId + "' is unrestricted (no valid_tasks) - grid construction " +
                            "selects from the full task pool and trims automatically, no fixed cardinality concern."), false);
            return 1;
        }

        Set<Identifier> totalCost = TaskManager.totalAbilityCost(preset.validTasks());
        int neededAbilities = totalCost.size();
        int totalTasks = preset.validTasks().size();
        int required = Math.max(neededAbilities, totalTasks);

        if (preset.defaultGridSize().isPresent()) {
            int size = preset.defaultGridSize().get();
            int budget = size * size;
            boolean fits = required <= budget;

            if (fits) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "Preset '" + presetId + "': " + totalTasks + " valid task(s) need " + neededAbilities +
                                " distinct abilities. Fits within the locked " + size + "x" + size + " grid (" + budget + " budget)."), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                        "Preset '" + presetId + "': " + totalTasks + " valid task(s) need " + neededAbilities +
                                " distinct abilities, but the locked grid size " + size + "x" + size + " only allows " + budget +
                                " grants. Raise default_grid_size or trim valid_tasks."));
                return 0;
            }
        }

        int minSize = (int) Math.ceil(Math.sqrt(required));
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Preset '" + presetId + "': " + totalTasks + " valid task(s) need " + neededAbilities +
                        " distinct abilities. No locked grid size - needs at least " + minSize + "x" + minSize + " to fully fit."), false);
        return 1;
    }

    private static int validateData(CommandContext<CommandSourceStack> ctx) {
        List<String> issues = new ArrayList<>();

        for (Map.Entry<Identifier, AbilityData> entry : AbilityLoader.DATA.entrySet()) {
            Identifier abilityId = entry.getKey();
            for (Identifier parent : entry.getValue().parents()) {
                if (!AbilityLoader.DATA.containsKey(parent)) {
                    issues.add("Ability '" + abilityId + "' has unknown parent ability '" + parent + "'");
                }
            }
        }

        for (Map.Entry<Identifier, TaskType> entry : TaskLoader.TASKS.entrySet()) {
            Identifier taskId = entry.getKey();
            TaskType task = entry.getValue();

            for (Identifier abilityId : task.getRequiredAbilities()) {
                if (!AbilityLoader.DATA.containsKey(abilityId)) {
                    issues.add("Task '" + taskId + "' requires unknown ability '" + abilityId + "'");
                }
            }

            for (Identifier parentTaskId : task.getParents()) {
                if (!TaskLoader.TASKS.containsKey(parentTaskId)) {
                    issues.add("Task '" + taskId + "' has unknown parent task '" + parentTaskId + "'");
                }
            }
        }

        if (issues.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Validation passed: " + AbilityLoader.DATA.size() + " abilities, " +
                            TaskLoader.TASKS.size() + " tasks - no dangling references found."), false);
            return 1;
        }

        ctx.getSource().sendFailure(Component.literal("Validation found " + issues.size() + " issue(s):"));
        for (String issue : issues) {
            ctx.getSource().sendFailure(Component.literal(" - " + issue));
        }
        return 0;
    }

    private static @Nullable Identifier getAbilityId(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "ability");
        try {
            return Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid ability id: " + raw));
            return null;
        }
    }

    private static @Nullable Identifier getTaskId(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "task");
        try {
            return Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid task id: " + raw));
            return null;
        }
    }

    public static int unlockRandom(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;

        return AbilityChecker.grantRandomEligible(player)
                .map(id -> {
                    String name = displayNameOf(id);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Unlocked '" + name + "' for " + player.getName().getString()), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            player.getName().getString() + " has no eligible abilities left to unlock."), false);
                    return 0;
                });
    }

    private static int unlockSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return resolveAndUnlock(ctx, player);
    }

    private static int unlockOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return resolveAndUnlock(ctx, player);
    }

    private static int lockSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return resolveAndLock(ctx, player);
    }

    private static int lockOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return resolveAndLock(ctx, player);
    }

    private static int resolveAndLock(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (!data.has(abilityId)) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " doesn't have '" + abilityId + "' unlocked."), false);
            return 1;
        }

        return applyLock(ctx, player, abilityId, abilityData);
    }

    private static int applyLock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Identifier abilityId, AbilityData abilityData) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<Identifier> updated = new HashSet<>(data.unlockedAbilities());
        Set<Identifier> removed = new HashSet<>();
        removeCascade(abilityId, updated, removed);

        AbilityLockData newData = new AbilityLockData(updated, data.presetId(), data.eliminated(), data.lastGranted(), data.bonusPoints());
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Locked '" + abilityData.displayName() + "'" +
                        (removed.size() > 1 ? " and " + (removed.size() - 1) + " dependent ability(ies)" : "") +
                        " for " + player.getName().getString()), true);
        return 1;
    }

    private static void removeCascade(Identifier id, Set<Identifier> unlocked, Set<Identifier> removed) {
        if (!unlocked.remove(id)) return;
        removed.add(id);

        for (Map.Entry<Identifier, AbilityData> entry : AbilityLoader.DATA.entrySet()) {
            if (entry.getValue().parents().contains(id)) {
                removeCascade(entry.getKey(), unlocked, removed);
            }
        }
    }

    private static ServerPlayer getSelfOrFail(CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by/as a player."));
            return null;
        }
    }

    private static int resolveAndUnlock(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.has(abilityId)) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " already has '" + abilityId + "' unlocked."), false);
            return 1;
        }

        for (Identifier parent : abilityData.parents()) {
            if (!AbilityChecker.isUnlocked(player, parent)) {
                ctx.getSource().sendFailure(Component.literal(
                        "Cannot unlock '" + abilityId + "' - prerequisite '" + parent + "' is not unlocked yet."));
                return 0;
            }
        }

        AbilityChecker.grantSpecific(player, abilityId);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Unlocked '" + abilityData.displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }

    private static int listSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doList(ctx, player);
    }

    private static int listOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doList(ctx, player);
    }

    private static int doList(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<Identifier> unlocked = data.unlockedAbilities();

        if (unlocked.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " has not unlocked any abilities yet."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                player.getName().getString() + " has unlocked (" + unlocked.size() + "/" + AbilityLoader.DATA.size() + "):"), false);

        unlocked.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .forEach(id -> {
                    String name = displayNameOf(id);
                    ctx.getSource().sendSuccess(() -> Component.literal(" - " + name), false);
                });

        return 1;
    }

    private static String displayNameOf(Identifier id) {
        AbilityData data = AbilityLoader.DATA.get(id);
        return data != null ? data.displayName() : id.toString();
    }

    private static int checkAbilitySelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doCheckAbility(ctx, player);
    }

    private static int checkAbilityOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doCheckAbility(ctx, player);
    }

    private static int doCheckAbility(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        Identifier broken = AbilityChecker.findUnreachableAncestor(player, abilityId);

        if (broken == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + abilityData.displayName() + "' (" + abilityId + ") CAN be unlocked during "
                            + player.getName().getString() + "'s current run."), false);
            return 1;
        }

        AbilityData brokenData = AbilityLoader.DATA.get(broken);
        String reason = brokenData == null
                ? "'" + broken + "' does not exist (broken ability id)"
                : "'" + brokenData.displayName() + "' (" + broken + ") is excluded by the active preset";

        String location = broken.equals(abilityId) ? "" : " (blocked via ancestor)";

        ctx.getSource().sendFailure(Component.literal(
                "'" + abilityData.displayName() + "' (" + abilityId + ") CANNOT be unlocked during "
                        + player.getName().getString() + "'s current run" + location + ": " + reason));
        return 0;
    }

    private static int completeTaskSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doCompleteTask(ctx, player);
    }

    private static int completeTaskOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doCompleteTask(ctx, player);
    }

    private static int doCompleteTask(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier taskId = getTaskId(ctx);
        if (taskId == null) return 0;

        TaskType task = TaskLoader.TASKS.get(taskId);
        if (task == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        TaskManager.forceComplete(player, task);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Marked '" + task.getData().displayName() + "' complete for " + player.getName().getString()), true);
        return 1;
    }

    private static int resetAllSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doResetAll(ctx, player);
    }

    private static int resetAllOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doResetAll(ctx, player);
    }

    private static int doResetAll(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        TaskManager.resetAll(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset all task progress for " + player.getName().getString()), true);
        return 1;
    }

    private static int resetOneSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doResetOne(ctx, player);
    }

    private static int resetOneOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doResetOne(ctx, player);
    }

    private static int doResetOne(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier taskId = getTaskId(ctx);
        if (taskId == null) return 0;

        TaskType task = TaskLoader.TASKS.get(taskId);
        if (task == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        TaskManager.resetOne(player, taskId);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset task '" + task.getData().displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }
}