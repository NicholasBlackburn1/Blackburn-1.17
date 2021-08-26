package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class HugeExplosionSeedParticle extends NoRenderParticle
{
    private int life;
    private final int lifeTime = 8;

    HugeExplosionSeedParticle(ClientLevel p_106947_, double p_106948_, double p_106949_, double p_106950_)
    {
        super(p_106947_, p_106948_, p_106949_, p_106950_, 0.0D, 0.0D, 0.0D);
    }

    public void tick()
    {
        for (int i = 0; i < 6; ++i)
        {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0D;
            this.level.addParticle(ParticleTypes.EXPLOSION, d0, d1, d2, (double)((float)this.life / (float)this.lifeTime), 0.0D, 0.0D);
        }

        ++this.life;

        if (this.life == this.lifeTime)
        {
            this.remove();
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106972_, double pY, double p_106974_, double pZ, double p_106976_)
        {
            return new HugeExplosionSeedParticle(pLevel, pX, p_106972_, pY);
        }
    }
}
