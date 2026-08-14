package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.task.Task;
import com.benbenlaw.abilitylock.task.TaskRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class TaskScreen extends Screen {

    private static final int BOX_WIDTH = 100;
    private static final int BOX_HEIGHT = 30;
    private static final int COL_GAP = 10;
    private static final int ROW_GAP = 10;
    private static final int TOP_MARGIN = 40;

    private static final int TEXT_PADDING = 3;
    private static final long SCROLL_PAUSE_MS = 800;
    private static final float SCROLL_SPEED_PX_PER_MS = 0.03f;

    private static final double MIN_SCALE = 0.4;
    private static final double MAX_SCALE = 2.5;
    private static final double ZOOM_STEP = 0.1;

    private final List<Task> visibleTasks = new ArrayList<>();
    private int gridSize = 1;

    private static double scale = 1.0;

    private double offsetX = 0;
    private double offsetY = 0;
    private boolean dragging = false;
    private double dragStartX;
    private double dragStartY;
    private double offsetStartX;
    private double offsetStartY;

    public TaskScreen() {
        super(Component.literal("Tasks"));
    }

    @Override
    protected void init() {
        super.init();
        buildLayout();
    }

    private void buildLayout() {
        visibleTasks.clear();

        if (this.minecraft == null || this.minecraft.player == null) return;
        TaskProgressData data = this.minecraft.player.getData(AbilityLockAttachments.TASK_PROGRESS);

        for (String taskId : data.gridTaskIds()) {
            TaskRegistry.get(taskId).ifPresent(visibleTasks::add);
        }

        gridSize = Math.max(1, (int) Math.ceil(Math.sqrt(Math.max(visibleTasks.size(), 1))));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        if (this.minecraft == null || this.minecraft.player == null) return;
        TaskProgressData data = this.minecraft.player.getData(AbilityLockAttachments.TASK_PROGRESS);

        int totalWidth = gridSize * (BOX_WIDTH + COL_GAP) - COL_GAP;
        int startX = (this.width - totalWidth) / 2;

        for (int i = 0; i < visibleTasks.size(); i++) {
            drawNode(graphics, visibleTasks.get(i), i, data, this.minecraft.player, startX, mouseX, mouseY);
        }

        Task hoveredTask = null;
        int boxW = (int) Math.round(BOX_WIDTH * scale);
        int boxH = (int) Math.round(BOX_HEIGHT * scale);
        for (int i = 0; i < visibleTasks.size(); i++) {
            int[] pos = boxTopLeft(i, startX);
            if (mouseX >= pos[0] && mouseX <= pos[0] + boxW && mouseY >= pos[1] && mouseY <= pos[1] + boxH) {
                hoveredTask = visibleTasks.get(i);
            }
        }

        if (hoveredTask != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(hoveredTask.displayName()));
            boolean hoveredComplete = data.isComplete(hoveredTask.id());
            boolean hoveredAttemptable = hoveredComplete || canAttempt(this.minecraft.player, hoveredTask);

            if (!hoveredAttemptable) {
                tooltip.add(Component.literal("Locked - requires ability:"));
                for (Identifier abilityId : hoveredTask.requiredAbilities()) {
                    if (!AbilityChecker.isUnlocked(this.minecraft.player, abilityId)) {
                        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
                        String label = abilityData != null ? abilityData.displayName() : abilityId.toString();
                        tooltip.add(Component.literal(" - " + label));
                    }
                }
            } else {
                tooltip.add(Component.literal(hoveredComplete ? "Complete" : "In Progress"));
                int hoveredTarget = hoveredTask.criterion().target();
                if (hoveredTarget > 1) {
                    int hoveredProgress = Math.min(data.progressOf(hoveredTask.id()), hoveredTarget);
                    tooltip.add(Component.literal(hoveredProgress + " / " + hoveredTarget));
                }
            }

            List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
            for (Component line : tooltip) {
                tooltipComponents.add(ClientTooltipComponent.create(line.getVisualOrderText()));
            }

            graphics.tooltip(this.font, tooltipComponents, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }

        if (data.isGridComplete()) {
            graphics.centeredText(this.font, Component.literal("Speedrun Complete!"), this.width / 2, 26, 0xFF55FF55);
        }
    }

    private boolean canAttempt(Player player, Task task) {
        if (!task.hasRequiredAbilities()) return true;
        for (Identifier abilityId : task.requiredAbilities()) {
            if (!AbilityChecker.isUnlocked(player, abilityId)) return false;
        }
        return true;
    }

    private int[] boxTopLeft(int index, int startX) {
        int col = index % gridSize;
        int row = index / gridSize;
        double worldX = startX + col * (BOX_WIDTH + COL_GAP);
        double worldY = TOP_MARGIN + row * (BOX_HEIGHT + ROW_GAP);
        int x = (int) Math.round(worldX * scale + offsetX);
        int y = (int) Math.round(worldY * scale + offsetY);
        return new int[]{x, y};
    }

    private void drawNode(GuiGraphicsExtractor graphics, Task task, int index, TaskProgressData data, Player player, int startX, int mouseX, int mouseY) {
        int[] pos = boxTopLeft(index, startX);
        int x = pos[0], y = pos[1];
        int boxW = (int) Math.round(BOX_WIDTH * scale);
        int boxH = (int) Math.round(BOX_HEIGHT * scale);

        boolean complete = data.isComplete(task.id());
        boolean attemptable = complete || canAttempt(player, task);
        boolean hovered = mouseX >= x && mouseX <= x + boxW && mouseY >= y && mouseY <= y + boxH;

        int fill;
        int border;
        if (!attemptable) {
            fill = 0xAA9E3A2C;
            border = hovered ? 0xFFAAAAAA : 0xFF6E2318;
        } else if (complete) {
            fill = 0xAA1D9E75;
            border = hovered ? 0xFFFFFFFF : 0xFF0F6E56;
        } else {
            fill = 0xAA2A6FB0;
            border = hovered ? 0xFFFFFFFF : 0xFF1B4F80;
        }

        graphics.fill(x, y, x + boxW, y + boxH, fill);
        graphics.fill(x, y, x + boxW, y + 1, border);
        graphics.fill(x, y + boxH - 1, x + boxW, y + boxH, border);
        graphics.fill(x, y, x + 1, y + boxH, border);
        graphics.fill(x + boxW - 1, y, x + boxW, y + boxH, border);

        int lineHeight = this.font.lineHeight;
        int textColor = complete ? 0xFFFFFFFF : (attemptable ? 0xFFAAAAAA : 0xFFD8A79E);
        int target = task.criterion().target();

        if (!attemptable) {
            int textY = y + (boxH - lineHeight) / 2;
            drawScrollingText(graphics, Component.nullToEmpty(task.displayName()), x, y, boxW, boxH, textY, textColor);
            return;
        }

        if (target > 1) {
            int lineGap = 3;
            int totalTextHeight = lineHeight * 2 + lineGap;
            int nameY = y + (boxH - totalTextHeight) / 2;
            int progressY = nameY + lineHeight + lineGap;
            int nameOverflow = Math.max(0, y - nameY);

            drawScrollingText(graphics, Component.nullToEmpty(task.displayName()), x, y, boxW, boxH, nameY, textColor, nameOverflow);

            int progress = Math.min(data.progressOf(task.id()), target);
            Component progressText = Component.literal(progress + " / " + target);
            graphics.centeredText(this.font, progressText, x + boxW / 2, progressY, textColor);
        } else {
            int textY = y + (boxH - lineHeight) / 2;
            drawScrollingText(graphics, Component.nullToEmpty(task.displayName()), x, y, boxW, boxH, textY, textColor);
        }
    }

    private void drawScrollingText(GuiGraphicsExtractor graphics, Component text, int boxX, int boxY, int boxWidth, int boxHeight, int textY, int color) {
        drawScrollingText(graphics, text, boxX, boxY, boxWidth, boxHeight, textY, color, 0);
    }

    private void drawScrollingText(GuiGraphicsExtractor graphics, Component text, int boxX, int boxY, int boxWidth, int boxHeight, int textY, int color, int topOverflow) {
        int textWidth = this.font.width(text);
        int available = boxWidth - TEXT_PADDING * 2;

        graphics.enableScissor(boxX, boxY - topOverflow, boxX + boxWidth, boxY + boxHeight);

        if (textWidth <= available) {
            graphics.centeredText(this.font, text, boxX + boxWidth / 2, textY, color);
            graphics.disableScissor();
            return;
        }

        int maxScroll = textWidth - available;
        long travelTime = (long) (maxScroll / SCROLL_SPEED_PX_PER_MS);
        long cycle = travelTime * 2 + SCROLL_PAUSE_MS * 2;
        long t = System.currentTimeMillis() % cycle;

        int scrollX;
        if (t < SCROLL_PAUSE_MS) {
            scrollX = 0;
        } else if (t < SCROLL_PAUSE_MS + travelTime) {
            scrollX = (int) ((t - SCROLL_PAUSE_MS) * SCROLL_SPEED_PX_PER_MS);
        } else if (t < SCROLL_PAUSE_MS * 2 + travelTime) {
            scrollX = maxScroll;
        } else {
            long backT = t - SCROLL_PAUSE_MS * 2 - travelTime;
            scrollX = maxScroll - (int) (backT * SCROLL_SPEED_PX_PER_MS);
        }

        int textX = boxX + TEXT_PADDING - scrollX;
        graphics.text(this.font, text, textX, textY, color);

        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == 0) {
            dragging = true;
            dragStartX = event.x();
            dragStartY = event.y();
            offsetStartX = offsetX;
            offsetStartY = offsetY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging && event.button() == 0) {
            offsetX = offsetStartX + (event.x() - dragStartX);
            offsetY = offsetStartY + (event.y() - dragStartY);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            dragging = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        double oldScale = scale;
        double newScale = oldScale + (scrollY > 0 ? ZOOM_STEP : -ZOOM_STEP);
        newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

        if (newScale != oldScale) {
            double factor = newScale / oldScale;
            offsetX = mouseX - (mouseX - offsetX) * factor;
            offsetY = mouseY - (mouseY - offsetY) * factor;
            scale = newScale;
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}