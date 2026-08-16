package com.benbenlaw.abilitylock.ability.client;

import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class AbilityUnlockToastManager {

    public record Entry(String displayName, boolean bonus, long shownAtMillis) {}

    private static final long DISPLAY_MS = 4000;
    private static final Deque<Entry> QUEUE = new ArrayDeque<>();
    private static @Nullable Entry current;

    public static void show(Identifier abilityId, boolean bonus) {
        AbilityData data = AbilityLoader.DATA.get(abilityId);
        String name = data != null ? data.displayName() : abilityId.toString();
        QUEUE.add(new Entry(name, bonus, 0));

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(bonus ? SoundEvents.PLAYER_LEVELUP : SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F)
        );
    }

    public static @Nullable Entry current() {
        long now = System.currentTimeMillis();

        if (current != null && now - current.shownAtMillis() > DISPLAY_MS) {
            current = null;
        }

        if (current == null && !QUEUE.isEmpty()) {
            Entry next = QUEUE.poll();
            current = new Entry(next.displayName(), next.bonus(), now);
        }

        return current;
    }
}