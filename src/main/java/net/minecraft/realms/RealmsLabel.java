package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;

public class RealmsLabel implements Widget
{
    private final Component text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(Component pText, int pX, int pY, int pColor)
    {
        this.text = pText;
        this.x = pX;
        this.y = pY;
        this.color = pColor;
    }

    public void render(PoseStack p_175036_, int p_175037_, int p_175038_, float p_175039_)
    {
        GuiComponent.drawCenteredString(p_175036_, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
    }

    public Component getText()
    {
        return this.text;
    }
}
