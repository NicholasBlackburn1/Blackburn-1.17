package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MinecartTNT extends AbstractMinecart
{
    private static final byte EVENT_PRIME = 10;
    private int fuse = -1;

    public MinecartTNT(EntityType <? extends MinecartTNT > p_38649_, Level p_38650_)
    {
        super(p_38649_, p_38650_);
    }

    public MinecartTNT(Level p_38652_, double p_38653_, double p_38654_, double p_38655_)
    {
        super(EntityType.TNT_MINECART, p_38652_, p_38653_, p_38654_, p_38655_);
    }

    public AbstractMinecart.Type getMinecartType()
    {
        return AbstractMinecart.Type.TNT;
    }

    public BlockState getDefaultDisplayBlockState()
    {
        return Blocks.TNT.defaultBlockState();
    }

    public void tick()
    {
        super.tick();

        if (this.fuse > 0)
        {
            --this.fuse;
            this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }
        else if (this.fuse == 0)
        {
            this.explode(this.getDeltaMovement().horizontalDistanceSqr());
        }

        if (this.horizontalCollision)
        {
            double d0 = this.getDeltaMovement().horizontalDistanceSqr();

            if (d0 >= (double)0.01F)
            {
                this.explode(d0);
            }
        }
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        Entity entity = pSource.getDirectEntity();

        if (entity instanceof AbstractArrow)
        {
            AbstractArrow abstractarrow = (AbstractArrow)entity;

            if (abstractarrow.isOnFire())
            {
                this.explode(abstractarrow.getDeltaMovement().lengthSqr());
            }
        }

        return super.hurt(pSource, pAmount);
    }

    public void destroy(DamageSource pSource)
    {
        double d0 = this.getDeltaMovement().horizontalDistanceSqr();

        if (!pSource.isFire() && !pSource.isExplosion() && !(d0 >= (double)0.01F))
        {
            super.destroy(pSource);

            if (!pSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
            {
                this.spawnAtLocation(Blocks.TNT);
            }
        }
        else
        {
            if (this.fuse < 0)
            {
                this.primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }
        }
    }

    protected void explode(double pRadiusModifier)
    {
        if (!this.level.isClientSide)
        {
            double d0 = Math.sqrt(pRadiusModifier);

            if (d0 > 5.0D)
            {
                d0 = 5.0D;
            }

            this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)(4.0D + this.random.nextDouble() * 1.5D * d0), Explosion.BlockInteraction.BREAK);
            this.discard();
        }
    }

    public boolean causeFallDamage(float p_150347_, float p_150348_, DamageSource p_150349_)
    {
        if (p_150347_ >= 3.0F)
        {
            float f = p_150347_ / 10.0F;
            this.explode((double)(f * f));
        }

        return super.causeFallDamage(p_150347_, p_150348_, p_150349_);
    }

    public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower)
    {
        if (pReceivingPower && this.fuse < 0)
        {
            this.primeFuse();
        }
    }

    public void handleEntityEvent(byte pId)
    {
        if (pId == 10)
        {
            this.primeFuse();
        }
        else
        {
            super.handleEntityEvent(pId);
        }
    }

    public void primeFuse()
    {
        this.fuse = 80;

        if (!this.level.isClientSide)
        {
            this.level.broadcastEntityEvent(this, (byte)10);

            if (!this.isSilent())
            {
                this.level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public int getFuse()
    {
        return this.fuse;
    }

    public boolean isPrimed()
    {
        return this.fuse > -1;
    }

    public float getBlockExplosionResistance(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower)
    {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(pExplosion, pLevel, pPos, pBlockState, pFluidState, pExplosionPower) : 0.0F;
    }

    public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower)
    {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.shouldBlockExplode(pExplosion, pLevel, pPos, pBlockState, pExplosionPower) : false;
    }

    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("TNTFuse", 99))
        {
            this.fuse = pCompound.getInt("TNTFuse");
        }
    }

    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("TNTFuse", this.fuse);
    }
}
