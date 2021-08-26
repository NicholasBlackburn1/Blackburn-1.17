package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class TotemParticle extends SimpleAnimatedParticle
{
    TotemParticle(ClientLevel p_108346_, double p_108347_, double p_108348_, double p_108349_, double p_108350_, double p_108351_, double p_108352_, SpriteSet p_108353_)
    {
        super(p_108346_, p_108347_, p_108348_, p_108349_, p_108353_, 1.25F);
        this.friction = 0.6F;
        this.xd = p_108350_;
        this.yd = p_108351_;
        this.zd = p_108352_;
        this.quadSize *= 0.75F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setSpriteFromAge(p_108353_);

        if (this.random.nextInt(4) == 0)
        {
            this.setColor(0.6F + this.random.nextFloat() * 0.2F, 0.6F + this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.2F);
        }
        else
        {
            this.setColor(0.1F + this.random.nextFloat() * 0.2F, 0.4F + this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.2F);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_108366_)
        {
            this.sprites = p_108366_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108380_, double pY, double p_108382_, double pZ, double p_108384_)
        {
            return new TotemParticle(pLevel, pX, p_108380_, pY, p_108382_, pZ, p_108384_, this.sprites);
        }
    }
}
