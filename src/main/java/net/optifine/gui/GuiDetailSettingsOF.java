package net.optifine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

public class GuiDetailSettingsOF extends GuiScreenOF
{
    private Screen prevScreen;
    private Options settings;
    private static Option[] enumOptions = new Option[] {Option.CLOUDS, Option.CLOUD_HEIGHT, Option.TREES, Option.RAIN, Option.SKY, Option.STARS, Option.SUN_MOON, Option.SHOW_CAPES, Option.FOG_FANCY, Option.FOG_START, Option.TRANSLUCENT_BLOCKS, Option.HELD_ITEM_TOOLTIPS, Option.DROPPED_ITEMS, Option.SWAMP_COLORS, Option.VIGNETTE, Option.ALTERNATE_BLOCKS, Option.ENTITY_DISTANCE_SCALING, Option.BIOME_BLEND_RADIUS};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiDetailSettingsOF(Screen guiscreen, Options gamesettings)
    {
        super(new TextComponent(I18n.m_118938_("of.options.detailsTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    public void init()
    {
        this.clearWidgets();

        for (int i = 0; i < enumOptions.length; ++i)
        {
            Option option = enumOptions[i];
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 + 21 * (i / 2) - 12;
            AbstractWidget abstractwidget = this.addRenderableWidget(option.createButton(this.minecraft.options, j, k, 150));
        }

        this.addRenderableWidget(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.m_118938_("gui.done")));
    }

    protected void actionPerformed(AbstractWidget guiElement)
    {
        if (guiElement instanceof GuiButtonOF)
        {
            GuiButtonOF guibuttonof = (GuiButtonOF)guiElement;

            if (guibuttonof.active)
            {
                if (guibuttonof.id == 200)
                {
                    this.minecraft.options.save();
                    this.minecraft.setScreen(this.prevScreen);
                }
            }
        }
    }

    public void removed()
    {
        this.minecraft.options.save();
        super.removed();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.minecraft.font, this.title, this.width / 2, 15, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.tooltipManager.drawTooltips(pMatrixStack, pMouseX, pMouseY, this.getButtonList());
    }
}
