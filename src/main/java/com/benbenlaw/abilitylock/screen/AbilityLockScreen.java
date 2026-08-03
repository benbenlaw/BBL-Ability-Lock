package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityRegistry;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;

public class AbilityLockScreen extends Screen {

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

    private final Map<String, Integer> nodeSlot = new HashMap<>();
    private final Map<String, Integer> nodeDepth = new HashMap<>();
    private final Map<String, List<String>> childrenOf = new LinkedHashMap<>();
    private int leafCounter = 0;

    private static double scale = 1.0;

    private double offsetX = 0;
    private double offsetY = 0;
    private boolean dragging = false;
    private double dragStartX;
    private double dragStartY;
    private double offsetStartX;
    private double offsetStartY;

    public AbilityLockScreen() {
        super(Component.literal("Ability Unlocks"));
    }

    @Override
    protected void init() {
        super.init();
        buildLayout();
    }

    private void buildLayout() {
        nodeSlot.clear();
        nodeDepth.clear();
        childrenOf.clear();
        leafCounter = 0;

        for (Ability ability : AbilityRegistry.all().values()) {
            String parentKey = ability.hasParent() ? ability.parent() : "__root__";
            childrenOf.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(ability.id());
        }

        for (Ability ability : AbilityRegistry.all().values()) {
            nodeDepth.put(ability.id(), computeDepth(ability));
        }

        for (String root : childrenOf.getOrDefault("__root__", List.of())) {
            assignSlot(root);
        }
    }

    private int computeDepth(Ability ability) {
        int depth = 0;
        Ability current = ability;
        while (current.hasParent()) {
            depth++;
            current = AbilityRegistry.get(current.parent()).orElse(null);
            if (current == null) break;
        }
        return depth;
    }

    private int assignSlot(String id) {
        List<String> kids = childrenOf.getOrDefault(id, List.of());
        int slot;
        if (kids.isEmpty()) {
            slot = leafCounter++;
        } else {
            int sum = 0;
            for (String kid : kids) sum += assignSlot(kid);
            slot = Math.round(sum / (float) kids.size());
        }
        nodeSlot.put(id, slot);
        return slot;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        if (this.minecraft == null || this.minecraft.player == null) return;
        AbilityLockData data = this.minecraft.player.getData(AbilityLockAttachments.ABILITY_LOCK);

        int totalSlots = Math.max(leafCounter, 1);
        int totalWidth = totalSlots * (BOX_WIDTH + COL_GAP) - COL_GAP;
        int startX = (this.width - totalWidth) / 2;

        for (Ability ability : AbilityRegistry.all().values()) {
            if (ability.hasParent()) {
                drawConnector(graphics, ability.parent(), ability.id(), startX);
            }
        }

        for (Ability ability : AbilityRegistry.all().values()) {
            drawNode(graphics, ability, data, startX, mouseX, mouseY);
        }

        Ability hoveredAbility = null;
        int boxW = (int) Math.round(BOX_WIDTH * scale);
        int boxH = (int) Math.round(BOX_HEIGHT * scale);
        for (Ability ability : AbilityRegistry.all().values()) {
            int[] pos = boxTopLeft(ability.id(), startX);
            if (mouseX >= pos[0] && mouseX <= pos[0] + boxW && mouseY >= pos[1] && mouseY <= pos[1] + boxH) {
                hoveredAbility = ability;
            }
        }

        if (hoveredAbility != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(hoveredAbility.displayName()));
            tooltip.add(Component.literal(data.has(hoveredAbility.id()) ? "Unlocked" : "Locked"));

            List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
            for (Component line : tooltip) {
                tooltipComponents.add(ClientTooltipComponent.create(line.getVisualOrderText()));
            }

            graphics.tooltip(this.font, tooltipComponents, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }
    }

    private int[] boxTopLeft(String id, int startX) {
        int slot = nodeSlot.get(id);
        int depth = nodeDepth.get(id);
        double worldX = startX + slot * (BOX_WIDTH + COL_GAP);
        double worldY = TOP_MARGIN + depth * (BOX_HEIGHT + ROW_GAP);
        int x = (int) Math.round(worldX * scale + offsetX);
        int y = (int) Math.round(worldY * scale + offsetY);
        return new int[]{x, y};
    }

    private void drawNode(GuiGraphicsExtractor graphics, Ability ability, AbilityLockData data, int startX, int mouseX, int mouseY) {
        int[] pos = boxTopLeft(ability.id(), startX);
        int x = pos[0], y = pos[1];
        int boxW = (int) Math.round(BOX_WIDTH * scale);
        int boxH = (int) Math.round(BOX_HEIGHT * scale);

        boolean unlocked = data.has(ability.id());
        boolean nextPotential = !unlocked && (!ability.hasParent() || data.has(ability.parent()));
        boolean hovered = mouseX >= x && mouseX <= x + boxW && mouseY >= y && mouseY <= y + boxH;

        int fill;
        int border;
        if (unlocked) {
            fill = 0xAA1D9E75;
            border = hovered ? 0xFFFFFFFF : 0xFF0F6E56;
        } else if (nextPotential) {
            fill = 0xAA2A6FB0;
            border = hovered ? 0xFFFFFFFF : 0xFF1B4F80;
        } else {
            fill = 0xAA9E3A2C;
            border = hovered ? 0xFFAAAAAA : 0xFF6E2318;
        }

        graphics.fill(x, y, x + boxW, y + boxH, fill);
        graphics.fill(x, y, x + boxW, y + 1, border);
        graphics.fill(x, y + boxH - 1, x + boxW, y + boxH, border);
        graphics.fill(x, y, x + 1, y + boxH, border);
        graphics.fill(x + boxW - 1, y, x + boxW, y + boxH, border);

        int textColor = unlocked ? 0xFFFFFFFF : 0xFFAAAAAA;
        int textY = y + (boxH - this.font.lineHeight) / 2;
        drawScrollingText(graphics, Component.nullToEmpty(ability.displayName()), x, y, boxW, boxH, textY, textColor);
    }

    private void drawScrollingText(GuiGraphicsExtractor graphics, Component text, int boxX, int boxY, int boxWidth, int boxHeight, int textY, int color) {
        int textWidth = this.font.width(text);
        int available = boxWidth - TEXT_PADDING * 2;

        graphics.enableScissor(boxX, boxY, boxX + boxWidth, boxY + boxHeight);

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

    private void drawConnector(GuiGraphicsExtractor graphics, String parentId, String childId, int startX) {
        int[] parentPos = boxTopLeft(parentId, startX);
        int[] childPos = boxTopLeft(childId, startX);
        int boxW = (int) Math.round(BOX_WIDTH * scale);
        int boxH = (int) Math.round(BOX_HEIGHT * scale);

        int parentCenterX = parentPos[0] + boxW / 2;
        int parentBottomY = parentPos[1] + boxH;
        int childCenterX = childPos[0] + boxW / 2;
        int childTopY = childPos[1];
        int midY = parentBottomY + (childTopY - parentBottomY) / 2;
        int color = 0xFF73726C;

        graphics.fill(parentCenterX, parentBottomY, parentCenterX + 1, midY, color);
        int lowX = Math.min(parentCenterX, childCenterX);
        int highX = Math.max(parentCenterX, childCenterX);
        graphics.fill(lowX, midY, highX + 1, midY + 1, color);
        graphics.fill(childCenterX, midY, childCenterX + 1, childTopY, color);
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