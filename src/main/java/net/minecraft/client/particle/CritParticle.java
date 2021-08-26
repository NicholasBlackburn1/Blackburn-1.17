package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class CritParticle extends TextureSheetParticle
{
    CritParticle(ClientLevel p_105919_, double p_105920_, double p_105921_, double p_105922_, double p_105923_, double p_105924_, double p_105925_)
    {
        super(p_105919_, p_105920_, p_105921_, p_105922_, 0.0D, 0.0D, 0.0D);
        this.friction = 0.7F;
        this.gravity = 0.5F;
        this.xd *= (double)0.1F;
        this.yd *= (double)0.1F;
        this.zd *= (double)0.1F;
        this.xd += p_105923_ * 0.4D;
        this.yd += p_105924_ * 0.4D;
        this.zd += p_105925_ * 0.4D;
        float f = (float)(Math.random() * (double)0.3F + (double)0.6F);
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.quadSize *= 0.75F;
        this.lifetime = Math.max((int)(6.0D / (Math.random() * 0.8D + 0.6D)), 1);
        this.hasPhysics = false;
        this.tick();
    }

    public float getQuadSize(float pScaleFactor)
    {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public void tick()
    {
        super.tick();
        this.gCol = (float)((double)this.gCol * 0.96D);
        this.bCol = (float)((double)this.bCol * 0.9D);
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public DamageIndicatorProvider(SpriteSet p_105941_)
        {
            this.sprite = p_105941_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105955_, double pY, double p_105957_, double pZ, double p_105959_)
        {
            CritParticle critparticle = new CritParticle(pLevel, pX, p_105955_, pY, p_105957_, pZ + 1.0D, p_105959_);
            critparticle.setLifetime(20);
            critparticle.pickSprite(this.sprite);
            return critparticle;
        }
    }

    public static class MagicProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public MagicProvider(SpriteSet p_105962_)
        {
            this.sprite = p_105962_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105976_, double pY, double p_105978_, double pZ, double p_105980_)
        {
            CritParticle critparticle = new CritParticle(pLevel, pX, p_105976_, pY, p_105978_, pZ, p_105980_);
            critparticle.rCol *= 0.3F;
            critparticle.gCol *= 0.8F;
            critparticle.pickSprite(this.sprite);
            return critparticle;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_105983_)
        {
            this.sprite = p_105983_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105997_, double pY, double p_105999_, double pZ, double p_106001_)
        {
            CritParticle critparticle = new CritParticle(pLevel, pX, p_105997_, pY, p_105999_, pZ, p_106001_);
            critparticle.pickSprite(this.sprite);
            return critparticle;
        }
    }
}
