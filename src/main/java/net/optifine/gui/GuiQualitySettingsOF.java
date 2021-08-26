package net.optifine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

public class GuiQualitySettingsOF extends GuiScreenOF
{
    private Screen prevScreen;
    private Options settings;
    private static Option[] enumOptions = new Option[] {Option.MIPMAP_LEVELS, Option.MIPMAP_TYPE, Option.AF_LEVEL, Option.AA_LEVEL, Option.EMISSIVE_TEXTURES, Option.RANDOM_ENTITIES, Option.BETTER_GRASS, Option.BETTER_SNOW, Option.CUSTOM_FONTS, Option.CUSTOM_COLORS, Option.CONNECTED_TEXTURES, Option.NATURAL_TEXTURES, Option.CUSTOM_SKY, Option.CUSTOM_ITEMS, Option.CUSTOM_ENTITY_MODELS, Option.CUSTOM_GUIS, Option.SCREEN_EFFECTS_SCALE, Option.FOV_EFFECTS_SCALE};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiQualitySettingsOF(Screen guiscreen, Options gamesettings)
    {
        super(new TextComponent(I18n.m_118938_("of.options.qualityTitle")));
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
        drawCenteredString(pMatrixStack, this.fontRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.tooltipManager.drawTooltips(pMatrixStack, pMouseX, pMouseY, this.getButtonList());
    }
}
