package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public abstract class AbstractSliderButton extends AbstractWidget
{
    protected double value;

    public AbstractSliderButton(int p_93579_, int p_93580_, int p_93581_, int p_93582_, Component p_93583_, double p_93584_)
    {
        super(p_93579_, p_93580_, p_93581_, p_93582_, p_93583_);
        this.value = p_93584_;
    }

    protected int getYImage(boolean pIsHovered)
    {
        return 0;
    }

    protected MutableComponent createNarrationMessage()
    {
        return new TranslatableComponent("gui.narrate.slider", this.getMessage());
    }

    public void updateNarration(NarrationElementOutput p_168798_)
    {
        p_168798_.add(NarratedElementType.TITLE, this.createNarrationMessage());

        if (this.active)
        {
            if (this.isFocused())
            {
                p_168798_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.slider.usage.focused"));
            }
            else
            {
                p_168798_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.slider.usage.hovered"));
            }
        }
    }

    protected void renderBg(PoseStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
    {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered() ? 2 : 1) * 20;
        this.blit(pMatrixStack, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
        this.blit(pMatrixStack, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }

    public void onClick(double pMouseX, double p_93589_)
    {
        this.setValueFromMouse(pMouseX);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        boolean flag = pKeyCode == 263;

        if (flag || pKeyCode == 262)
        {
            float f = flag ? -1.0F : 1.0F;
            this.setValue(this.value + (double)(f / (float)(this.width - 8)));
        }

        return false;
    }

    private void setValueFromMouse(double pMouseX)
    {
        this.setValue((pMouseX - (double)(this.x + 4)) / (double)(this.width - 8));
    }

    private void setValue(double pValue)
    {
        double d0 = this.value;
        this.value = Mth.clamp(pValue, 0.0D, 1.0D);

        if (d0 != this.value)
        {
            this.applyValue();
        }

        this.updateMessage();
    }

    protected void onDrag(double pMouseX, double p_93592_, double pMouseY, double p_93594_)
    {
        this.setValueFromMouse(pMouseX);
        super.onDrag(pMouseX, p_93592_, pMouseY, p_93594_);
    }

    public void playDownSound(SoundManager pHandler)
    {
    }

    public void onRelease(double pMouseX, double p_93610_)
    {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}
