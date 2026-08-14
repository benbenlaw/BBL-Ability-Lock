package com.benbenlaw.abilitylock.command;

import com.benbenlaw.abilitylock.ability.old.Ability;
import com.benbenlaw.abilitylock.ability.old.AbilityChecker;
import com.benbenlaw.abilitylock.ability.old.AbilityRegistry;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.task.Task;
import com.benbenlaw.abilitylock.task.TaskManager;
import com.benbenlaw.abilitylock.task.TaskRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AbilityLockCommand {

    private static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(AbilityRegistry.all().keySet(), builder);

    private static final SuggestionProvider<CommandSourceStack> TASK_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(TaskRegistry.all().keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("abilitylock")
                        .then(Commands.literal("unlock")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(AbilityLockCommand::unlockRandom)
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .suggests(ABILITY_SUGGESTIONS)
                                        .executes(AbilityLockCommand::unlockSelf)
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .executes(AbilityLockCommand::unlockOther)
                                        )
                                )
                        )
                        .then(Commands.literal("lock")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .suggests(ABILITY_SUGGESTIONS)
                                        .executes(AbilityLockCommand::lockSelf)
                                        .then(Commands.argument("target", StringArgumentType.word())
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
                        .then(Commands.literal("task")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("task", StringArgumentType.word())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::completeTaskSelf)
                                                .then(Commands.argument("target", StringArgumentType.word())
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
                                        .then(Commands.argument("task", StringArgumentType.word())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::resetOneSelf)
                                                .then(Commands.argument("target", StringArgumentType.word())
                                                        .executes(AbilityLockCommand::resetOneOther)
                                                )
                                        )
                                )
                        )
        );
    }

    public static int unlockRandom(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;

        return AbilityChecker.grantRandomEligible(player)
                .map(a -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Unlocked '" + a.displayName() + "' for " + player.getName().getString()), true);
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
        String abilityId = StringArgumentType.getString(ctx, "ability");

        Optional<Ability> abilityOpt = AbilityRegistry.get(abilityId);
        if (abilityOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        Ability ability = abilityOpt.get();
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (!data.has(ability.id())) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " doesn't have '" + ability.id() + "' unlocked."), false);
            return 1;
        }

        return applyLock(ctx, player, ability);
    }

    private static int applyLock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Ability ability) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<String> updated = new HashSet<>(data.unlockedAbilities());
        Set<String> removed = new HashSet<>();
        removeCascade(ability.id(), updated, removed);

        AbilityLockData newData = new AbilityLockData(updated);
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Locked '" + ability.displayName() + "'" +
                        (removed.size() > 1 ? " and " + (removed.size() - 1) + " dependent ability(ies)" : "") +
                        " for " + player.getName().getString()), true);
        return 1;
    }

    private static void removeCascade(String id, Set<String> unlocked, Set<String> removed) {
        if (!unlocked.remove(id)) return;
        removed.add(id);

        for (Ability ability : AbilityRegistry.all().values()) {
            if (id.equals(ability.parent())) {
                removeCascade(ability.id(), unlocked, removed);
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
        String abilityId = StringArgumentType.getString(ctx, "ability");

        Optional<Ability> abilityOpt = AbilityRegistry.get(abilityId);
        if (abilityOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        Ability ability = abilityOpt.get();
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.has(ability.id())) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " already has '" + ability.id() + "' unlocked."), false);
            return 1;
        }

        if (ability.hasParent() && !AbilityChecker.isUnlocked(player, ability.parent())) {
            ctx.getSource().sendFailure(Component.literal(
                    "Cannot unlock '" + ability.id() + "' - prerequisite '" + ability.parent() + "' is not unlocked yet."));
            return 0;
        }

        AbilityChecker.grantSpecific(player, ability);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Unlocked '" + ability.displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }

    private static int applyUnlock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Ability ability) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        Set<String> updated = new HashSet<>(data.unlockedAbilities());
        updated.add(ability.id());
        AbilityLockData newData = new AbilityLockData(updated);
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Unlocked '" + ability.displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }

    public static List<Ability> getEligibleAbilities(AbilityLockData data) {
        List<Ability> eligible = new ArrayList<>();
        for (Ability ability : AbilityRegistry.all().values()) {
            if (data.has(ability.id())) continue;
            if (ability.hasParent() && !data.has(ability.parent())) continue;
            eligible.add(ability);
        }
        return eligible;
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
        Set<String> unlocked = data.unlockedAbilities();

        if (unlocked.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " has not unlocked any abilities yet."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                player.getName().getString() + " has unlocked (" + unlocked.size() + "/" + AbilityRegistry.all().size() + "):"), false);

        AbilityRegistry.all().values().stream()
                .filter(a -> unlocked.contains(a.id()))
                .forEach(a -> ctx.getSource().sendSuccess(() -> Component.literal(" - " + a.displayName()), false));

        return 1;
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
        String taskId = StringArgumentType.getString(ctx, "task");
        Optional<Task> taskOpt = TaskRegistry.get(taskId);
        if (taskOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        Task task = taskOpt.get();
        TaskManager.forceComplete(player, task);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Marked '" + task.displayName() + "' complete for " + player.getName().getString()), true);
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
        String taskId = StringArgumentType.getString(ctx, "task");
        Optional<Task> taskOpt = TaskRegistry.get(taskId);
        if (taskOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        TaskManager.resetOne(player, taskId);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset task '" + taskOpt.get().displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }
}