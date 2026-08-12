package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.command.AbilityLockCommand;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import com.benbenlaw.abilitylock.task.TaskManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AbilityLockCommand.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String levelName = player.level().getServer().getWorldData().getLevelName();
        PendingAbilityLockWorldSettings.Result pending = PendingAbilityLockWorldSettings.consumeIfMatches(levelName);

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.unlockedAbilities().isEmpty()) {
            data = applyFirstLoginAbilities(player, data, pending);
            player.setData(AbilityLockAttachments.ABILITY_LOCK, data);
        }

        int gridSize = pending != null ? pending.gridSize() : PendingAbilityLockWorldSettings.DEFAULT_GRID_SIZE;
        TaskManager.ensureGridAssigned(player, gridSize, data.unlockedAbilities());

        TaskProgressData taskData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(taskData));

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(data));
    }

    private static AbilityLockData applyFirstLoginAbilities(
            ServerPlayer player, AbilityLockData data, PendingAbilityLockWorldSettings.@Nullable Result pending) {

        Set<String> presetAbilities = pending != null && pending.preset() != null
                ? pending.preset().startingAbilities()
                : null;

        if (presetAbilities != null) {
            AbilityLockData updated = data;
            for (String abilityId : presetAbilities) {
                updated = updated.withUnlocked(abilityId);
            }
            if (!presetAbilities.isEmpty()) {
                player.sendSystemMessage(Component.literal(
                        "AbilityLock preset applied: " + pending.preset().displayName().getString()));
            } else {
                player.sendSystemMessage(Component.literal("AbilityLock: Hardcore preset - nothing unlocked yet."));
            }
            return updated;
        }

        List<Ability> eligible = AbilityChecker.getEligibleAbilities(data);
        if (!eligible.isEmpty()) {
            Ability chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
            player.sendSystemMessage(Component.literal("You've unlocked: " + chosen.displayName()));
            return data.withUnlocked(chosen.id());
        }

        return data;
    }
}