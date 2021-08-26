package net.minecraft.world.entity.animal.horse;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class TraderLlama extends Llama
{
    private int despawnDelay = 47999;

    public TraderLlama(EntityType <? extends TraderLlama > p_30939_, Level p_30940_)
    {
        super(p_30939_, p_30940_);
    }

    public boolean isTraderLlama()
    {
        return true;
    }

    protected Llama makeBabyLlama()
    {
        return EntityType.TRADER_LLAMA.create(this.level);
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("DespawnDelay", this.despawnDelay);
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);

        if (pCompound.contains("DespawnDelay", 99))
        {
            this.despawnDelay = pCompound.getInt("DespawnDelay");
        }
    }

    protected void registerGoals()
    {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.targetSelector.addGoal(1, new TraderLlama.TraderLlamaDefendWanderingTraderGoal(this));
    }

    public void setDespawnDelay(int p_149556_)
    {
        this.despawnDelay = p_149556_;
    }

    protected void doPlayerRide(Player pPlayer)
    {
        Entity entity = this.getLeashHolder();

        if (!(entity instanceof WanderingTrader))
        {
            super.doPlayerRide(pPlayer);
        }
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
        if (this.canDespawn())
        {
            this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTrader)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;

            if (this.despawnDelay <= 0)
            {
                this.dropLeash(true, false);
                this.discard();
            }
        }
    }

    private boolean canDespawn()
    {
        return !this.isTamed() && !this.isLeashedToSomethingOtherThanTheWanderingTrader() && !this.hasExactlyOnePlayerPassenger();
    }

    private boolean isLeashedToWanderingTrader()
    {
        return this.getLeashHolder() instanceof WanderingTrader;
    }

    private boolean isLeashedToSomethingOtherThanTheWanderingTrader()
    {
        return this.isLeashed() && !this.isLeashedToWanderingTrader();
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag)
    {
        if (pReason == MobSpawnType.EVENT)
        {
            this.setAge(0);
        }

        if (pSpawnData == null)
        {
            pSpawnData = new AgeableMob.AgeableMobGroupData(false);
        }

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    protected static class TraderLlamaDefendWanderingTraderGoal extends TargetGoal
    {
        private final Llama llama;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public TraderLlamaDefendWanderingTraderGoal(Llama p_149558_)
        {
            super(p_149558_, false);
            this.llama = p_149558_;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        public boolean canUse()
        {
            if (!this.llama.isLeashed())
            {
                return false;
            }
            else
            {
                Entity entity = this.llama.getLeashHolder();

                if (!(entity instanceof WanderingTrader))
                {
                    return false;
                }
                else
                {
                    WanderingTrader wanderingtrader = (WanderingTrader)entity;
                    this.ownerLastHurtBy = wanderingtrader.getLastHurtByMob();
                    int i = wanderingtrader.getLastHurtByMobTimestamp();
                    return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
                }
            }
        }

        public void start()
        {
            this.mob.setTarget(this.ownerLastHurtBy);
            Entity entity = this.llama.getLeashHolder();

            if (entity instanceof WanderingTrader)
            {
                this.timestamp = ((WanderingTrader)entity).getLastHurtByMobTimestamp();
            }

            super.start();
        }
    }
}
