
//* this class is for handling the custom about screen like when u click the copyrigted text on title screen

package net.blackburn.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.blackburn.Const;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderOptions;

public class AboutScreen extends GuiScreenOF
{
    private Screen prevScreen;
    private Options settings;
    private static Option[] enumOptions = new Option[] {};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    private String splash;
    public AboutScreen(Screen guiscreen, Options gamesettings)
    {
        super(new TextComponent(I18n.m_118938_("blackburn.about")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    public void init()
    {
        this.clearWidgets();

        splash = this.minecraft.getSplashManager().getSplash();
        // UWU
        for (int i = 0; i < enumOptions.length; ++i)
        {
            Option option = enumOptions[i];
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 + 21 * (i / 2) - 12;
            AbstractWidget abstractwidget = this.addRenderableWidget(option.createButton(this.minecraft.options, j, k, 150));
        }

      
    }

    protected void actionPerformed(AbstractWidget guiElement)
    {
        
        
    }

    public void removed()
    {
        this.minecraft.options.save();
        super.removed();
    }

    public void render(PoseStack pMatrixStack, int x, int y, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.minecraft.font, this.title, this.width / 2, 15, 16777215);

        this.drawCenteredString(pMatrixStack, this.font, splash,this.width/2 ,30, 16776960);

        this.drawCenteredString(pMatrixStack, fontRenderer,"§6Blackburn Software Stats UwU",this.width/2 ,140, 16777215);
        this.drawCenteredString(pMatrixStack, fontRenderer,"§6Version:"+"§r "+Const.VERSION+Const.RELEASE,this.width/2 ,150, 16777215);
        this.drawCenteredString(pMatrixStack, fontRenderer,"§6Build:"+"§r "+Const.RELEASE,this.width/2 ,160, 16777215);
        this.drawCenteredString(pMatrixStack, fontRenderer,"§6BuildDate:"+"§r "+Const.Date,this.width/2 ,170, 16777215);
        
        

        super.render(pMatrixStack, x, y, pPartialTicks);
    }
}