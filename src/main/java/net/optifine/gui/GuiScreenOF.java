package net.optifine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.optifine.util.GuiUtils;

public class GuiScreenOF extends Screen
{
    protected Font fontRenderer = Minecraft.getInstance().font;
    protected boolean mousePressed = false;

    public GuiScreenOF(Component p_96550_)
    {
        super(p_96550_);
    }

    public List<AbstractWidget> getButtonList()
    {
        List<AbstractWidget> list = new ArrayList<>();

        for (GuiEventListener guieventlistener : this.children())
        {
            if (guieventlistener instanceof AbstractWidget)
            {
                list.add((AbstractWidget)guieventlistener);
            }
        }

        return list;
    }

    protected void actionPerformed(AbstractWidget button)
    {
    }

    protected void actionPerformedRightClick(AbstractWidget button)
    {
    }

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        boolean flag = super.mouseClicked(pMouseX, p_94738_, pMouseY);
        this.mousePressed = true;
        AbstractWidget abstractwidget = getSelectedButton((int)pMouseX, (int)p_94738_, this.getButtonList());

        if (abstractwidget != null && abstractwidget.active)
        {
            if (pMouseY == 1 && abstractwidget instanceof IOptionControl)
            {
                IOptionControl ioptioncontrol = (IOptionControl)abstractwidget;

                if (ioptioncontrol.getControlOption() == Option.GUI_SCALE)
                {
                    abstractwidget.playDownSound(super.minecraft.getSoundManager());
                }
            }

            if (pMouseY == 0)
            {
                this.actionPerformed(abstractwidget);
            }
            else if (pMouseY == 1)
            {
                this.actionPerformedRightClick(abstractwidget);
            }

            return true;
        }
        else
        {
            return flag;
        }
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        if (!this.mousePressed)
        {
            return false;
        }
        else
        {
            this.mousePressed = false;
            this.setDragging(false);
            return this.getFocused() != null && this.getFocused().mouseReleased(pMouseX, p_94754_, pMouseY) ? true : super.mouseReleased(pMouseX, p_94754_, pMouseY);
        }
    }

    public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
    {
        return !this.mousePressed ? false : super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton);
    }

    public static AbstractWidget getSelectedButton(int x, int y, List<AbstractWidget> listButtons)
    {
        for (int i = 0; i < listButtons.size(); ++i)
        {
            AbstractWidget abstractwidget = listButtons.get(i);

            if (abstractwidget.visible)
            {
                int j = GuiUtils.getWidth(abstractwidget);
                int k = GuiUtils.getHeight(abstractwidget);

                if (x >= abstractwidget.x && y >= abstractwidget.y && x < abstractwidget.x + j && y < abstractwidget.y + k)
                {
                    return abstractwidget;
                }
            }
        }

        return null;
    }

    public static void drawCenteredString(PoseStack matrixStackIn, Font fontRendererIn, FormattedCharSequence textIn, int xIn, int yIn, int colorIn)
    {
        fontRendererIn.drawShadow(matrixStackIn, textIn, (float)(xIn - fontRendererIn.width(textIn) / 2), (float)yIn, colorIn);
    }
}
