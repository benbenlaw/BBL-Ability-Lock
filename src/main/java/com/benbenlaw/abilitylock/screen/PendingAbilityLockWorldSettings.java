package com.benbenlaw.abilitylock.screen;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class PendingAbilityLockWorldSettings {

    public static final int DEFAULT_GRID_WIDTH = 5;
    public static final int DEFAULT_GRID_HEIGHT = 5;

    private static @Nullable String pendingLevelName;
    private static @Nullable Identifier pendingPresetId;
    private static int pendingGridWidth = DEFAULT_GRID_WIDTH;
    private static int pendingGridHeight = DEFAULT_GRID_HEIGHT;

    private PendingAbilityLockWorldSettings() {
    }

    public static void set(String levelName, @Nullable Identifier presetId, int gridWidth, int gridHeight) {
        pendingLevelName = levelName;
        pendingPresetId = presetId;
        pendingGridWidth = gridWidth;
        pendingGridHeight = gridHeight;
    }

    public static @Nullable Result consumeIfMatches(@Nullable String levelName) {
        if (pendingLevelName == null || levelName == null || !pendingLevelName.equals(levelName)) {
            return null;
        }

        Result result = new Result(pendingPresetId, pendingGridWidth, pendingGridHeight);
        pendingLevelName = null;
        pendingPresetId = null;
        pendingGridWidth = DEFAULT_GRID_WIDTH;
        pendingGridHeight = DEFAULT_GRID_HEIGHT;
        return result;
    }

    public record Result(@Nullable Identifier presetId, int gridWidth, int gridHeight) {
    }
}