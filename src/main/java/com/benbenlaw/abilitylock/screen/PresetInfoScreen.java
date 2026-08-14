package com.benbenlaw.abilitylock.screen;

import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.presets.PresetData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class PresetInfoScreen extends Screen {

    private final PresetData data;
    private final @Nullable Screen previous;

    public PresetInfoScreen(Identifier presetId, PresetData data, @Nullable Screen previous) {
        super(Component.literal(data.displayName()));
        this.data = data;
        this.previous = previous;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        y = this.addLine(centerX, y,
                Component.literal(this.data.displayName()).withStyle(ChatFormatting.BOLD));
        y += 4;

        y = this.addLine(centerX, y,
                Component.literal(this.data.onDeathLoseWorld() ? "On Death: Lose World" : "On Death: Keep World"));
        y += 8;

        y = this.addLine(centerX, y,
                Component.literal("Starting Abilities:").withStyle(ChatFormatting.UNDERLINE));
        if (this.data.startingAbilities().isEmpty()) {
            y = this.addLine(centerX, y, Component.literal("(none)").withStyle(ChatFormatting.GRAY));
        } else {
            for (Identifier ability : this.data.startingAbilities()) {
                y = this.addLine(centerX, y, Component.literal(AbilityLoader.DATA.get(ability).displayName()));
            }
        }
        y += 4;

        y = this.addLine(centerX, y,
                Component.literal("Unlockable Abilities:").withStyle(ChatFormatting.UNDERLINE));
        if (this.data.unlockableAbilities().isEmpty()) {
            this.addLine(centerX, y, Component.literal("(all abilities)").withStyle(ChatFormatting.GRAY));
        } else {
            for (Identifier ability : this.data.unlockableAbilities()) {
                y = this.addLine(centerX, y, Component.literal(AbilityLoader.DATA.get(ability).displayName()));
            }
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose())
                .bounds(centerX - 50, this.height - 28, 100, 20)
                .build());
    }

    private int addLine(int centerX, int y, Component text) {
        this.addRenderableWidget(new StringWidget(centerX - 100, y, 200, 12, text, this.font));
        return y + 12;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.previous);
    }
}