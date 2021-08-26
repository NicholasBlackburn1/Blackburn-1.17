package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

public class BubbleColumnUpParticle extends TextureSheetParticle
{
    BubbleColumnUpParticle(ClientLevel p_105733_, double p_105734_, double p_105735_, double p_105736_, double p_105737_, double p_105738_, double p_105739_)
    {
        super(p_105733_, p_105734_, p_105735_, p_105736_);
        this.gravity = -0.125F;
        this.friction = 0.85F;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.xd = p_105737_ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.yd = p_105738_ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.zd = p_105739_ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.lifetime = (int)(40.0D / (Math.random() * 0.8D + 0.2D));
    }

    public void tick()
    {
        super.tick();

        if (!this.removed && !this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER))
        {
            this.remove();
        }
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_105753_)
        {
            this.sprite = p_105753_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105767_, double pY, double p_105769_, double pZ, double p_105771_)
        {
            BubbleColumnUpParticle bubblecolumnupparticle = new BubbleColumnUpParticle(pLevel, pX, p_105767_, pY, p_105769_, pZ, p_105771_);
            bubblecolumnupparticle.pickSprite(this.sprite);
            return bubblecolumnupparticle;
        }
    }
}
