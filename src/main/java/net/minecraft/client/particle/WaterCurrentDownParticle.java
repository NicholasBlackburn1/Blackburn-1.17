package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;

public class WaterCurrentDownParticle extends TextureSheetParticle
{
    private float angle;

    WaterCurrentDownParticle(ClientLevel p_108450_, double p_108451_, double p_108452_, double p_108453_)
    {
        super(p_108450_, p_108451_, p_108452_, p_108453_);
        this.lifetime = (int)(Math.random() * 60.0D) + 30;
        this.hasPhysics = false;
        this.xd = 0.0D;
        this.yd = -0.05D;
        this.zd = 0.0D;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.gravity = 0.002F;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
            float f = 0.6F;
            this.xd += (double)(0.6F * Mth.cos(this.angle));
            this.zd += (double)(0.6F * Mth.sin(this.angle));
            this.xd *= 0.07D;
            this.zd *= 0.07D;
            this.move(this.xd, this.yd, this.zd);

            if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER) || this.onGround)
            {
                this.remove();
            }

            this.angle = (float)((double)this.angle + 0.08D);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_108464_)
        {
            this.sprite = p_108464_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_108478_, double pY, double p_108480_, double pZ, double p_108482_)
        {
            WaterCurrentDownParticle watercurrentdownparticle = new WaterCurrentDownParticle(pLevel, pX, p_108478_, pY);
            watercurrentdownparticle.pickSprite(this.sprite);
            return watercurrentdownparticle;
        }
    }
}
