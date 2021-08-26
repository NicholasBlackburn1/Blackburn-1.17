package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SplashParticle extends WaterDropParticle
{
    SplashParticle(ClientLevel p_107929_, double p_107930_, double p_107931_, double p_107932_, double p_107933_, double p_107934_, double p_107935_)
    {
        super(p_107929_, p_107930_, p_107931_, p_107932_);
        this.gravity = 0.04F;

        if (p_107934_ == 0.0D && (p_107933_ != 0.0D || p_107935_ != 0.0D))
        {
            this.xd = p_107933_;
            this.yd = 0.1D;
            this.zd = p_107935_;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_107947_)
        {
            this.sprite = p_107947_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107961_, double pY, double p_107963_, double pZ, double p_107965_)
        {
            SplashParticle splashparticle = new SplashParticle(pLevel, pX, p_107961_, pY, p_107963_, pZ, p_107965_);
            splashparticle.pickSprite(this.sprite);
            return splashparticle;
        }
    }
}
