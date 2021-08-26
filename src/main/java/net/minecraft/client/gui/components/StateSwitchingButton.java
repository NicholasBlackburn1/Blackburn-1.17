package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class StateSwitchingButton extends AbstractWidget
{
    protected ResourceLocation resourceLocation;
    protected boolean isStateTriggered;
    protected int xTexStart;
    protected int yTexStart;
    protected int xDiffTex;
    protected int yDiffTex;

    public StateSwitchingButton(int p_94615_, int p_94616_, int p_94617_, int p_94618_, boolean p_94619_)
    {
        super(p_94615_, p_94616_, p_94617_, p_94618_, TextComponent.EMPTY);
        this.isStateTriggered = p_94619_;
    }

    public void initTextureValues(int pXTexStart, int pYTexStart, int pXDiffTex, int pYDiffTex, ResourceLocation pResourceLocation)
    {
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.xDiffTex = pXDiffTex;
        this.yDiffTex = pYDiffTex;
        this.resourceLocation = pResourceLocation;
    }

    public void setStateTriggered(boolean pTriggered)
    {
        this.isStateTriggered = pTriggered;
    }

    public boolean isStateTriggered()
    {
        return this.isStateTriggered;
    }

    public void setPosition(int pX, int pY)
    {
        this.x = pX;
        this.y = pY;
    }

    public void updateNarration(NarrationElementOutput p_169069_)
    {
        this.defaultButtonNarrationText(p_169069_);
    }

    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.disableDepthTest();
        int i = this.xTexStart;
        int j = this.yTexStart;

        if (this.isStateTriggered)
        {
            i += this.xDiffTex;
        }

        if (this.isHovered())
        {
            j += this.yDiffTex;
        }

        this.blit(pMatrixStack, this.x, this.y, i, j, this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}
