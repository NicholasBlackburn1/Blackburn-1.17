package net.optifine.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

public class VertexBuilderDummy implements VertexConsumer
{
    private MultiBufferSource.BufferSource renderTypeBuffer = null;

    public VertexBuilderDummy(MultiBufferSource.BufferSource renderTypeBuffer)
    {
        this.renderTypeBuffer = renderTypeBuffer;
    }

    public MultiBufferSource.BufferSource getRenderTypeBuffer()
    {
        return this.renderTypeBuffer;
    }

    public VertexConsumer vertex(double pX, double p_85946_, double pY)
    {
        return this;
    }

    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha)
    {
        return this;
    }

    public VertexConsumer uv(float pU, float pV)
    {
        return this;
    }

    public VertexConsumer overlayCoords(int pOverlayUV, int p_85972_)
    {
        return this;
    }

    public VertexConsumer uv2(int pLightmapUV, int p_86011_)
    {
        return this;
    }

    public VertexConsumer normal(float pX, float pY, float pZ)
    {
        return this;
    }

    public void endVertex()
    {
    }

    public void defaultColor(int p_166901_, int p_166902_, int p_166903_, int p_166904_)
    {
    }

    public void unsetDefaultColor()
    {
    }
}
