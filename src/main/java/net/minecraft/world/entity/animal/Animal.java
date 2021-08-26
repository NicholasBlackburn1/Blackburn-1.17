package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class Animal extends AgeableMob
{
    static final int PARENT_AGE_AFTER_BREEDING = 6000;
    private int inLove;
    private UUID loveCause;

    protected Animal(EntityType <? extends Animal > p_27557_, Level p_27558_)
    {
        super(p_27557_, p_27558_);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    protected void customServerAiStep()
    {
        if (this.getAge() != 0)
        {
            this.inLove = 0;
        }

        super.customServerAiStep();
    }

    public void aiStep()
    {
        super.aiStep();

        if (this.getAge() != 0)
        {
            this.inLove = 0;
        }

        if (this.inLove > 0)
        {
            --this.inLove;

            if (this.inLove % 10 == 0)
            {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        }
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else
        {
            this.inLove = 0;
            return super.hurt(pSource, pAmount);
        }
    }

    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel)
    {
        return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : pLevel.getBrightness(pPos) - 0.5F;
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("InLove", this.inLove);

        if (this.loveCause != null)
        {
            pCompound.putUUID("LoveCause", this.loveCause);
        }
    }

    public double getMyRidingOffset()
    {
        return 0.14D;
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.inLove = pCompound.getInt("InLove");
        this.loveCause = pCompound.hasUUID("LoveCause") ? pCompound.getUUID("LoveCause") : null;
    }

    public static boolean checkAnimalSpawnRules(EntityType <? extends Animal > pAnimal, LevelAccessor pLevel, MobSpawnType pReason, BlockPos pPos, Random pRandom)
    {
        return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) && pLevel.getRawBrightness(pPos, 0) > 8;
    }

    public int getAmbientSoundInterval()
    {
        return 120;
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer)
    {
        return false;
    }

    protected int getExperienceReward(Player pPlayer)
    {
        return 1 + this.level.random.nextInt(3);
    }

    public boolean isFood(ItemStack pStack)
    {
        return pStack.is(Items.WHEAT);
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (this.isFood(itemstack))
        {
            int i = this.getAge();

            if (!this.level.isClientSide && i == 0 && this.canFallInLove())
            {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.setInLove(pPlayer);
                this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby())
            {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp((int)((float)(-i / 20) * 0.1F), true);
                this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide)
            {
                return InteractionResult.CONSUME;
            }
        }

        return super.mobInteract(pPlayer, pHand);
    }

    protected void usePlayerItem(Player p_148715_, InteractionHand p_148716_, ItemStack p_148717_)
    {
        if (!p_148715_.getAbilities().instabuild)
        {
            p_148717_.shrink(1);
        }
    }

    public boolean canFallInLove()
    {
        return this.inLove <= 0;
    }

    public void setInLove(@Nullable Player pPlayer)
    {
        this.inLove = 600;

        if (pPlayer != null)
        {
            this.loveCause = pPlayer.getUUID();
        }

        this.level.broadcastEntityEvent(this, (byte)18);
    }

    public void setInLoveTime(int pTicks)
    {
        this.inLove = pTicks;
    }

    public int getInLoveTime()
    {
        return this.inLove;
    }

    @Nullable
    public ServerPlayer getLoveCause()
    {
        if (this.loveCause == null)
        {
            return null;
        }
        else
        {
            Player player = this.level.getPlayerByUUID(this.loveCause);
            return player instanceof ServerPlayer ? (ServerPlayer)player : null;
        }
    }

    public boolean isInLove()
    {
        return this.inLove > 0;
    }

    public void resetLove()
    {
        this.inLove = 0;
    }

    public boolean canMate(Animal pOtherAnimal)
    {
        if (pOtherAnimal == this)
        {
            return false;
        }
        else if (pOtherAnimal.getClass() != this.getClass())
        {
            return false;
        }
        else
        {
            return this.isInLove() && pOtherAnimal.isInLove();
        }
    }

    public void spawnChildFromBreeding(ServerLevel pLevel, Animal p_27565_)
    {
        AgeableMob ageablemob = this.getBreedOffspring(pLevel, p_27565_);

        if (ageablemob != null)
        {
            ServerPlayer serverplayer = this.getLoveCause();

            if (serverplayer == null && p_27565_.getLoveCause() != null)
            {
                serverplayer = p_27565_.getLoveCause();
            }

            if (serverplayer != null)
            {
                serverplayer.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this, p_27565_, ageablemob);
            }

            this.setAge(6000);
            p_27565_.setAge(6000);
            this.resetLove();
            p_27565_.resetLove();
            ageablemob.setBaby(true);
            ageablemob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            pLevel.addFreshEntityWithPassengers(ageablemob);
            pLevel.broadcastEntityEvent(this, (byte)18);

            if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
            {
                pLevel.addFreshEntity(new ExperienceOrb(pLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
            }
        }
    }

    public void handleEntityEvent(byte pId)
    {
        if (pId == 18)
        {
            for (int i = 0; i < 7; ++i)
            {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        }
        else
        {
            super.handleEntityEvent(pId);
        }
    }
}
