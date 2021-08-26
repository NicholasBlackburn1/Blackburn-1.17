package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity
{
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = 32767;
    private static final int INFINITE_LIFETIME = -32768;
    private int age;
    private int pickupDelay;
    private int health = 5;
    private UUID thrower;
    private UUID owner;
    public final float bobOffs;

    public ItemEntity(EntityType <? extends ItemEntity > p_31991_, Level p_31992_)
    {
        super(p_31991_, p_31992_);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
        this.setYRot(this.random.nextFloat() * 360.0F);
    }

    public ItemEntity(Level p_32001_, double p_32002_, double p_32003_, double p_32004_, ItemStack p_32005_)
    {
        this(p_32001_, p_32002_, p_32003_, p_32004_, p_32005_, p_32001_.random.nextDouble() * 0.2D - 0.1D, 0.2D, p_32001_.random.nextDouble() * 0.2D - 0.1D);
    }

    public ItemEntity(Level p_149663_, double p_149664_, double p_149665_, double p_149666_, ItemStack p_149667_, double p_149668_, double p_149669_, double p_149670_)
    {
        this(EntityType.ITEM, p_149663_);
        this.setPos(p_149664_, p_149665_, p_149666_);
        this.setDeltaMovement(p_149668_, p_149669_, p_149670_);
        this.setItem(p_149667_);
    }

    private ItemEntity(ItemEntity p_31994_)
    {
        super(p_31994_.getType(), p_31994_.level);
        this.setItem(p_31994_.getItem().copy());
        this.copyPosition(p_31994_);
        this.age = p_31994_.age;
        this.bobOffs = p_31994_.bobOffs;
    }

    public boolean occludesVibrations()
    {
        return ItemTags.OCCLUDES_VIBRATION_SIGNALS.contains(this.getItem().getItem());
    }

    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    protected void defineSynchedData()
    {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    public void tick()
    {
        if (this.getItem().isEmpty())
        {
            this.discard();
        }
        else
        {
            super.tick();

            if (this.pickupDelay > 0 && this.pickupDelay != 32767)
            {
                --this.pickupDelay;
            }

            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 vec3 = this.getDeltaMovement();
            float f = this.getEyeHeight() - 0.11111111F;

            if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f)
            {
                this.setUnderwaterMovement();
            }
            else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f)
            {
                this.setUnderLavaMovement();
            }
            else if (!this.isNoGravity())
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            if (this.level.isClientSide)
            {
                this.noPhysics = false;
            }
            else
            {
                this.noPhysics = !this.level.noCollision(this, this.getBoundingBox().deflate(1.0E-7D), (p_149672_) ->
                {
                    return true;
                });

                if (this.noPhysics)
                {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
                }
            }

            if (!this.onGround || this.getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5F || (this.tickCount + this.getId()) % 4 == 0)
            {
                this.move(MoverType.SELF, this.getDeltaMovement());
                float f1 = 0.98F;

                if (this.onGround)
                {
                    f1 = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock().getFriction() * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double)f1, 0.98D, (double)f1));

                if (this.onGround)
                {
                    Vec3 vec31 = this.getDeltaMovement();

                    if (vec31.y < 0.0D)
                    {
                        this.setDeltaMovement(vec31.multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }

            boolean flag = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
            int i = flag ? 2 : 40;

            if (this.tickCount % i == 0 && !this.level.isClientSide && this.isMergable())
            {
                this.mergeWithNeighbours();
            }

            if (this.age != -32768)
            {
                ++this.age;
            }

            this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();

            if (!this.level.isClientSide)
            {
                double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();

                if (d0 > 0.01D)
                {
                    this.hasImpulse = true;
                }
            }

            if (!this.level.isClientSide && this.age >= 6000)
            {
                this.discard();
            }
        }
    }

    private void setUnderwaterMovement()
    {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.99F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.99F);
    }

    private void setUnderLavaMovement()
    {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
    }

    private void mergeWithNeighbours()
    {
        if (this.isMergable())
        {
            for (ItemEntity itementity : this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D), (p_149676_) ->
        {
            return p_149676_ != this && p_149676_.isMergable();
            }))
            {
                if (itementity.isMergable())
                {
                    this.tryToMerge(itementity);

                    if (this.isRemoved())
                    {
                        break;
                    }
                }
            }
        }
    }

    private boolean isMergable()
    {
        ItemStack itemstack = this.getItem();
        return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity p_32016_)
    {
        ItemStack itemstack = this.getItem();
        ItemStack itemstack1 = p_32016_.getItem();

        if (Objects.equals(this.getOwner(), p_32016_.getOwner()) && areMergable(itemstack, itemstack1))
        {
            if (itemstack1.getCount() < itemstack.getCount())
            {
                merge(this, itemstack, p_32016_, itemstack1);
            }
            else
            {
                merge(p_32016_, itemstack1, this, itemstack);
            }
        }
    }

    public static boolean areMergable(ItemStack pStack1, ItemStack pStack2)
    {
        if (!pStack2.is(pStack1.getItem()))
        {
            return false;
        }
        else if (pStack2.getCount() + pStack1.getCount() > pStack2.getMaxStackSize())
        {
            return false;
        }
        else if (pStack2.hasTag() ^ pStack1.hasTag())
        {
            return false;
        }
        else
        {
            return !pStack2.hasTag() || pStack2.getTag().equals(pStack1.getTag());
        }
    }

    public static ItemStack merge(ItemStack pStack1, ItemStack pStack2, int p_32032_)
    {
        int i = Math.min(Math.min(pStack1.getMaxStackSize(), p_32032_) - pStack1.getCount(), pStack2.getCount());
        ItemStack itemstack = pStack1.copy();
        itemstack.grow(i);
        pStack2.shrink(i);
        return itemstack;
    }

    private static void merge(ItemEntity pStack1, ItemStack pStack2, ItemStack p_32025_)
    {
        ItemStack itemstack = merge(pStack2, p_32025_, 64);
        pStack1.setItem(itemstack);
    }

    private static void merge(ItemEntity pStack1, ItemStack pStack2, ItemEntity p_32020_, ItemStack p_32021_)
    {
        merge(pStack1, pStack2, p_32021_);
        pStack1.pickupDelay = Math.max(pStack1.pickupDelay, p_32020_.pickupDelay);
        pStack1.age = Math.min(pStack1.age, p_32020_.age);

        if (p_32021_.isEmpty())
        {
            p_32020_.discard();
        }
    }

    public boolean fireImmune()
    {
        return this.getItem().getItem().isFireResistant() || super.fireImmune();
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && pSource.isExplosion())
        {
            return false;
        }
        else if (!this.getItem().getItem().canBeHurtBy(pSource))
        {
            return false;
        }
        else
        {
            this.markHurt();
            this.health = (int)((float)this.health - pAmount);
            this.gameEvent(GameEvent.ENTITY_DAMAGED, pSource.getEntity());

            if (this.health <= 0)
            {
                this.getItem().onDestroyed(this);
                this.discard();
            }

            return true;
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        pCompound.putShort("Health", (short)this.health);
        pCompound.putShort("Age", (short)this.age);
        pCompound.putShort("PickupDelay", (short)this.pickupDelay);

        if (this.getThrower() != null)
        {
            pCompound.putUUID("Thrower", this.getThrower());
        }

        if (this.getOwner() != null)
        {
            pCompound.putUUID("Owner", this.getOwner());
        }

        if (!this.getItem().isEmpty())
        {
            pCompound.put("Item", this.getItem().save(new CompoundTag()));
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        this.health = pCompound.getShort("Health");
        this.age = pCompound.getShort("Age");

        if (pCompound.contains("PickupDelay"))
        {
            this.pickupDelay = pCompound.getShort("PickupDelay");
        }

        if (pCompound.hasUUID("Owner"))
        {
            this.owner = pCompound.getUUID("Owner");
        }

        if (pCompound.hasUUID("Thrower"))
        {
            this.thrower = pCompound.getUUID("Thrower");
        }

        CompoundTag compoundtag = pCompound.getCompound("Item");
        this.setItem(ItemStack.of(compoundtag));

        if (this.getItem().isEmpty())
        {
            this.discard();
        }
    }

    public void playerTouch(Player pEntity)
    {
        if (!this.level.isClientSide)
        {
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();

            if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(pEntity.getUUID())) && pEntity.getInventory().add(itemstack))
            {
                pEntity.take(this, i);

                if (itemstack.isEmpty())
                {
                    this.discard();
                    itemstack.setCount(i);
                }

                pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                pEntity.onItemPickup(this);
            }
        }
    }

    public Component getName()
    {
        Component component = this.getCustomName();
        return (Component)(component != null ? component : new TranslatableComponent(this.getItem().getDescriptionId()));
    }

    public boolean isAttackable()
    {
        return false;
    }

    @Nullable
    public Entity changeDimension(ServerLevel pServer)
    {
        Entity entity = super.changeDimension(pServer);

        if (!this.level.isClientSide && entity instanceof ItemEntity)
        {
            ((ItemEntity)entity).mergeWithNeighbours();
        }

        return entity;
    }

    public ItemStack getItem()
    {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack pStack)
    {
        this.getEntityData().set(DATA_ITEM, pStack);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey)
    {
        super.onSyncedDataUpdated(pKey);

        if (DATA_ITEM.equals(pKey))
        {
            this.getItem().setEntityRepresentation(this);
        }
    }

    @Nullable
    public UUID getOwner()
    {
        return this.owner;
    }

    public void setOwner(@Nullable UUID pOwnerId)
    {
        this.owner = pOwnerId;
    }

    @Nullable
    public UUID getThrower()
    {
        return this.thrower;
    }

    public void setThrower(@Nullable UUID pThrowerId)
    {
        this.thrower = pThrowerId;
    }

    public int getAge()
    {
        return this.age;
    }

    public void setDefaultPickUpDelay()
    {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay()
    {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp()
    {
        this.pickupDelay = 32767;
    }

    public void setPickUpDelay(int pTicks)
    {
        this.pickupDelay = pTicks;
    }

    public boolean hasPickUpDelay()
    {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime()
    {
        this.age = -32768;
    }

    public void setExtendedLifetime()
    {
        this.age = -6000;
    }

    public void makeFakeItem()
    {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public float getSpin(float pPartialTicks)
    {
        return ((float)this.getAge() + pPartialTicks) / 20.0F + this.bobOffs;
    }

    public Packet<?> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    public ItemEntity copy()
    {
        return new ItemEntity(this);
    }

    public SoundSource getSoundSource()
    {
        return SoundSource.AMBIENT;
    }
}
