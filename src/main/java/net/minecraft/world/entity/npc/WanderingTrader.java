package net.minecraft.world.entity.npc;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WanderingTrader extends AbstractVillager
{
    private static final int NUMBER_OF_TRADE_OFFERS = 5;
    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    public WanderingTrader(EntityType <? extends WanderingTrader > p_35843_, Level p_35844_)
    {
        super(p_35843_, p_35844_);
    }

    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new UseItemGoal<>(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, (p_35882_) ->
        {
            return this.level.isNight() && !p_35882_.isInvisible();
        }));
        this.goalSelector.addGoal(0, new UseItemGoal<>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, (p_35880_) ->
        {
            return this.level.isDay() && p_35880_.isInvisible();
        }));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderingTrader.WanderToPositionGoal(this, 2.0D, 0.35D));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel p_150046_, AgeableMob p_150047_)
    {
        return null;
    }

    public boolean showProgressBar()
    {
        return false;
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!itemstack.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby())
        {
            if (pHand == InteractionHand.MAIN_HAND)
            {
                pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (this.getOffers().isEmpty())
            {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            else
            {
                if (!this.level.isClientSide)
                {
                    this.setTradingPlayer(pPlayer);
                    this.openTradingScreen(pPlayer, this.getDisplayName(), 1);
                }

                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        else
        {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    protected void updateTrades()
    {
        VillagerTrades.ItemListing[] avillagertrades$itemlisting = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
        VillagerTrades.ItemListing[] avillagertrades$itemlisting1 = VillagerTrades.WANDERING_TRADER_TRADES.get(2);

        if (avillagertrades$itemlisting != null && avillagertrades$itemlisting1 != null)
        {
            MerchantOffers merchantoffers = this.getOffers();
            this.m_35277_(merchantoffers, avillagertrades$itemlisting, 5);
            int i = this.random.nextInt(avillagertrades$itemlisting1.length);
            VillagerTrades.ItemListing villagertrades$itemlisting = avillagertrades$itemlisting1[i];
            MerchantOffer merchantoffer = villagertrades$itemlisting.getOffer(this, this.random);

            if (merchantoffer != null)
            {
                merchantoffers.add(merchantoffer);
            }
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("DespawnDelay", this.despawnDelay);

        if (this.wanderTarget != null)
        {
            pCompound.put("WanderTarget", NbtUtils.writeBlockPos(this.wanderTarget));
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("DespawnDelay", 99))
        {
            this.despawnDelay = pCompound.getInt("DespawnDelay");
        }

        if (pCompound.contains("WanderTarget"))
        {
            this.wanderTarget = NbtUtils.readBlockPos(pCompound.getCompound("WanderTarget"));
        }

        this.setAge(Math.max(0, this.getAge()));
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer)
    {
        return false;
    }

    protected void rewardTradeXp(MerchantOffer pOffer)
    {
        if (pOffer.shouldRewardExp())
        {
            int i = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }
    }

    protected SoundEvent getAmbientSound()
    {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    protected SoundEvent getDrinkingSound(ItemStack pStack)
    {
        return pStack.is(Items.MILK_BUCKET) ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    protected SoundEvent getTradeUpdatedSound(boolean pGetYesSound)
    {
        return pGetYesSound ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    public SoundEvent getNotifyTradeSound()
    {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int pDelay)
    {
        this.despawnDelay = pDelay;
    }

    public int getDespawnDelay()
    {
        return this.despawnDelay;
    }

    public void aiStep()
    {
        super.aiStep();

        if (!this.level.isClientSide)
        {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn()
    {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0)
        {
            this.discard();
        }
    }

    public void setWanderTarget(@Nullable BlockPos pPos)
    {
        this.wanderTarget = pPos;
    }

    @Nullable
    BlockPos getWanderTarget()
    {
        return this.wanderTarget;
    }

    class WanderToPositionGoal extends Goal
    {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(WanderingTrader p_35899_, double p_35900_, double p_35901_)
        {
            this.trader = p_35899_;
            this.stopDistance = p_35900_;
            this.speedModifier = p_35901_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public void stop()
        {
            this.trader.setWanderTarget((BlockPos)null);
            WanderingTrader.this.navigation.stop();
        }

        public boolean canUse()
        {
            BlockPos blockpos = this.trader.getWanderTarget();
            return blockpos != null && this.isTooFarAway(blockpos, this.stopDistance);
        }

        public void tick()
        {
            BlockPos blockpos = this.trader.getWanderTarget();

            if (blockpos != null && WanderingTrader.this.navigation.isDone())
            {
                if (this.isTooFarAway(blockpos, 10.0D))
                {
                    Vec3 vec3 = (new Vec3((double)blockpos.getX() - this.trader.getX(), (double)blockpos.getY() - this.trader.getY(), (double)blockpos.getZ() - this.trader.getZ())).normalize();
                    Vec3 vec31 = vec3.scale(10.0D).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo(vec31.x, vec31.y, vec31.z, this.speedModifier);
                }
                else
                {
                    WanderingTrader.this.navigation.moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos pPos, double pDistance)
        {
            return !pPos.closerThan(this.trader.position(), pDistance);
        }
    }
}
