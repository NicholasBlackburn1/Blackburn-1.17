package net.optifine.shaders.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.optifine.shaders.config.ShaderOption;

public class GuiSliderShaderOption extends GuiButtonShaderOption
{
    private float sliderValue;
    public boolean dragging;
    private ShaderOption shaderOption = null;

    public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text)
    {
        super(buttonId, x, y, w, h, shaderOption, text);
        this.sliderValue = 1.0F;
        this.shaderOption = shaderOption;
        this.sliderValue = shaderOption.getIndexNormalized();
        this.setMessage(GuiShaderOptions.getButtonText(shaderOption, this.width));
    }

    protected int getYImage(boolean pIsHovered)
    {
        return 0;
    }

    protected void renderBg(PoseStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
    {
        if (this.visible)
        {
            if (this.dragging && !Screen.hasShiftDown())
            {
                this.sliderValue = (float)(pMouseX - (this.x + 4)) / (float)(this.width - 8);
                this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
                this.shaderOption.setIndexNormalized(this.sliderValue);
                this.sliderValue = this.shaderOption.getIndexNormalized();
                this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
            }

            pMinecraft.getTextureManager().bindForSetup(WIDGETS_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int i = (this.isHovered() ? 2 : 1) * 20;
            this.blit(pMatrixStack, this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
            this.blit(pMatrixStack, this.x + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
        }
    }

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        if (super.mouseClicked(pMouseX, p_94738_, pMouseY))
        {
            this.sliderValue = (float)(pMouseX - (double)(this.x + 4)) / (float)(this.width - 8);
            this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
            this.shaderOption.setIndexNormalized(this.sliderValue);
            this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        this.dragging = false;
        return true;
    }

    public void valueChanged()
    {
        this.sliderValue = this.shaderOption.getIndexNormalized();
    }

    public boolean isSwitchable()
    {
        return false;
    }
}
