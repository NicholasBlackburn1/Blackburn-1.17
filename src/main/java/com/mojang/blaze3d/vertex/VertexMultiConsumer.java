package com.mojang.blaze3d.vertex;

import java.util.function.Consumer;
import net.optifine.render.VertexBuilderWrapper;

public class VertexMultiConsumer
{
    public static VertexConsumer create()
    {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer pVertexBuilder)
    {
        return pVertexBuilder;
    }

    public static VertexConsumer create(VertexConsumer pVertexBuilder, VertexConsumer pDelegateBuilder)
    {
        return new VertexMultiConsumer.Double(pVertexBuilder, pDelegateBuilder);
    }

    public static VertexConsumer m_167063_(VertexConsumer... p_167064_)
    {
        return new VertexMultiConsumer.Multiple(p_167064_);
    }

    static class Double extends VertexBuilderWrapper implements VertexConsumer
    {
        private final VertexConsumer first;
        private final VertexConsumer second;
        private boolean fixMultitextureUV;

        public Double(VertexConsumer p_86174_, VertexConsumer p_86175_)
        {
            super(p_86175_);

            if (p_86174_ == p_86175_)
            {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            else
            {
                this.first = p_86174_;
                this.second = p_86175_;
                this.updateFixMultitextureUv();
            }
        }

        public VertexConsumer vertex(double pX, double p_86178_, double pY)
        {
            this.first.vertex(pX, p_86178_, pY);
            this.second.vertex(pX, p_86178_, pY);
            return this;
        }

        public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha)
        {
            this.first.color(pRed, pGreen, pBlue, pAlpha);
            this.second.color(pRed, pGreen, pBlue, pAlpha);
            return this;
        }

        public VertexConsumer uv(float pU, float pV)
        {
            this.first.uv(pU, pV);
            this.second.uv(pU, pV);
            return this;
        }

        public VertexConsumer overlayCoords(int pU, int pV)
        {
            this.first.overlayCoords(pU, pV);
            this.second.overlayCoords(pU, pV);
            return this;
        }

        public VertexConsumer uv2(int pU, int pV)
        {
            this.first.uv2(pU, pV);
            this.second.uv2(pU, pV);
            return this;
        }

        public VertexConsumer normal(float pX, float pY, float pZ)
        {
            this.first.normal(pX, pY, pZ);
            this.second.normal(pX, pY, pZ);
            return this;
        }

        public void vertex(float pX, float p_86185_, float pY, float p_86187_, float pZ, float p_86189_, float p_86190_, float p_86191_, float p_86192_, int p_86193_, int p_86194_, float p_86195_, float p_86196_, float p_86197_)
        {
            if (this.fixMultitextureUV)
            {
                this.first.vertex(pX, p_86185_, pY, p_86187_, pZ, p_86189_, p_86190_, p_86191_ / 32.0F, p_86192_ / 32.0F, p_86193_, p_86194_, p_86195_, p_86196_, p_86197_);
            }
            else
            {
                this.first.vertex(pX, p_86185_, pY, p_86187_, pZ, p_86189_, p_86190_, p_86191_, p_86192_, p_86193_, p_86194_, p_86195_, p_86196_, p_86197_);
            }

            this.second.vertex(pX, p_86185_, pY, p_86187_, pZ, p_86189_, p_86190_, p_86191_, p_86192_, p_86193_, p_86194_, p_86195_, p_86196_, p_86197_);
        }

        public void endVertex()
        {
            this.first.endVertex();
            this.second.endVertex();
        }

        public void defaultColor(int p_167066_, int p_167067_, int p_167068_, int p_167069_)
        {
            this.first.defaultColor(p_167066_, p_167067_, p_167068_, p_167069_);
            this.second.defaultColor(p_167066_, p_167067_, p_167068_, p_167069_);
        }

        public void unsetDefaultColor()
        {
            this.first.unsetDefaultColor();
            this.second.unsetDefaultColor();
        }

        public void setRenderBlocks(boolean renderBlocks)
        {
            super.setRenderBlocks(renderBlocks);
            this.updateFixMultitextureUv();
        }

        private void updateFixMultitextureUv()
        {
            this.fixMultitextureUV = !this.first.isMultiTexture() && this.second.isMultiTexture();
        }

        public VertexConsumer getSecondaryBuilder()
        {
            return this.first;
        }
    }

    static class Multiple extends VertexBuilderWrapper implements VertexConsumer
    {
        private final VertexConsumer[] delegates;

        public Multiple(VertexConsumer[] p_167073_)
        {
            super(p_167073_.length > 0 ? p_167073_[0] : null);

            for (int i = 0; i < p_167073_.length; ++i)
            {
                for (int j = i + 1; j < p_167073_.length; ++j)
                {
                    if (p_167073_[i] == p_167073_[j])
                    {
                        throw new IllegalArgumentException("Duplicate delegates");
                    }
                }
            }

            this.delegates = p_167073_;
        }

        private void forEach(Consumer<VertexConsumer> p_167145_)
        {
            for (VertexConsumer vertexconsumer : this.delegates)
            {
                p_167145_.accept(vertexconsumer);
            }
        }

        public VertexConsumer vertex(double p_167075_, double p_167076_, double p_167077_)
        {
            this.forEach((p_313201_6_) ->
            {
                p_313201_6_.vertex(p_167075_, p_167076_, p_167077_);
            });
            return this;
        }

        public VertexConsumer color(int p_167130_, int p_167131_, int p_167132_, int p_167133_)
        {
            this.forEach((p_313281_4_) ->
            {
                p_313281_4_.color(p_167130_, p_167131_, p_167132_, p_167133_);
            });
            return this;
        }

        public VertexConsumer uv(float p_167084_, float p_167085_)
        {
            this.forEach((p_313245_2_) ->
            {
                p_313245_2_.uv(p_167084_, p_167085_);
            });
            return this;
        }

        public VertexConsumer overlayCoords(int p_167127_, int p_167128_)
        {
            this.forEach((p_313287_2_) ->
            {
                p_313287_2_.overlayCoords(p_167127_, p_167128_);
            });
            return this;
        }

        public VertexConsumer uv2(int p_167151_, int p_167152_)
        {
            this.forEach((p_313263_2_) ->
            {
                p_313263_2_.uv2(p_167151_, p_167152_);
            });
            return this;
        }

        public VertexConsumer normal(float p_167147_, float p_167148_, float p_167149_)
        {
            this.forEach((p_313240_3_) ->
            {
                p_313240_3_.normal(p_167147_, p_167148_, p_167149_);
            });
            return this;
        }

        public void vertex(float p_167087_, float p_167088_, float p_167089_, float p_167090_, float p_167091_, float p_167092_, float p_167093_, float p_167094_, float p_167095_, int p_167096_, int p_167097_, float p_167098_, float p_167099_, float p_167100_)
        {
            this.forEach((p_313224_14_) ->
            {
                p_313224_14_.vertex(p_167087_, p_167088_, p_167089_, p_167090_, p_167091_, p_167092_, p_167093_, p_167094_, p_167095_, p_167096_, p_167097_, p_167098_, p_167099_, p_167100_);
            });
        }

        public void endVertex()
        {
            this.forEach(VertexConsumer::endVertex);
        }

        public void defaultColor(int p_167154_, int p_167155_, int p_167156_, int p_167157_)
        {
            this.forEach((p_313257_4_) ->
            {
                p_313257_4_.defaultColor(p_167154_, p_167155_, p_167156_, p_167157_);
            });
        }

        public void unsetDefaultColor()
        {
            this.forEach(VertexConsumer::unsetDefaultColor);
        }
    }
}
