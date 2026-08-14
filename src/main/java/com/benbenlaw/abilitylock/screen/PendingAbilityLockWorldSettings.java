package com.benbenlaw.abilitylock.screen;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class PendingAbilityLockWorldSettings {

    public static final int DEFAULT_GRID_SIZE = 5;

    private static @Nullable String pendingLevelName;
    private static @Nullable Identifier pendingPresetId;
    private static int pendingGridSize = DEFAULT_GRID_SIZE;

    private PendingAbilityLockWorldSettings() {
    }

    public static void set(String levelName, @Nullable Identifier presetId, int gridSize) {
        pendingLevelName = levelName;
        pendingPresetId = presetId;
        pendingGridSize = gridSize;
    }

    public static @Nullable Result consumeIfMatches(@Nullable String levelName) {
        if (pendingLevelName == null || levelName == null || !pendingLevelName.equals(levelName)) {
            return null;
        }

        Result result = new Result(pendingPresetId, pendingGridSize);
        pendingLevelName = null;
        pendingPresetId = null;
        pendingGridSize = DEFAULT_GRID_SIZE;
        return result;
    }

    public record Result(@Nullable Identifier presetId, int gridSize) {
    }
}