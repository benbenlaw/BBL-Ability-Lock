package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.util.AbilityLockWorldPreset;
import org.jspecify.annotations.Nullable;

public final class PendingAbilityLockWorldSettings {

    public static final int DEFAULT_GRID_SIZE = 5;

    private static @Nullable String pendingLevelName;
    private static @Nullable AbilityLockWorldPreset pendingPreset;
    private static int pendingGridSize = DEFAULT_GRID_SIZE;

    private static boolean pendingTabRequest = false;

    private PendingAbilityLockWorldSettings() {
    }

    public static void set(String levelName, AbilityLockWorldPreset preset, int gridSize) {
        pendingLevelName = levelName;
        pendingPreset = preset;
        pendingGridSize = gridSize;
    }

    public static @Nullable Result consumeIfMatches(@Nullable String levelName) {
        if (pendingLevelName == null || levelName == null || !pendingLevelName.equals(levelName)) {
            return null;
        }

        Result result = new Result(pendingPreset, pendingGridSize);
        pendingLevelName = null;
        pendingPreset = null;
        pendingGridSize = DEFAULT_GRID_SIZE;
        return result;
    }

    public static void requestTabOnNextScreen() {
        pendingTabRequest = true;
    }

    public static boolean consumeTabRequest() {
        boolean was = pendingTabRequest;
        pendingTabRequest = false;
        return was;
    }

    public record Result(AbilityLockWorldPreset preset, int gridSize) {
    }
}