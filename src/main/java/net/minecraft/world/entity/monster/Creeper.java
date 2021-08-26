package net.minecraft.world.entity.monster;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class Creeper extends Monster implements PowerableMob
{
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
    private int oldSwell;
    private int swell;
    private int maxSwell = 30;
    private int explosionRadius = 3;
    private int droppedSkulls;

    public Creeper(EntityType <? extends Creeper > p_32278_, Level p_32279_)
    {
        super(p_32278_, p_32279_);
    }

    protected void registerGoals()
    {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    public int getMaxFallDistance()
    {
        return this.getTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
    }

    public boolean causeFallDamage(float p_149687_, float p_149688_, DamageSource p_149689_)
    {
        boolean flag = super.causeFallDamage(p_149687_, p_149688_, p_149689_);
        this.swell = (int)((float)this.swell + p_149687_ * 1.5F);

        if (this.swell > this.maxSwell - 5)
        {
            this.swell = this.maxSwell - 5;
        }

        return flag;
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_SWELL_DIR, -1);
        this.entityData.define(DATA_IS_POWERED, false);
        this.entityData.define(DATA_IS_IGNITED, false);
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);

        if (this.entityData.get(DATA_IS_POWERED))
        {
            pCompound.putBoolean("powered", true);
        }

        pCompound.putShort("Fuse", (short)this.maxSwell);
        pCompound.putByte("ExplosionRadius", (byte)this.explosionRadius);
        pCompound.putBoolean("ignited", this.isIgnited());
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.entityData.set(DATA_IS_POWERED, pCompound.getBoolean("powered"));

        if (pCompound.contains("Fuse", 99))
        {
            this.maxSwell = pCompound.getShort("Fuse");
        }

        if (pCompound.contains("ExplosionRadius", 99))
        {
            this.explosionRadius = pCompound.getByte("ExplosionRadius");
        }

        if (pCompound.getBoolean("ignited"))
        {
            this.ignite();
        }
    }

    public void tick()
    {
        if (this.isAlive())
        {
            this.oldSwell = this.swell;

            if (this.isIgnited())
            {
                this.setSwellDir(1);
            }

            int i = this.getSwellDir();

            if (i > 0 && this.swell == 0)
            {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
                this.gameEvent(GameEvent.PRIME_FUSE);
            }

            this.swell += i;

            if (this.swell < 0)
            {
                this.swell = 0;
            }

            if (this.swell >= this.maxSwell)
            {
                this.swell = this.maxSwell;
                this.explodeCreeper();
            }
        }

        super.tick();
    }

    public void setTarget(@Nullable LivingEntity p_149691_)
    {
        if (!(p_149691_ instanceof Goat))
        {
            super.setTarget(p_149691_);
        }
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return SoundEvents.CREEPER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.CREEPER_DEATH;
    }

    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit)
    {
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
        Entity entity = pSource.getEntity();

        if (entity != this && entity instanceof Creeper)
        {
            Creeper creeper = (Creeper)entity;

            if (creeper.canDropMobsSkull())
            {
                creeper.increaseDroppedSkulls();
                this.spawnAtLocation(Items.CREEPER_HEAD);
            }
        }
    }

    public boolean doHurtTarget(Entity pEntity)
    {
        return true;
    }

    public boolean isPowered()
    {
        return this.entityData.get(DATA_IS_POWERED);
    }

    public float getSwelling(float pPartialTicks)
    {
        return Mth.lerp(pPartialTicks, (float)this.oldSwell, (float)this.swell) / (float)(this.maxSwell - 2);
    }

    public int getSwellDir()
    {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int pState)
    {
        this.entityData.set(DATA_SWELL_DIR, pState);
    }

    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning)
    {
        super.thunderHit(pLevel, pLightning);
        this.entityData.set(DATA_IS_POWERED, true);
    }

    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (itemstack.is(Items.FLINT_AND_STEEL))
        {
            this.level.playSound(pPlayer, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);

            if (!this.level.isClientSide)
            {
                this.ignite();
                itemstack.hurtAndBreak(1, pPlayer, (p_32290_) ->
                {
                    p_32290_.broadcastBreakEvent(pHand);
                });
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else
        {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    private void explodeCreeper()
    {
        if (!this.level.isClientSide)
        {
            Explosion.BlockInteraction explosion$blockinteraction = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
            float f = this.isPowered() ? 2.0F : 1.0F;
            this.dead = true;
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, explosion$blockinteraction);
            this.discard();
            this.spawnLingeringCloud();
        }
    }

    private void spawnLingeringCloud()
    {
        Collection<MobEffectInstance> collection = this.getActiveEffects();

        if (!collection.isEmpty())
        {
            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
            areaeffectcloud.setRadius(2.5F);
            areaeffectcloud.setRadiusOnUse(-0.5F);
            areaeffectcloud.setWaitTime(10);
            areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());

            for (MobEffectInstance mobeffectinstance : collection)
            {
                areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
            }

            this.level.addFreshEntity(areaeffectcloud);
        }
    }

    public boolean isIgnited()
    {
        return this.entityData.get(DATA_IS_IGNITED);
    }

    public void ignite()
    {
        this.entityData.set(DATA_IS_IGNITED, true);
    }

    public boolean canDropMobsSkull()
    {
        return this.isPowered() && this.droppedSkulls < 1;
    }

    public void increaseDroppedSkulls()
    {
        ++this.droppedSkulls;
    }
}
