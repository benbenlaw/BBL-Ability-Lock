package com.benbenlaw.abilitylock.mixin.client;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import com.benbenlaw.abilitylock.screen.AbilityLockGameTabAccess;
import com.benbenlaw.abilitylock.screen.PresetInfoScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab")
public abstract class CreateWorldScreenGameTabMixin implements AbilityLockGameTabAccess {

    @Unique
    private static final Integer[] abilityLock$GRID_DIMENSIONS = {3, 4, 5, 6, 7};

    @Unique
    private @Nullable Identifier abilityLock$selectedPreset;

    @Unique
    private int abilityLock$selectedGridWidth = 5;

    @Unique
    private int abilityLock$selectedGridHeight = 5;

    @Unique
    private @Nullable CycleButton<Integer> abilityLock$gridWidthButton;

    @Unique
    private @Nullable CycleButton<Integer> abilityLock$gridHeightButton;

    @ModifyVariable(method = "<init>", at = @At("TAIL"), name = "helper")
    private GridLayout.RowHelper abilityLock$appendWidgets(GridLayout.RowHelper helper) {
        if (!ClientConfig.showAbilityLockWorldCreation.get()) {
            return helper;
        }

        StringWidget header = new StringWidget(
                Component.literal("AbilityLock").withStyle(ChatFormatting.BOLD),
                Minecraft.getInstance().font
        );
        helper.addChild(header, helper.newCellSettings().alignHorizontallyCenter());

        List<Identifier> knownPresets = abilityLock$resolveKnownPresets();
        this.abilityLock$selectedPreset = knownPresets.getFirst();

        LinearLayout presetRow = LinearLayout.horizontal().spacing(6);

        assert this.abilityLock$selectedPreset != null;
        CycleButton<Identifier> presetButton = presetRow.addChild(CycleButton.builder(
                        this::abilityLock$labelFor, this.abilityLock$selectedPreset)
                .withValues(knownPresets)
                .create(0, 0, 184, 20, Component.literal("AbilityLock Preset"),
                        (button, value) -> {
                            this.abilityLock$selectedPreset = value;
                            this.abilityLock$applyGridSizeLock(value);
                        }));

        presetRow.addChild(Button.builder(Component.literal("i"),
                        (b) -> this.abilityLock$openPresetInfo())
                .width(20)
                .build());

        helper.addChild(presetRow);

        this.abilityLock$gridWidthButton = CycleButton.builder(
                        (size) -> Component.literal(size + " wide"), this.abilityLock$selectedGridWidth)
                .withValues(abilityLock$GRID_DIMENSIONS)
                .create(0, 0, 210, 20, Component.literal("AbilityLock Grid Width"),
                        (button, value) -> this.abilityLock$selectedGridWidth = value);
        helper.addChild(this.abilityLock$gridWidthButton);

        this.abilityLock$gridHeightButton = CycleButton.builder(
                        (size) -> Component.literal(size + " tall"), this.abilityLock$selectedGridHeight)
                .withValues(abilityLock$GRID_DIMENSIONS)
                .create(0, 0, 210, 20, Component.literal("AbilityLock Grid Height"),
                        (button, value) -> this.abilityLock$selectedGridHeight = value);
        helper.addChild(this.abilityLock$gridHeightButton);

        this.abilityLock$applyGridSizeLock(this.abilityLock$selectedPreset);

        return helper;
    }

    @Unique
    private void abilityLock$applyGridSizeLock(@Nullable Identifier presetId) {
        if (this.abilityLock$gridWidthButton == null || this.abilityLock$gridHeightButton == null) return;

        PresetData data = presetId != null ? PresetLoader.DATA.get(presetId) : null;
        Optional<Integer> locked = data != null ? data.defaultGridSize() : Optional.empty();

        if (locked.isPresent()) {
            int size = locked.get();
            this.abilityLock$selectedGridWidth = size;
            this.abilityLock$selectedGridHeight = size;
            this.abilityLock$gridWidthButton.setValue(size);
            this.abilityLock$gridHeightButton.setValue(size);
            this.abilityLock$gridWidthButton.active = false;
            this.abilityLock$gridHeightButton.active = false;
        } else {
            this.abilityLock$gridWidthButton.active = true;
            this.abilityLock$gridHeightButton.active = true;
        }
    }

    @Unique
    private void abilityLock$openPresetInfo() {
        PresetData data = PresetLoader.DATA.get(this.abilityLock$selectedPreset);
        if (data != null) {
            Screen current = Minecraft.getInstance().screen;
            Minecraft.getInstance().setScreen(new PresetInfoScreen(this.abilityLock$selectedPreset, data, current));
        }
    }

    @Unique
    private Component abilityLock$labelFor(Identifier presetId) {
        PresetData data = PresetLoader.DATA.get(presetId);
        if (data != null) {
            return Component.literal(data.displayName());
        }
        return Component.literal(presetId.toString());
    }

    @Unique
    private static List<Identifier> abilityLock$resolveKnownPresets() {
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

    @Override
    public @Nullable Identifier abilityLock$getSelectedPreset() {
        return this.abilityLock$selectedPreset;
    }

    @Override
    public int abilityLock$getSelectedGridWidth() {
        return this.abilityLock$selectedGridWidth;
    }

    @Override
    public int abilityLock$getSelectedGridHeight() {
        return this.abilityLock$selectedGridHeight;
    }
}