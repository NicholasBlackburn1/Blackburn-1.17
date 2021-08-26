package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class AshParticle extends BaseAshSmokeParticle
{
    protected AshParticle(ClientLevel p_105514_, double p_105515_, double p_105516_, double p_105517_, double p_105518_, double p_105519_, double p_105520_, float p_105521_, SpriteSet p_105522_)
    {
        super(p_105514_, p_105515_, p_105516_, p_105517_, 0.1F, -0.1F, 0.1F, p_105518_, p_105519_, p_105520_, p_105521_, p_105522_, 0.5F, 20, 0.1F, false);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_105525_)
        {
            this.sprites = p_105525_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105539_, double pY, double p_105541_, double pZ, double p_105543_)
        {
            return new AshParticle(pLevel, pX, p_105539_, pY, 0.0D, 0.0D, 0.0D, 1.0F, this.sprites);
        }
    }
}
