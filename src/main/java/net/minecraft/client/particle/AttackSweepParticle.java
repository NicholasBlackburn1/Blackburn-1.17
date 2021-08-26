package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class AttackSweepParticle extends TextureSheetParticle
{
    private final SpriteSet sprites;

    AttackSweepParticle(ClientLevel p_105546_, double p_105547_, double p_105548_, double p_105549_, double p_105550_, SpriteSet p_105551_)
    {
        super(p_105546_, p_105547_, p_105548_, p_105549_, 0.0D, 0.0D, 0.0D);
        this.sprites = p_105551_;
        this.lifetime = 4;
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.quadSize = 1.0F - (float)p_105550_ * 0.5F;
        this.setSpriteFromAge(p_105551_);
    }

    public int getLightColor(float pPartialTick)
    {
        return 15728880;
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
        else
        {
            this.setSpriteFromAge(this.sprites);
        }
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_105566_)
        {
            this.sprites = p_105566_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105580_, double pY, double p_105582_, double pZ, double p_105584_)
        {
            return new AttackSweepParticle(pLevel, pX, p_105580_, pY, p_105582_, this.sprites);
        }
    }
}
