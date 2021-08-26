package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SuspendedTownParticle extends TextureSheetParticle
{
    SuspendedTownParticle(ClientLevel p_108104_, double p_108105_, double p_108106_, double p_108107_, double p_108108_, double p_108109_, double p_108110_)
    {
        super(p_108104_, p_108105_, p_108106_, p_108107_, p_108108_, p_108109_, p_108110_);
        float f = this.random.nextFloat() * 0.1F + 0.2F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
        this.xd *= (double)0.02F;
        this.yd *= (double)0.02F;
        this.zd *= (double)0.02F;
        this.lifetime = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void move(double pX, double p_108123_, double pY)
    {
        this.setBoundingBox(this.getBoundingBox().move(pX, p_108123_, pY));
        this.setLocationFromBoundingbox();
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0)
        {
            this.remove();
        }
        else
        {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99D;
            this.yd *= 0.99D;
            this.zd *= 0.99D;
        }
    }

    public static class ComposterFillProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public ComposterFillProvider(SpriteSet p_108128_)
        {
            this.sprite = p_108128_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108142_, double pY, double p_108144_, double pZ, double p_108146_)
        {
            SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, p_108142_, pY, p_108144_, pZ, p_108146_);
            suspendedtownparticle.pickSprite(this.sprite);
            suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
            suspendedtownparticle.setLifetime(3 + pLevel.getRandom().nextInt(5));
            return suspendedtownparticle;
        }
    }

    public static class DolphinSpeedProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public DolphinSpeedProvider(SpriteSet p_108149_)
        {
            this.sprite = p_108149_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108163_, double pY, double p_108165_, double pZ, double p_108167_)
        {
            SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, p_108163_, pY, p_108165_, pZ, p_108167_);
            suspendedtownparticle.setColor(0.3F, 0.5F, 1.0F);
            suspendedtownparticle.pickSprite(this.sprite);
            suspendedtownparticle.setAlpha(1.0F - pLevel.random.nextFloat() * 0.7F);
            suspendedtownparticle.setLifetime(suspendedtownparticle.getLifetime() / 2);
            return suspendedtownparticle;
        }
    }

    public static class HappyVillagerProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public HappyVillagerProvider(SpriteSet p_108170_)
        {
            this.sprite = p_108170_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108184_, double pY, double p_108186_, double pZ, double p_108188_)
        {
            SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, p_108184_, pY, p_108186_, pZ, p_108188_);
            suspendedtownparticle.pickSprite(this.sprite);
            suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
            return suspendedtownparticle;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_108191_)
        {
            this.sprite = p_108191_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108205_, double pY, double p_108207_, double pZ, double p_108209_)
        {
            SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, p_108205_, pY, p_108207_, pZ, p_108209_);
            suspendedtownparticle.pickSprite(this.sprite);
            return suspendedtownparticle;
        }
    }
}
