package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish extends WaterAnimal implements Bucketable
{
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);

    public AbstractFish(EntityType <? extends AbstractFish > p_27461_, Level p_27462_)
    {
        super(p_27461_, p_27462_);
        this.moveControl = new AbstractFish.FishMoveControl(this);
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize)
    {
        return pSize.height * 0.65F;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0D);
    }

    public boolean requiresCustomPersistence()
    {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    public static boolean checkFishSpawnRules(EntityType <? extends AbstractFish > p_27468_, LevelAccessor p_27469_, MobSpawnType p_27470_, BlockPos p_27471_, Random p_27472_)
    {
        return p_27469_.getBlockState(p_27471_).is(Blocks.WATER) && p_27469_.getBlockState(p_27471_.above()).is(Blocks.WATER);
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer)
    {
        return !this.fromBucket() && !this.hasCustomName();
    }

    public int getMaxSpawnClusterSize()
    {
        return 8;
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(FROM_BUCKET, false);
    }

    public boolean fromBucket()
    {
        return this.entityData.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean p_27498_)
    {
        this.entityData.set(FROM_BUCKET, p_27498_);
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("FromBucket", this.fromBucket());
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.setFromBucket(pCompound.getBoolean("FromBucket"));
    }

    protected void registerGoals()
    {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6D, 1.4D, EntitySelector.NO_SPECTATORS::test));
        this.goalSelector.addGoal(4, new AbstractFish.FishSwimGoal(this));
    }

    protected PathNavigation createNavigation(Level pLevel)
    {
        return new WaterBoundPathNavigation(this, pLevel);
    }

    public void travel(Vec3 pTravelVector)
    {
        if (this.isEffectiveAi() && this.isInWater())
        {
            this.moveRelative(0.01F, pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));

            if (this.getTarget() == null)
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        }
        else
        {
            super.travel(pTravelVector);
        }
    }

    public void aiStep()
    {
        if (!this.isInWater() && this.onGround && this.verticalCollision)
        {
            this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), (double)0.4F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F)));
            this.onGround = false;
            this.hasImpulse = true;
            this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
        }

        super.aiStep();
    }

    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        return Bucketable.bucketMobPickup(pPlayer, pHand, this).orElse(super.mobInteract(pPlayer, pHand));
    }

    public void saveToBucketTag(ItemStack p_27494_)
    {
        Bucketable.saveDefaultDataToBucketTag(this, p_27494_);
    }

    public void loadFromBucketTag(CompoundTag p_148708_)
    {
        Bucketable.loadDefaultDataFromBucketTag(this, p_148708_);
    }

    public SoundEvent getPickupSound()
    {
        return SoundEvents.BUCKET_FILL_FISH;
    }

    protected boolean canRandomSwim()
    {
        return true;
    }

    protected abstract SoundEvent getFlopSound();

    protected SoundEvent getSwimSound()
    {
        return SoundEvents.FISH_SWIM;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock)
    {
    }

    static class FishMoveControl extends MoveControl
    {
        private final AbstractFish fish;

        FishMoveControl(AbstractFish p_27501_)
        {
            super(p_27501_);
            this.fish = p_27501_;
        }

        public void tick()
        {
            if (this.fish.isEyeInFluid(FluidTags.WATER))
            {
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
            }

            if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone())
            {
                float f = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), f));
                double d0 = this.wantedX - this.fish.getX();
                double d1 = this.wantedY - this.fish.getY();
                double d2 = this.wantedZ - this.fish.getZ();

                if (d1 != 0.0D)
                {
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, (double)this.fish.getSpeed() * (d1 / d3) * 0.1D, 0.0D));
                }

                if (d0 != 0.0D || d2 != 0.0D)
                {
                    float f1 = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                    this.fish.setYRot(this.rotlerp(this.fish.getYRot(), f1, 90.0F));
                    this.fish.yBodyRot = this.fish.getYRot();
                }
            }
            else
            {
                this.fish.setSpeed(0.0F);
            }
        }
    }

    static class FishSwimGoal extends RandomSwimmingGoal
    {
        private final AbstractFish fish;

        public FishSwimGoal(AbstractFish p_27505_)
        {
            super(p_27505_, 1.0D, 40);
            this.fish = p_27505_;
        }

        public boolean canUse()
        {
            return this.fish.canRandomSwim() && super.canUse();
        }
    }
}
