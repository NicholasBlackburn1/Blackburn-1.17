package net.minecraft.world.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spider extends Monster
{
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);
    private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1F;

    public Spider(EntityType <? extends Spider > p_33786_, Level p_33787_)
    {
        super(p_33786_, p_33787_);
    }

    protected void registerGoals()
    {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new Spider.SpiderAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new Spider.SpiderTargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(3, new Spider.SpiderTargetGoal<>(this, IronGolem.class));
    }

    public double getPassengersRidingOffset()
    {
        return (double)(this.getBbHeight() * 0.5F);
    }

    protected PathNavigation createNavigation(Level pLevel)
    {
        return new WallClimberNavigation(this, pLevel);
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public void tick()
    {
        super.tick();

        if (!this.level.isClientSide)
        {
            this.setClimbing(this.horizontalCollision);
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F);
    }

    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock)
    {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean onClimbable()
    {
        return this.isClimbing();
    }

    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier)
    {
        if (!pState.is(Blocks.COBWEB))
        {
            super.makeStuckInBlock(pState, pMotionMultiplier);
        }
    }

    public MobType getMobType()
    {
        return MobType.ARTHROPOD;
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect)
    {
        return pPotioneffect.getEffect() == MobEffects.POISON ? false : super.canBeAffected(pPotioneffect);
    }

    public boolean isClimbing()
    {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean pClimbing)
    {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);

        if (pClimbing)
        {
            b0 = (byte)(b0 | 1);
        }
        else
        {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag)
    {
        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);

        if (pLevel.getRandom().nextInt(100) == 0)
        {
            Skeleton skeleton = EntityType.SKELETON.create(this.level);
            skeleton.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            skeleton.finalizeSpawn(pLevel, pDifficulty, pReason, (SpawnGroupData)null, (CompoundTag)null);
            skeleton.startRiding(this);
        }

        if (pSpawnData == null)
        {
            pSpawnData = new Spider.SpiderEffectsGroupData();

            if (pLevel.getDifficulty() == Difficulty.HARD && pLevel.getRandom().nextFloat() < 0.1F * pDifficulty.getSpecialMultiplier())
            {
                ((Spider.SpiderEffectsGroupData)pSpawnData).setRandomEffect(pLevel.getRandom());
            }
        }

        if (pSpawnData instanceof Spider.SpiderEffectsGroupData)
        {
            MobEffect mobeffect = ((Spider.SpiderEffectsGroupData)pSpawnData).effect;

            if (mobeffect != null)
            {
                this.addEffect(new MobEffectInstance(mobeffect, Integer.MAX_VALUE));
            }
        }

        return pSpawnData;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize)
    {
        return 0.65F;
    }

    static class SpiderAttackGoal extends MeleeAttackGoal
    {
        public SpiderAttackGoal(Spider p_33822_)
        {
            super(p_33822_, 1.0D, true);
        }

        public boolean canUse()
        {
            return super.canUse() && !this.mob.isVehicle();
        }

        public boolean canContinueToUse()
        {
            float f = this.mob.getBrightness();

            if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0)
            {
                this.mob.setTarget((LivingEntity)null);
                return false;
            }
            else
            {
                return super.canContinueToUse();
            }
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            return (double)(4.0F + pAttackTarget.getBbWidth());
        }
    }

    public static class SpiderEffectsGroupData implements SpawnGroupData
    {
        public MobEffect effect;

        public void setRandomEffect(Random pRand)
        {
            int i = pRand.nextInt(5);

            if (i <= 1)
            {
                this.effect = MobEffects.MOVEMENT_SPEED;
            }
            else if (i <= 2)
            {
                this.effect = MobEffects.DAMAGE_BOOST;
            }
            else if (i <= 3)
            {
                this.effect = MobEffects.REGENERATION;
            }
            else if (i <= 4)
            {
                this.effect = MobEffects.INVISIBILITY;
            }
        }
    }

    static class SpiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T>
    {
        public SpiderTargetGoal(Spider p_33832_, Class<T> p_33833_)
        {
            super(p_33832_, p_33833_, true);
        }

        public boolean canUse()
        {
            float f = this.mob.getBrightness();
            return f >= 0.5F ? false : super.canUse();
        }
    }
}
