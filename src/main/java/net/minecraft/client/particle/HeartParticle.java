package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class HeartParticle extends TextureSheetParticle
{
    HeartParticle(ClientLevel p_106847_, double p_106848_, double p_106849_, double p_106850_)
    {
        super(p_106847_, p_106848_, p_106849_, p_106850_, 0.0D, 0.0D, 0.0D);
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.86F;
        this.xd *= (double)0.01F;
        this.yd *= (double)0.01F;
        this.zd *= (double)0.01F;
        this.yd += 0.1D;
        this.quadSize *= 1.5F;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor)
    {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public static class AngryVillagerProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public AngryVillagerProvider(SpriteSet p_106863_)
        {
            this.sprite = p_106863_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106877_, double pY, double p_106879_, double pZ, double p_106881_)
        {
            HeartParticle heartparticle = new HeartParticle(pLevel, pX, p_106877_ + 0.5D, pY);
            heartparticle.pickSprite(this.sprite);
            heartparticle.setColor(1.0F, 1.0F, 1.0F);
            return heartparticle;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_106884_)
        {
            this.sprite = p_106884_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106898_, double pY, double p_106900_, double pZ, double p_106902_)
        {
            HeartParticle heartparticle = new HeartParticle(pLevel, pX, p_106898_, pY);
            heartparticle.pickSprite(this.sprite);
            return heartparticle;
        }
    }
}
