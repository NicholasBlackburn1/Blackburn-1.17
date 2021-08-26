package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FlameParticle extends RisingParticle
{
    FlameParticle(ClientLevel p_106800_, double p_106801_, double p_106802_, double p_106803_, double p_106804_, double p_106805_, double p_106806_)
    {
        super(p_106800_, p_106801_, p_106802_, p_106803_, p_106804_, p_106805_, p_106806_);
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void move(double pX, double p_106818_, double pY)
    {
        this.setBoundingBox(this.getBoundingBox().move(pX, p_106818_, pY));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float pScaleFactor)
    {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    public int getLightColor(float pPartialTick)
    {
        float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int)(f * 15.0F * 16.0F);

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_106827_)
        {
            this.sprite = p_106827_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106841_, double pY, double p_106843_, double pZ, double p_106845_)
        {
            FlameParticle flameparticle = new FlameParticle(pLevel, pX, p_106841_, pY, p_106843_, pZ, p_106845_);
            flameparticle.pickSprite(this.sprite);
            return flameparticle;
        }
    }

    public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public SmallFlameProvider(SpriteSet p_172113_)
        {
            this.sprite = p_172113_;
        }

        public Particle createParticle(SimpleParticleType p_172124_, ClientLevel p_172125_, double p_172126_, double p_172127_, double p_172128_, double p_172129_, double p_172130_, double p_172131_)
        {
            FlameParticle flameparticle = new FlameParticle(p_172125_, p_172126_, p_172127_, p_172128_, p_172129_, p_172130_, p_172131_);
            flameparticle.pickSprite(this.sprite);
            flameparticle.scale(0.5F);
            return flameparticle;
        }
    }
}
