package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SmokeParticle extends BaseAshSmokeParticle
{
    protected SmokeParticle(ClientLevel p_107685_, double p_107686_, double p_107687_, double p_107688_, double p_107689_, double p_107690_, double p_107691_, float p_107692_, SpriteSet p_107693_)
    {
        super(p_107685_, p_107686_, p_107687_, p_107688_, 0.1F, 0.1F, 0.1F, p_107689_, p_107690_, p_107691_, p_107692_, p_107693_, 0.3F, 8, -0.1F, true);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_107696_)
        {
            this.sprites = p_107696_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107710_, double pY, double p_107712_, double pZ, double p_107714_)
        {
            return new SmokeParticle(pLevel, pX, p_107710_, pY, p_107712_, pZ, p_107714_, 1.0F, this.sprites);
        }
    }
}
