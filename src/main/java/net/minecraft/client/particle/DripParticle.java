package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class DripParticle extends TextureSheetParticle
{
    private final Fluid type;
    protected boolean isGlowing;

    DripParticle(ClientLevel p_106051_, double p_106052_, double p_106053_, double p_106054_, Fluid p_106055_)
    {
        super(p_106051_, p_106052_, p_106053_, p_106054_);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.type = p_106055_;
    }

    protected Fluid getType()
    {
        return this.type;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public int getLightColor(float pPartialTick)
    {
        return this.isGlowing ? 240 : super.getLightColor(pPartialTick);
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();

        if (!this.removed)
        {
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.postMoveUpdate();

            if (!this.removed)
            {
                this.xd *= (double)0.98F;
                this.yd *= (double)0.98F;
                this.zd *= (double)0.98F;
                BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
                FluidState fluidstate = this.level.getFluidState(blockpos);

                if (fluidstate.getType() == this.type && this.y < (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos)))
                {
                    this.remove();
                }
            }
        }
    }

    protected void preMoveUpdate()
    {
        if (this.lifetime-- <= 0)
        {
            this.remove();
        }
    }

    protected void postMoveUpdate()
    {
    }

    static class CoolingDripHangParticle extends DripParticle.DripHangParticle
    {
        CoolingDripHangParticle(ClientLevel p_106068_, double p_106069_, double p_106070_, double p_106071_, Fluid p_106072_, ParticleOptions p_106073_)
        {
            super(p_106068_, p_106069_, p_106070_, p_106071_, p_106072_, p_106073_);
        }

        protected void preMoveUpdate()
        {
            this.rCol = 1.0F;
            this.gCol = 16.0F / (float)(40 - this.lifetime + 16);
            this.bCol = 4.0F / (float)(40 - this.lifetime + 8);
            super.preMoveUpdate();
        }
    }

    static class DripHangParticle extends DripParticle
    {
        private final ParticleOptions fallingParticle;

        DripHangParticle(ClientLevel p_106085_, double p_106086_, double p_106087_, double p_106088_, Fluid p_106089_, ParticleOptions p_106090_)
        {
            super(p_106085_, p_106086_, p_106087_, p_106088_, p_106089_);
            this.fallingParticle = p_106090_;
            this.gravity *= 0.02F;
            this.lifetime = 40;
        }

        protected void preMoveUpdate()
        {
            if (this.lifetime-- <= 0)
            {
                this.remove();
                this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
        }

        protected void postMoveUpdate()
        {
            this.xd *= 0.02D;
            this.yd *= 0.02D;
            this.zd *= 0.02D;
        }
    }

    static class DripLandParticle extends DripParticle
    {
        DripLandParticle(ClientLevel p_106102_, double p_106103_, double p_106104_, double p_106105_, Fluid p_106106_)
        {
            super(p_106102_, p_106103_, p_106104_, p_106105_, p_106106_);
            this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
        }
    }

    static class DripstoneFallAndLandParticle extends DripParticle.FallAndLandParticle
    {
        DripstoneFallAndLandParticle(ClientLevel p_171930_, double p_171931_, double p_171932_, double p_171933_, Fluid p_171934_, ParticleOptions p_171935_)
        {
            super(p_171930_, p_171931_, p_171932_, p_171933_, p_171934_, p_171935_);
        }

        protected void postMoveUpdate()
        {
            if (this.onGround)
            {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
                SoundEvent soundevent = this.getType() == Fluids.LAVA ? SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;
                float f = Mth.randomBetween(this.random, 0.3F, 1.0F);
                this.level.playLocalSound(this.x, this.y, this.z, soundevent, SoundSource.BLOCKS, f, 1.0F, false);
            }
        }
    }

    public static class DripstoneLavaFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public DripstoneLavaFallProvider(SpriteSet p_171939_)
        {
            this.sprite = p_171939_;
        }

        public Particle createParticle(SimpleParticleType p_171950_, ClientLevel p_171951_, double p_171952_, double p_171953_, double p_171954_, double p_171955_, double p_171956_, double p_171957_)
        {
            DripParticle dripparticle = new DripParticle.DripstoneFallAndLandParticle(p_171951_, p_171952_, p_171953_, p_171954_, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
            dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class DripstoneLavaHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public DripstoneLavaHangProvider(SpriteSet p_171960_)
        {
            this.sprite = p_171960_;
        }

        public Particle createParticle(SimpleParticleType p_171971_, ClientLevel p_171972_, double p_171973_, double p_171974_, double p_171975_, double p_171976_, double p_171977_, double p_171978_)
        {
            DripParticle dripparticle = new DripParticle.CoolingDripHangParticle(p_171972_, p_171973_, p_171974_, p_171975_, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class DripstoneWaterFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public DripstoneWaterFallProvider(SpriteSet p_171981_)
        {
            this.sprite = p_171981_;
        }

        public Particle createParticle(SimpleParticleType p_171992_, ClientLevel p_171993_, double p_171994_, double p_171995_, double p_171996_, double p_171997_, double p_171998_, double p_171999_)
        {
            DripParticle dripparticle = new DripParticle.DripstoneFallAndLandParticle(p_171993_, p_171994_, p_171995_, p_171996_, Fluids.WATER, ParticleTypes.SPLASH);
            dripparticle.setColor(0.2F, 0.3F, 1.0F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class DripstoneWaterHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public DripstoneWaterHangProvider(SpriteSet p_172002_)
        {
            this.sprite = p_172002_;
        }

        public Particle createParticle(SimpleParticleType p_172013_, ClientLevel p_172014_, double p_172015_, double p_172016_, double p_172017_, double p_172018_, double p_172019_, double p_172020_)
        {
            DripParticle dripparticle = new DripParticle.DripHangParticle(p_172014_, p_172015_, p_172016_, p_172017_, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER);
            dripparticle.setColor(0.2F, 0.3F, 1.0F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    static class FallAndLandParticle extends DripParticle.FallingParticle
    {
        protected final ParticleOptions landParticle;

        FallAndLandParticle(ClientLevel p_106116_, double p_106117_, double p_106118_, double p_106119_, Fluid p_106120_, ParticleOptions p_106121_)
        {
            super(p_106116_, p_106117_, p_106118_, p_106119_, p_106120_);
            this.landParticle = p_106121_;
        }

        protected void postMoveUpdate()
        {
            if (this.onGround)
            {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    static class FallingParticle extends DripParticle
    {
        FallingParticle(ClientLevel p_106132_, double p_106133_, double p_106134_, double p_106135_, Fluid p_106136_)
        {
            this(p_106132_, p_106133_, p_106134_, p_106135_, p_106136_, (int)(64.0D / (Math.random() * 0.8D + 0.2D)));
        }

        FallingParticle(ClientLevel p_172022_, double p_172023_, double p_172024_, double p_172025_, Fluid p_172026_, int p_172027_)
        {
            super(p_172022_, p_172023_, p_172024_, p_172025_, p_172026_);
            this.lifetime = p_172027_;
        }

        protected void postMoveUpdate()
        {
            if (this.onGround)
            {
                this.remove();
            }
        }
    }

    static class HoneyFallAndLandParticle extends DripParticle.FallAndLandParticle
    {
        HoneyFallAndLandParticle(ClientLevel p_106146_, double p_106147_, double p_106148_, double p_106149_, Fluid p_106150_, ParticleOptions p_106151_)
        {
            super(p_106146_, p_106147_, p_106148_, p_106149_, p_106150_, p_106151_);
        }

        protected void postMoveUpdate()
        {
            if (this.onGround)
            {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
                float f = Mth.randomBetween(this.random, 0.3F, 1.0F);
                this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, f, 1.0F, false);
            }
        }
    }

    public static class HoneyFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public HoneyFallProvider(SpriteSet p_106163_)
        {
            this.sprite = p_106163_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106177_, double pY, double p_106179_, double pZ, double p_106181_)
        {
            DripParticle dripparticle = new DripParticle.HoneyFallAndLandParticle(pLevel, pX, p_106177_, pY, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
            dripparticle.gravity = 0.01F;
            dripparticle.setColor(0.582F, 0.448F, 0.082F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class HoneyHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public HoneyHangProvider(SpriteSet p_106184_)
        {
            this.sprite = p_106184_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106198_, double pY, double p_106200_, double pZ, double p_106202_)
        {
            DripParticle.DripHangParticle dripparticle$driphangparticle = new DripParticle.DripHangParticle(pLevel, pX, p_106198_, pY, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
            dripparticle$driphangparticle.gravity *= 0.01F;
            dripparticle$driphangparticle.lifetime = 100;
            dripparticle$driphangparticle.setColor(0.622F, 0.508F, 0.082F);
            dripparticle$driphangparticle.pickSprite(this.sprite);
            return dripparticle$driphangparticle;
        }
    }

    public static class HoneyLandProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public HoneyLandProvider(SpriteSet p_106205_)
        {
            this.sprite = p_106205_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106219_, double pY, double p_106221_, double pZ, double p_106223_)
        {
            DripParticle dripparticle = new DripParticle.DripLandParticle(pLevel, pX, p_106219_, pY, Fluids.EMPTY);
            dripparticle.lifetime = (int)(128.0D / (Math.random() * 0.8D + 0.2D));
            dripparticle.setColor(0.522F, 0.408F, 0.082F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class LavaFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public LavaFallProvider(SpriteSet p_106226_)
        {
            this.sprite = p_106226_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106240_, double pY, double p_106242_, double pZ, double p_106244_)
        {
            DripParticle dripparticle = new DripParticle.FallAndLandParticle(pLevel, pX, p_106240_, pY, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
            dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class LavaHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public LavaHangProvider(SpriteSet p_106247_)
        {
            this.sprite = p_106247_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106261_, double pY, double p_106263_, double pZ, double p_106265_)
        {
            DripParticle.CoolingDripHangParticle dripparticle$coolingdriphangparticle = new DripParticle.CoolingDripHangParticle(pLevel, pX, p_106261_, pY, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
            dripparticle$coolingdriphangparticle.pickSprite(this.sprite);
            return dripparticle$coolingdriphangparticle;
        }
    }

    public static class LavaLandProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public LavaLandProvider(SpriteSet p_106268_)
        {
            this.sprite = p_106268_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106282_, double pY, double p_106284_, double pZ, double p_106286_)
        {
            DripParticle dripparticle = new DripParticle.DripLandParticle(pLevel, pX, p_106282_, pY, Fluids.LAVA);
            dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class NectarFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public NectarFallProvider(SpriteSet p_106289_)
        {
            this.sprite = p_106289_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106303_, double pY, double p_106305_, double pZ, double p_106307_)
        {
            DripParticle dripparticle = new DripParticle.FallingParticle(pLevel, pX, p_106303_, pY, Fluids.EMPTY);
            dripparticle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
            dripparticle.gravity = 0.007F;
            dripparticle.setColor(0.92F, 0.782F, 0.72F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class ObsidianTearFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public ObsidianTearFallProvider(SpriteSet p_106310_)
        {
            this.sprite = p_106310_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106324_, double pY, double p_106326_, double pZ, double p_106328_)
        {
            DripParticle dripparticle = new DripParticle.FallAndLandParticle(pLevel, pX, p_106324_, pY, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
            dripparticle.isGlowing = true;
            dripparticle.gravity = 0.01F;
            dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class ObsidianTearHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public ObsidianTearHangProvider(SpriteSet p_106331_)
        {
            this.sprite = p_106331_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106345_, double pY, double p_106347_, double pZ, double p_106349_)
        {
            DripParticle.DripHangParticle dripparticle$driphangparticle = new DripParticle.DripHangParticle(pLevel, pX, p_106345_, pY, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
            dripparticle$driphangparticle.isGlowing = true;
            dripparticle$driphangparticle.gravity *= 0.01F;
            dripparticle$driphangparticle.lifetime = 100;
            dripparticle$driphangparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
            dripparticle$driphangparticle.pickSprite(this.sprite);
            return dripparticle$driphangparticle;
        }
    }

    public static class ObsidianTearLandProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public ObsidianTearLandProvider(SpriteSet p_106352_)
        {
            this.sprite = p_106352_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106366_, double pY, double p_106368_, double pZ, double p_106370_)
        {
            DripParticle dripparticle = new DripParticle.DripLandParticle(pLevel, pX, p_106366_, pY, Fluids.EMPTY);
            dripparticle.isGlowing = true;
            dripparticle.lifetime = (int)(28.0D / (Math.random() * 0.8D + 0.2D));
            dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class SporeBlossomFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;
        private final Random random;

        public SporeBlossomFallProvider(SpriteSet p_172031_)
        {
            this.sprite = p_172031_;
            this.random = new Random();
        }

        public Particle createParticle(SimpleParticleType p_172042_, ClientLevel p_172043_, double p_172044_, double p_172045_, double p_172046_, double p_172047_, double p_172048_, double p_172049_)
        {
            int i = (int)(64.0F / Mth.randomBetween(this.random, 0.1F, 0.9F));
            DripParticle dripparticle = new DripParticle.FallingParticle(p_172043_, p_172044_, p_172045_, p_172046_, Fluids.EMPTY, i);
            dripparticle.gravity = 0.005F;
            dripparticle.setColor(0.32F, 0.5F, 0.22F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class WaterFallProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public WaterFallProvider(SpriteSet p_106373_)
        {
            this.sprite = p_106373_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106387_, double pY, double p_106389_, double pZ, double p_106391_)
        {
            DripParticle dripparticle = new DripParticle.FallAndLandParticle(pLevel, pX, p_106387_, pY, Fluids.WATER, ParticleTypes.SPLASH);
            dripparticle.setColor(0.2F, 0.3F, 1.0F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }

    public static class WaterHangProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet sprite;

        public WaterHangProvider(SpriteSet p_106394_)
        {
            this.sprite = p_106394_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106408_, double pY, double p_106410_, double pZ, double p_106412_)
        {
            DripParticle dripparticle = new DripParticle.DripHangParticle(pLevel, pX, p_106408_, pY, Fluids.WATER, ParticleTypes.FALLING_WATER);
            dripparticle.setColor(0.2F, 0.3F, 1.0F);
            dripparticle.pickSprite(this.sprite);
            return dripparticle;
        }
    }
}
