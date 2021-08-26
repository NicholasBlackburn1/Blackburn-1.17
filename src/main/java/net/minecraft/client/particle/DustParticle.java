package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;

public class DustParticle extends DustParticleBase<DustParticleOptions>
{
    protected DustParticle(ClientLevel p_106415_, double p_106416_, double p_106417_, double p_106418_, double p_106419_, double p_106420_, double p_106421_, DustParticleOptions p_106422_, SpriteSet p_106423_)
    {
        super(p_106415_, p_106416_, p_106417_, p_106418_, p_106419_, p_106420_, p_106421_, p_106422_, p_106423_);
    }

    public static class Provider implements ParticleProvider<DustParticleOptions>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_106441_)
        {
            this.sprites = p_106441_;
        }

        public Particle createParticle(DustParticleOptions pType, ClientLevel pLevel, double pX, double p_106446_, double pY, double p_106448_, double pZ, double p_106450_)
        {
            return new DustParticle(pLevel, pX, p_106446_, pY, p_106448_, pZ, p_106450_, pType, this.sprites);
        }
    }
}
