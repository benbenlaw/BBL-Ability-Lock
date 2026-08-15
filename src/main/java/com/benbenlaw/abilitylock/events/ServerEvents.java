package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.command.AbilityLockCommand;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import com.benbenlaw.abilitylock.task.TaskLoader;
import com.benbenlaw.abilitylock.task.TaskManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(AbilityLock.identifier("ability"), new AbilityLoader());
        event.addListener(AbilityLock.identifier("preset"), new PresetLoader());
        event.addListener(AbilityLock.identifier("task"), new TaskLoader());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AbilityLockCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (data.eliminated()) return;

        PresetData preset = resolvePreset(data);
        if (preset == null || !preset.onDeathLoseWorld()) return;

        player.setData(AbilityLockAttachments.ABILITY_LOCK, data.withEliminated(true));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (data.eliminated()) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String levelName = player.level().getServer().getWorldData().getLevelName();
        PendingAbilityLockWorldSettings.Result pending = PendingAbilityLockWorldSettings.consumeIfMatches(levelName);

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.eliminated()) {
            player.setGameMode(GameType.SPECTATOR);
        }

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

    private static @Nullable PresetData resolvePreset(AbilityLockData data) {
        return data.presetId().map(PresetLoader.DATA::get).orElse(null);
    }

    private static AbilityLockData applyFirstLoginAbilities(
            ServerPlayer player, AbilityLockData data, PendingAbilityLockWorldSettings.@Nullable Result pending) {

        Identifier presetId = pending != null ? pending.presetId() : null;
        PresetData preset = presetId != null ? PresetLoader.DATA.get(presetId) : null;

        if (preset != null) {
            AbilityLockData updated = data.withPreset(presetId);
            for (Identifier abilityId : preset.startingAbilities()) {
                updated = updated.withUnlocked(abilityId);
            }

            if (!preset.startingAbilities().isEmpty()) {
                player.sendSystemMessage(Component.literal("AbilityLock preset applied: " + preset.displayName()));
            } else {
                player.sendSystemMessage(Component.literal("AbilityLock: " + preset.displayName() + " - nothing unlocked yet."));
            }
            return updated;
        }

        List<Identifier> eligible = AbilityChecker.getEligibleAbilities(data);
        if (!eligible.isEmpty()) {
            Identifier chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
            AbilityData chosenData = AbilityLoader.DATA.get(chosen);
            String name = chosenData != null ? chosenData.displayName() : chosen.toString();

            player.sendSystemMessage(Component.literal("You've unlocked: " + name));
            return data.withUnlocked(chosen);
        }

        return data;
    }
}