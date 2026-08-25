package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.task.TaskLoader;
import com.benbenlaw.abilitylock.task.TaskType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PresetInfoScreen extends Screen {

    private static final int CONTENT_TOP = 40;
    private static final int CONTENT_BOTTOM_MARGIN = 36;
    private static final int LINE_HEIGHT = 12;
    private static final double SCROLL_SPEED = 12;

    private final PresetData data;
    private final @Nullable Screen previous;
    private final List<Component> lines = new ArrayList<>();
    private double scrollOffset = 0;

    public PresetInfoScreen(Identifier presetId, PresetData data, @Nullable Screen previous) {
        super(Component.literal(data.displayName()));
        this.data = data;
        this.previous = previous;
    }

    @Override
    protected void init() {
        lines.clear();
        scrollOffset = 0;

        lines.add(Component.literal(this.data.displayName()).withStyle(ChatFormatting.BOLD));
        lines.add(Component.empty());

        lines.add(Component.literal(this.data.onDeathLoseWorld() ? "On Death: Lose World" : "On Death: Keep World"));

        lines.add(this.data.defaultGridSize().isPresent()
                ? Component.literal("Grid Size: Locked to " + this.data.defaultGridSize().get() + "x" + this.data.defaultGridSize().get())
                : Component.literal("Grid Size: Player Choice"));

        lines.add(this.data.immediateTaskPercentage().isPresent()
                ? Component.literal("Tasks Available Immediately: " + this.data.immediateTaskPercentage().get() + "%")
                : Component.literal("Tasks Available Immediately: Server Default"));

        lines.add(this.data.bonusAbilityPercentage() > 0
                ? Component.literal("Bonus Ability Chance: " + this.data.bonusAbilityPercentage() + "% per task")
                : Component.literal("Bonus Ability Chance: Disabled"));
        lines.add(Component.empty());

        lines.add(Component.literal("Starting Abilities:").withStyle(ChatFormatting.UNDERLINE));
        if (this.data.startingAbilities().isEmpty()) {
            lines.add(Component.literal("(none)").withStyle(ChatFormatting.GRAY));
        } else {
            for (Identifier ability : this.data.startingAbilities()) {
                lines.add(Component.literal(abilityDisplayName(ability)));
            }
        }
        lines.add(Component.empty());

        lines.add(Component.literal("Unlockable Abilities:").withStyle(ChatFormatting.UNDERLINE));
        if (this.data.unlockableAbilities().isEmpty()) {
            lines.add(Component.literal("(all abilities)").withStyle(ChatFormatting.GRAY));
        } else {
            for (Identifier ability : this.data.unlockableAbilities()) {
                lines.add(Component.literal(abilityDisplayName(ability)));
            }
        }
        lines.add(Component.empty());

        lines.add(Component.literal("Valid Tasks:").withStyle(ChatFormatting.UNDERLINE));
        if (this.data.validTasks().isEmpty()) {
            lines.add(Component.literal("(all tasks)").withStyle(ChatFormatting.GRAY));
        } else {
            for (Identifier taskId : this.data.validTasks()) {
                lines.add(Component.literal(taskDisplayName(taskId)));
            }
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int centerX = this.width / 2;
        int contentBottom = this.height - CONTENT_BOTTOM_MARGIN;

        graphics.enableScissor(0, CONTENT_TOP, this.width, contentBottom);

        int y = CONTENT_TOP - (int) scrollOffset;
        for (Component line : lines) {
            if (y + LINE_HEIGHT >= CONTENT_TOP && y <= contentBottom) {
                graphics.centeredText(this.font, line, centerX, y, 0xFFFFFFFF);
            }
            y += LINE_HEIGHT;
        }

        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int contentBottom = this.height - CONTENT_BOTTOM_MARGIN;
        int visibleHeight = contentBottom - CONTENT_TOP;
        int totalHeight = lines.size() * LINE_HEIGHT;
        double maxScroll = Math.max(0, totalHeight - visibleHeight);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_SPEED));
        return true;
    }

    private static String abilityDisplayName(Identifier abilityId) {
        AbilityData data = AbilityLoader.DATA.get(abilityId);
        return data != null ? data.displayName() : prettify(abilityId);
    }

    private static String taskDisplayName(Identifier taskId) {
        TaskType task = TaskLoader.TASKS.get(taskId);
        return task != null ? task.getData().displayName() : prettify(taskId);
    }

    private static String prettify(Identifier id) {
        return capitalize(id.getNamespace()) + " - " + titleCase(id.getPath().replace('_', ' '));
    }

    private static String titleCase(String s) {
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(capitalize(words[i]));
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.previous);
    }
}