package com.benbenlaw.abilitylock.ability;

import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AbilityChecker {

    public static boolean isUnlocked(Player player, String abilityId) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        return isUnlockedRecursive(data, abilityId);
    }

    private static boolean isUnlockedRecursive(AbilityLockData data, String abilityId) {
        if (!data.has(abilityId)) return false;

        Optional<Ability> ability = AbilityRegistry.get(abilityId);
        if (ability.isEmpty()) return false;

        Ability a = ability.get();
        if (!a.hasParent()) return true;

        return isUnlockedRecursive(data, a.parent());
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

    public static Optional<Ability> grantRandomEligible(ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Ability> eligible = getEligibleAbilities(data);
        if (eligible.isEmpty()) return Optional.empty();

        Ability chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
        grant(player, data, chosen);
        return Optional.of(chosen);
    }

    public static void grantSpecific(ServerPlayer player, Ability ability) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        grant(player, data, ability);
    }

    private static void grant(ServerPlayer player, AbilityLockData data, Ability ability) {
        Set<String> updated = new HashSet<>(data.unlockedAbilities());
        updated.add(ability.id());
        AbilityLockData newData = new AbilityLockData(updated);
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);
        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));
    }
}