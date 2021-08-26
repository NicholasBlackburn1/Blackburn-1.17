package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.optifine.SmartAnimations;
import net.optifine.render.VertexBuilderWrapper;

public class SpriteCoordinateExpander extends VertexBuilderWrapper implements VertexConsumer
{
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateExpander(VertexConsumer p_110798_, TextureAtlasSprite p_110799_)
    {
        super(p_110798_);

        if (SmartAnimations.isActive())
        {
            SmartAnimations.spriteRendered(p_110799_);
        }

        this.delegate = p_110798_;
        this.sprite = p_110799_;
    }

    public VertexConsumer vertex(double pX, double p_110802_, double pY)
    {
        return this.delegate.vertex(pX, p_110802_, pY);
    }

    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha)
    {
        return this.delegate.color(pRed, pGreen, pBlue, pAlpha);
    }

    public VertexConsumer uv(float pU, float pV)
    {
        return this.delegate.uv(this.sprite.getU((double)(pU * 16.0F)), this.sprite.getV((double)(pV * 16.0F)));
    }

    public VertexConsumer overlayCoords(int pU, int pV)
    {
        return this.delegate.overlayCoords(pU, pV);
    }

    public VertexConsumer uv2(int pU, int pV)
    {
        return this.delegate.uv2(pU, pV);
    }

    public VertexConsumer normal(float pX, float pY, float pZ)
    {
        return this.delegate.normal(pX, pY, pZ);
    }

    public void endVertex()
    {
        this.delegate.endVertex();
    }

    public void defaultColor(int p_173392_, int p_173393_, int p_173394_, int p_173395_)
    {
        this.delegate.defaultColor(p_173392_, p_173393_, p_173394_, p_173395_);
    }

    public void unsetDefaultColor()
    {
        this.delegate.unsetDefaultColor();
    }

    public void vertex(float pX, float p_110809_, float pY, float p_110811_, float pZ, float p_110813_, float p_110814_, float p_110815_, float p_110816_, int p_110817_, int p_110818_, float p_110819_, float p_110820_, float p_110821_)
    {
        this.delegate.vertex(pX, p_110809_, pY, p_110811_, pZ, p_110813_, p_110814_, this.sprite.getU((double)(p_110815_ * 16.0F)), this.sprite.getV((double)(p_110816_ * 16.0F)), p_110817_, p_110818_, p_110819_, p_110820_, p_110821_);
    }
}
