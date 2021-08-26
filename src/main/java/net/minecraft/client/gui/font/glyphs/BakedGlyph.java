package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.optifine.util.MathUtils;

public class BakedGlyph
{
    private final RenderType normalType;
    private final RenderType seeThroughType;
    private final RenderType polygonOffsetType;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;
    public static final Matrix4f MATRIX_IDENTITY = MathUtils.makeMatrixIdentity();

    public BakedGlyph(RenderType p_181376_, RenderType p_181377_, RenderType p_181378_, float p_181379_, float p_181380_, float p_181381_, float p_181382_, float p_181383_, float p_181384_, float p_181385_, float p_181386_)
    {
        this.normalType = p_181376_;
        this.seeThroughType = p_181377_;
        this.polygonOffsetType = p_181378_;
        this.u0 = p_181379_;
        this.u1 = p_181380_;
        this.v0 = p_181381_;
        this.v1 = p_181382_;
        this.left = p_181383_;
        this.right = p_181384_;
        this.up = p_181385_;
        this.down = p_181386_;
    }

    public void render(boolean pItalic, float pX, float pY, Matrix4f pMatrix, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight)
    {
        int i = 3;
        float f = pX + this.left;
        float f1 = pX + this.right;
        float f2 = this.up - 3.0F;
        float f3 = this.down - 3.0F;
        float f4 = pY + f2;
        float f5 = pY + f3;
        float f6 = pItalic ? 1.0F - 0.25F * f2 : 0.0F;
        float f7 = pItalic ? 1.0F - 0.25F * f3 : 0.0F;

        if (pBuffer instanceof BufferBuilder && pMatrix == MATRIX_IDENTITY)
        {
            BufferBuilder bufferbuilder = (BufferBuilder)pBuffer;
            int j = (int)(pRed * 255.0F);
            int k = (int)(pGreen * 255.0F);
            int l = (int)(pBlue * 255.0F);
            int i1 = (int)(pAlpha * 255.0F);
            int j1 = pPackedLight & 65535;
            int k1 = pPackedLight >> 16 & 65535;
            bufferbuilder.addVertexText(f + f6, f4, 0.0F, j, k, l, i1, this.u0, this.v0, j1, k1);
            bufferbuilder.addVertexText(f + f7, f5, 0.0F, j, k, l, i1, this.u0, this.v1, j1, k1);
            bufferbuilder.addVertexText(f1 + f7, f5, 0.0F, j, k, l, i1, this.u1, this.v1, j1, k1);
            bufferbuilder.addVertexText(f1 + f6, f4, 0.0F, j, k, l, i1, this.u1, this.v0, j1, k1);
        }
        else
        {
            pBuffer.vertex(pMatrix, f + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
            pBuffer.vertex(pMatrix, f + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
            pBuffer.vertex(pMatrix, f1 + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
            pBuffer.vertex(pMatrix, f1 + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
        }
    }

    public void renderEffect(BakedGlyph.Effect pEffect, Matrix4f pMatrix, VertexConsumer pBuffer, int pPackedLight)
    {
        pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
        pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
        pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
        pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
    }

    public RenderType renderType(Font.DisplayMode p_181388_)
    {
        switch (p_181388_)
        {
            case NORMAL:
            default:
                return this.normalType;

            case SEE_THROUGH:
                return this.seeThroughType;

            case POLYGON_OFFSET:
                return this.polygonOffsetType;
        }
    }

    public static class Effect
    {
        protected final float x0;
        protected final float y0;
        protected final float x1;
        protected final float y1;
        protected final float depth;
        protected final float r;
        protected final float g;
        protected final float b;
        protected final float a;

        public Effect(float p_95247_, float p_95248_, float p_95249_, float p_95250_, float p_95251_, float p_95252_, float p_95253_, float p_95254_, float p_95255_)
        {
            this.x0 = p_95247_;
            this.y0 = p_95248_;
            this.x1 = p_95249_;
            this.y1 = p_95250_;
            this.depth = p_95251_;
            this.r = p_95252_;
            this.g = p_95253_;
            this.b = p_95254_;
            this.a = p_95255_;
        }
    }
}
