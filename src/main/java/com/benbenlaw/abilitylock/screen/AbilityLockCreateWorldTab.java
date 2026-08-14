package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.config.ClientConfig;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AbilityLockCreateWorldTab extends GridLayoutTab {

    private static final Integer[] GRID_SIZES = {3, 4, 5};
    private static final Component TITLE = Component.literal("AbilityLock");

    private Identifier selectedPreset;
    private int selectedGridSize = 5;

    public AbilityLockCreateWorldTab() {
        super(TITLE);

        List<Identifier> knownPresets = resolveKnownPresets();
        this.selectedPreset = knownPresets.getFirst();

        GridLayout.RowHelper helper = this.layout.rowSpacing(8).createRowHelper(1);

        CycleButton<Identifier> presetButton = CycleButton.builder(
                        this::labelFor, this.selectedPreset)
                .withValues(knownPresets)
                .create(0, 0, 210, 20, Component.literal("Preset"),
                        (button, value) -> this.selectedPreset = value);
        helper.addChild(presetButton);

        CycleButton<Integer> gridSizeButton = CycleButton.builder(
                        (size) -> Component.literal(size + "x" + size), this.selectedGridSize)
                .withValues(GRID_SIZES)
                .create(0, 0, 210, 20, Component.literal("Task Grid"),
                        (button, value) -> this.selectedGridSize = value);
        helper.addChild(gridSizeButton);
    }

    private static List<Identifier> resolveKnownPresets() {
        List<Identifier> result = new ArrayList<>();

        for (String raw : ClientConfig.knownWorldPresets.get()) {
            try {
                result.add(Identifier.parse(raw));
            } catch (Exception ignored) {
            }
        }

        if (result.isEmpty()) {
            result.add(AbilityLock.identifier("standard"));
        }

        return result;
    }

    private Component labelFor(Identifier presetId) {
        return Component.literal(presetId.toString());
    }

    public Identifier getSelectedPreset() {
        return selectedPreset;
    }

    public int getSelectedGridSize() {
        return selectedGridSize;
    }
}