package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.util.AbilityLockWorldPreset;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FileUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

public class AbilityLockCreateWorldScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.literal("Create AbilityLock World");

    private final Screen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 49, 60);

    private @Nullable EditBox nameEdit;
    private @Nullable EditBox seedEdit;

    private static final Integer[] GRID_SIZES = {3, 4, 5};

    private AbilityLockWorldPreset selectedPreset = AbilityLockWorldPreset.STANDARD;
    private int selectedGridSize = GRID_SIZES[0];

    public AbilityLockCreateWorldScreen(Screen lastScreen) {
        super(TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.title, this.font));

        LinearLayout body = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        body.defaultCellSetting().alignHorizontallyCenter();

        this.nameEdit = new EditBox(this.font, 0, 0, 200, 20, Component.literal("World Name"));
        this.nameEdit.setValue("New AbilityLock World");
        body.addChild(this.nameEdit);

        this.seedEdit = new EditBox(this.font, 0, 0, 200, 20, Component.literal("Seed"));
        this.seedEdit.setHint(Component.literal("Leave blank for random seed"));
        body.addChild(this.seedEdit);

        CycleButton<AbilityLockWorldPreset> presetButton = CycleButton.builder(AbilityLockWorldPreset::displayName, this.selectedPreset)
                .withValues(AbilityLockWorldPreset.values())
                .create(0, 0, 200, 20, Component.literal("Preset"),
                        (button, value) -> this.selectedPreset = value);
        body.addChild(presetButton);

        CycleButton<Integer> gridSizeButton = CycleButton.<Integer>builder(
                        (size) -> Component.literal(size + "x" + size), this.selectedGridSize)
                .withValues(GRID_SIZES)
                .create(0, 0, 200, 20, Component.literal("Task Grid"),
                        (button, value) -> this.selectedGridSize = value);
        body.addChild(gridSizeButton);

        GridLayout footer = this.layout.addToFooter(new GridLayout().columnSpacing(8));
        footer.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = footer.createRowHelper(2);
        rowHelper.addChild(Button.builder(Component.literal("Create"), (button) -> this.onCreate()).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL,
                (button) -> this.minecraft.setScreen(this.lastScreen)).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    private void onCreate() {
        if (this.nameEdit == null) {
            return;
        }

        String worldName = this.nameEdit.getValue().trim();
        if (worldName.isEmpty()) {
            worldName = "New AbilityLock World";
        }

        String seedText = this.seedEdit != null ? this.seedEdit.getValue().trim() : "";
        long seed = seedText.isEmpty() ? WorldOptions.randomSeed() : parseSeed(seedText);

        LevelSettings levelSettings = new LevelSettings(
                worldName,
                GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.NORMAL, this.selectedPreset.isOneLife(), false),
                false,
                WorldDataConfiguration.DEFAULT
        );

        WorldOptions worldOptions = new WorldOptions(seed, true, false);

        try {
            String resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), worldName, "");

            PendingAbilityLockWorldSettings.set(worldName, this.selectedPreset, this.selectedGridSize);

            this.minecraft.createWorldOpenFlows().createFreshLevel(
                    resultFolder,
                    levelSettings,
                    worldOptions,
                    WorldPresets::createNormalWorldDimensions,
                    this
            );
        } catch (IOException e) {
            LOGGER.error("Failed to create AbilityLock world", e);
        }
    }

    private static long parseSeed(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return text.hashCode();
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}