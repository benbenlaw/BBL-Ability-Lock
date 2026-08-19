package com.benbenlaw.abilitylock.ability;

import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AbilityChecker {

    public static boolean isUnlocked(Player player, Identifier abilityId) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        return isUnlockedRecursive(data, abilityId, new HashSet<>());
    }

    private static boolean isUnlockedRecursive(AbilityLockData data, Identifier abilityId, Set<Identifier> visiting) {
        if (!data.has(abilityId)) return false;
        if (!visiting.add(abilityId)) return true;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) return true;

        for (Identifier parent : abilityData.parents()) {
            if (!isUnlockedRecursive(data, parent, visiting)) return false;
        }
        return true;
    }

    public static boolean hasAll(Player player, Collection<Identifier> abilityIds) {
        for (Identifier id : abilityIds) {
            if (!isUnlocked(player, id)) return false;
        }
        return true;
    }

    public static List<Identifier> getEligibleAbilities(AbilityLockData data) {
        List<Identifier> eligible = new ArrayList<>();

        Set<Identifier> allowedByPreset = allowedByPreset(data);

        for (Identifier id : AbilityLoader.DATA.keySet()) {
            if (data.has(id)) continue;
            if (allowedByPreset != null && !allowedByPreset.contains(id)) continue;

            AbilityData abilityData = AbilityLoader.DATA.get(id);
            if (!data.unlockedAbilities().containsAll(abilityData.parents())) continue;

            eligible.add(id);
        }

        return eligible;
    }

    private static @Nullable Set<Identifier> allowedByPreset(AbilityLockData data) {
        if (data.presetId().isEmpty()) return null;

        PresetData preset = PresetLoader.DATA.get(data.presetId().get());
        if (preset == null || !preset.restrictsUnlockable()) return null;

        return new HashSet<>(preset.unlockableAbilities());
    }

    public static Optional<Identifier> grantRandomEligible(ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Identifier> eligible = getEligibleAbilities(data);
        if (eligible.isEmpty()) return Optional.empty();

        Identifier chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
        grantSpecific(player, chosen);
        return Optional.of(chosen);
    }

    public static void grantSpecific(ServerPlayer player, Identifier abilityId) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (data.has(abilityId)) return;

        AbilityLockData newData = data.withUnlocked(abilityId);
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);
        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));
    }

    public static @Nullable Identifier findUnreachableAncestor(Player player, Identifier abilityId) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<Identifier> allowedByPreset = allowedByPreset(data);
        return findUnreachableAncestorRecursive(abilityId, allowedByPreset, new HashSet<>());
    }

    private static @Nullable Identifier findUnreachableAncestorRecursive(
            Identifier abilityId, @Nullable Set<Identifier> allowedByPreset, Set<Identifier> visiting) {

        if (!visiting.add(abilityId)) return null; // cycle guard, same idiom as isUnlockedRecursive

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) return abilityId;

        if (allowedByPreset != null && !allowedByPreset.contains(abilityId)) return abilityId;

        for (Identifier parent : abilityData.parents()) {
            Identifier broken = findUnreachableAncestorRecursive(parent, allowedByPreset, visiting);
            if (broken != null) return broken;
        }
        return null;
    }

    public static Identifier pickPreferredAbility(List<Identifier> eligible, Set<Identifier> relevantAbilities, RandomSource random) {
        List<Identifier> preferred = new ArrayList<>();
        for (Identifier id : eligible) {
            if (relevantAbilities.contains(id)) preferred.add(id);
        }
        List<Identifier> pool = preferred.isEmpty() ? eligible : preferred;
        return pool.get(random.nextInt(pool.size()));
    }
}