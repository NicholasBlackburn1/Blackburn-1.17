package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class LargeSmokeParticle extends SmokeParticle
{
    protected LargeSmokeParticle(ClientLevel p_107044_, double p_107045_, double p_107046_, double p_107047_, double p_107048_, double p_107049_, double p_107050_, SpriteSet p_107051_)
    {
        super(p_107044_, p_107045_, p_107046_, p_107047_, p_107048_, p_107049_, p_107050_, 2.5F, p_107051_);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_107054_)
        {
            this.sprites = p_107054_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107068_, double pY, double p_107070_, double pZ, double p_107072_)
        {
            return new LargeSmokeParticle(pLevel, pX, p_107068_, pY, p_107070_, pZ, p_107072_, this.sprites);
        }
    }
}
