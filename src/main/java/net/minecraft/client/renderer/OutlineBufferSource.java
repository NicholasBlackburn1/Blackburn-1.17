package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;

public class OutlineBufferSource implements MultiBufferSource
{
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
    private int teamR = 255;
    private int teamG = 255;
    private int teamB = 255;
    private int teamA = 255;

    public OutlineBufferSource(MultiBufferSource.BufferSource p_109927_)
    {
        this.bufferSource = p_109927_;
    }

    public VertexConsumer getBuffer(RenderType p_109935_)
    {
        if (p_109935_.isOutline())
        {
            VertexConsumer vertexconsumer2 = this.outlineBufferSource.getBuffer(p_109935_);
            return new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
        }
        else
        {
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_109935_);
            Optional<RenderType> optional = p_109935_.outline();

            if (optional.isPresent())
            {
                VertexConsumer vertexconsumer1 = this.outlineBufferSource.getBuffer(optional.get());
                OutlineBufferSource.EntityOutlineGenerator outlinebuffersource$entityoutlinegenerator = new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer1, this.teamR, this.teamG, this.teamB, this.teamA);
                return VertexMultiConsumer.create(outlinebuffersource$entityoutlinegenerator, vertexconsumer);
            }
            else
            {
                return vertexconsumer;
            }
        }
    }

    public void setColor(int pRed, int pGreen, int pBlue, int pAlpha)
    {
        this.teamR = pRed;
        this.teamG = pGreen;
        this.teamB = pBlue;
        this.teamA = pAlpha;
    }

    public void endOutlineBatch()
    {
        this.outlineBufferSource.endBatch();
    }

    static class EntityOutlineGenerator extends DefaultedVertexConsumer
    {
        private final VertexConsumer delegate;
        private double x;
        private double y;
        private double z;
        private float u;
        private float v;

        EntityOutlineGenerator(VertexConsumer p_109943_, int p_109944_, int p_109945_, int p_109946_, int p_109947_)
        {
            this.delegate = p_109943_;
            super.defaultColor(p_109944_, p_109945_, p_109946_, p_109947_);
        }

        public void defaultColor(int p_109993_, int p_109994_, int p_109995_, int p_109996_)
        {
        }

        public void unsetDefaultColor()
        {
        }

        public VertexConsumer vertex(double pX, double p_109957_, double pY)
        {
            this.x = pX;
            this.y = p_109957_;
            this.z = pY;
            return this;
        }

        public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha)
        {
            return this;
        }

        public VertexConsumer uv(float pU, float pV)
        {
            this.u = pU;
            this.v = pV;
            return this;
        }

        public VertexConsumer overlayCoords(int pU, int pV)
        {
            return this;
        }

        public VertexConsumer uv2(int pU, int pV)
        {
            return this;
        }

        public VertexConsumer normal(float pX, float pY, float pZ)
        {
            return this;
        }

        public void vertex(float pX, float p_109964_, float pY, float p_109966_, float pZ, float p_109968_, float p_109969_, float p_109970_, float p_109971_, int p_109972_, int p_109973_, float p_109974_, float p_109975_, float p_109976_)
        {
            this.delegate.vertex((double)pX, (double)p_109964_, (double)pY).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(p_109970_, p_109971_).endVertex();
        }

        public void endVertex()
        {
            this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
        }
    }
}
