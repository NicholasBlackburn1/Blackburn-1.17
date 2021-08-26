package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SkeletonHorse extends AbstractHorse
{
    private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
    private static final int TRAP_MAX_LIFE = 18000;
    private boolean isTrap;
    private int trapTime;

    public SkeletonHorse(EntityType <? extends SkeletonHorse > p_30894_, Level p_30895_)
    {
        super(p_30894_, p_30895_);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0D).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
    }

    protected void randomizeAttributes()
    {
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
    }

    protected void addBehaviourGoals()
    {
    }

    protected SoundEvent getAmbientSound()
    {
        super.getAmbientSound();
        return this.isEyeInFluid(FluidTags.WATER) ? SoundEvents.SKELETON_HORSE_AMBIENT_WATER : SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    protected SoundEvent getDeathSound()
    {
        super.getDeathSound();
        return SoundEvents.SKELETON_HORSE_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        super.getHurtSound(pDamageSource);
        return SoundEvents.SKELETON_HORSE_HURT;
    }

    protected SoundEvent getSwimSound()
    {
        if (this.onGround)
        {
            if (!this.isVehicle())
            {
                return SoundEvents.SKELETON_HORSE_STEP_WATER;
            }

            ++this.gallopSoundCounter;

            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0)
            {
                return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
            }

            if (this.gallopSoundCounter <= 5)
            {
                return SoundEvents.SKELETON_HORSE_STEP_WATER;
            }
        }

        return SoundEvents.SKELETON_HORSE_SWIM;
    }

    protected void playSwimSound(float pVolume)
    {
        if (this.onGround)
        {
            super.playSwimSound(0.3F);
        }
        else
        {
            super.playSwimSound(Math.min(0.1F, pVolume * 25.0F));
        }
    }

    protected void playJumpSound()
    {
        if (this.isInWater())
        {
            this.playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4F, 1.0F);
        }
        else
        {
            super.playJumpSound();
        }
    }

    public MobType getMobType()
    {
        return MobType.UNDEAD;
    }

    public double getPassengersRidingOffset()
    {
        return super.getPassengersRidingOffset() - 0.1875D;
    }

    public void aiStep()
    {
        super.aiStep();

        if (this.isTrap() && this.trapTime++ >= 18000)
        {
            this.discard();
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("SkeletonTrap", this.isTrap());
        pCompound.putInt("SkeletonTrapTime", this.trapTime);
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.setTrap(pCompound.getBoolean("SkeletonTrap"));
        this.trapTime = pCompound.getInt("SkeletonTrapTime");
    }

    public boolean rideableUnderWater()
    {
        return true;
    }

    protected float getWaterSlowDown()
    {
        return 0.96F;
    }

    public boolean isTrap()
    {
        return this.isTrap;
    }

    public void setTrap(boolean pTrap)
    {
        if (pTrap != this.isTrap)
        {
            this.isTrap = pTrap;

            if (pTrap)
            {
                this.goalSelector.addGoal(1, this.skeletonTrapGoal);
            }
            else
            {
                this.goalSelector.removeGoal(this.skeletonTrapGoal);
            }
        }
    }

    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel p_149553_, AgeableMob p_149554_)
    {
        return EntityType.SKELETON_HORSE.create(p_149553_);
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!this.isTamed())
        {
            return InteractionResult.PASS;
        }
        else if (this.isBaby())
        {
            return super.mobInteract(pPlayer, pHand);
        }
        else if (pPlayer.isSecondaryUseActive())
        {
            this.openInventory(pPlayer);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (this.isVehicle())
        {
            return super.mobInteract(pPlayer, pHand);
        }
        else
        {
            if (!itemstack.isEmpty())
            {
                if (itemstack.is(Items.SADDLE) && !this.isSaddled())
                {
                    this.openInventory(pPlayer);
                    return InteractionResult.sidedSuccess(this.level.isClientSide);
                }

                InteractionResult interactionresult = itemstack.interactLivingEntity(pPlayer, this, pHand);

                if (interactionresult.consumesAction())
                {
                    return interactionresult;
                }
            }

            this.doPlayerRide(pPlayer);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
    }
}
