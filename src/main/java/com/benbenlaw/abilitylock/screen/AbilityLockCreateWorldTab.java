package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.util.AbilityLockWorldPreset;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

/**
 * The AbilityLock tab injected into vanilla's CreateWorldScreen via
 * CreateWorldScreenMixin. Holds preset + task-grid-size selection; read
 * back out by the mixin's onCreate() hook.
 */
public class AbilityLockCreateWorldTab extends GridLayoutTab {

    private static final Integer[] GRID_SIZES = {3, 4, 5};
    private static final Component TITLE = Component.literal("AbilityLock");

    private AbilityLockWorldPreset selectedPreset = AbilityLockWorldPreset.STANDARD;
    private int selectedGridSize = 5;

    public AbilityLockCreateWorldTab() {
        super(TITLE);

        GridLayout.RowHelper helper = this.layout.rowSpacing(8).createRowHelper(1);

        CycleButton<AbilityLockWorldPreset> presetButton = CycleButton.builder(AbilityLockWorldPreset::displayName, this.selectedPreset)
                .withValues(AbilityLockWorldPreset.values())
                .create(0, 0, 210, 20, Component.literal("Preset"),
                        (button, value) -> this.selectedPreset = value);
        helper.addChild(presetButton);

        CycleButton<Integer> gridSizeButton = CycleButton.<Integer>builder(
                        (size) -> Component.literal(size + "x" + size), this.selectedGridSize)
                .withValues(GRID_SIZES)
                .create(0, 0, 210, 20, Component.literal("Task Grid"),
                        (button, value) -> this.selectedGridSize = value);
        helper.addChild(gridSizeButton);
    }

    public AbilityLockWorldPreset getSelectedPreset() {
        return selectedPreset;
    }

    public int getSelectedGridSize() {
        return selectedGridSize;
    }
}