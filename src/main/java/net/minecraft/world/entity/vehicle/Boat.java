package net.minecraft.world.entity.vehicle;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Boat extends Entity
{
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    public static final int PADDLE_LEFT = 0;
    public static final int PADDLE_RIGHT = 1;
    private static final int TIME_TO_EJECT = 60;
    private static final double PADDLE_SPEED = (double)((float)Math.PI / 8F);
    public static final double PADDLE_SOUND_TIME = (double)((float)Math.PI / 4F);
    public static final int BUBBLE_TIME = 60;
    private final float[] paddlePositions = new float[2];
    private float invFriction;
    private float outOfControlTicks;
    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private Boat.Status status;
    private Boat.Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;

    public Boat(EntityType <? extends Boat > p_38290_, Level p_38291_)
    {
        super(p_38290_, p_38291_);
        this.blocksBuilding = true;
    }

    public Boat(Level p_38293_, double p_38294_, double p_38295_, double p_38296_)
    {
        this(EntityType.BOAT, p_38293_);
        this.setPos(p_38294_, p_38295_, p_38296_);
        this.xo = p_38294_;
        this.yo = p_38295_;
        this.zo = p_38296_;
    }

    protected float getEyeHeight(Pose pPose, EntityDimensions pSize)
    {
        return pSize.height;
    }

    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    protected void defineSynchedData()
    {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
        this.entityData.define(DATA_ID_TYPE, Boat.Type.OAK.ordinal());
        this.entityData.define(DATA_ID_PADDLE_LEFT, false);
        this.entityData.define(DATA_ID_PADDLE_RIGHT, false);
        this.entityData.define(DATA_ID_BUBBLE_TIME, 0);
    }

    public boolean canCollideWith(Entity pEntity)
    {
        return canVehicleCollide(this, pEntity);
    }

    public static boolean canVehicleCollide(Entity p_38324_, Entity p_38325_)
    {
        return (p_38325_.canBeCollidedWith() || p_38325_.isPushable()) && !p_38324_.isPassengerOfSameVehicle(p_38325_);
    }

    public boolean canBeCollidedWith()
    {
        return true;
    }

    public boolean isPushable()
    {
        return true;
    }

    protected Vec3 getRelativePortalPosition(Direction.Axis p_38335_, BlockUtil.FoundRectangle p_38336_)
    {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(p_38335_, p_38336_));
    }

    public double getPassengersRidingOffset()
    {
        return -0.1D;
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else if (!this.level.isClientSide && !this.isRemoved())
        {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(this.getDamage() + pAmount * 10.0F);
            this.markHurt();
            this.gameEvent(GameEvent.ENTITY_DAMAGED, pSource.getEntity());
            boolean flag = pSource.getEntity() instanceof Player && ((Player)pSource.getEntity()).getAbilities().instabuild;

            if (flag || this.getDamage() > 40.0F)
            {
                if (!flag && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
                {
                    this.spawnAtLocation(this.getDropItem());
                }

                this.discard();
            }

            return true;
        }
        else
        {
            return true;
        }
    }

    public void onAboveBubbleCol(boolean pDownwards)
    {
        if (!this.level.isClientSide)
        {
            this.isAboveBubbleColumn = true;
            this.bubbleColumnDirectionIsDown = pDownwards;

            if (this.getBubbleTime() == 0)
            {
                this.setBubbleTime(60);
            }
        }

        this.level.addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7D, this.getZ() + (double)this.random.nextFloat(), 0.0D, 0.0D, 0.0D);

        if (this.random.nextInt(20) == 0)
        {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
        }

        this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
    }

    public void push(Entity pEntity)
    {
        if (pEntity instanceof Boat)
        {
            if (pEntity.getBoundingBox().minY < this.getBoundingBox().maxY)
            {
                super.push(pEntity);
            }
        }
        else if (pEntity.getBoundingBox().minY <= this.getBoundingBox().minY)
        {
            super.push(pEntity);
        }
    }

    public Item getDropItem()
    {
        switch (this.getBoatType())
        {
            case OAK:
            default:
                return Items.OAK_BOAT;

            case SPRUCE:
                return Items.SPRUCE_BOAT;

            case BIRCH:
                return Items.BIRCH_BOAT;

            case JUNGLE:
                return Items.JUNGLE_BOAT;

            case ACACIA:
                return Items.ACACIA_BOAT;

            case DARK_OAK:
                return Items.DARK_OAK_BOAT;
        }
    }

    public void animateHurt()
    {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0F);
    }

    public boolean isPickable()
    {
        return !this.isRemoved();
    }

    public void lerpTo(double pX, double p_38300_, double pY, float p_38302_, float pZ, int p_38304_, boolean pYaw)
    {
        this.lerpX = pX;
        this.lerpY = p_38300_;
        this.lerpZ = pY;
        this.lerpYRot = (double)p_38302_;
        this.lerpXRot = (double)pZ;
        this.lerpSteps = 10;
    }

    public Direction getMotionDirection()
    {
        return this.getDirection().getClockWise();
    }

    public void tick()
    {
        this.oldStatus = this.status;
        this.status = this.getStatus();

        if (this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER)
        {
            this.outOfControlTicks = 0.0F;
        }
        else
        {
            ++this.outOfControlTicks;
        }

        if (!this.level.isClientSide && this.outOfControlTicks >= 60.0F)
        {
            this.ejectPassengers();
        }

        if (this.getHurtTime() > 0)
        {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F)
        {
            this.setDamage(this.getDamage() - 1.0F);
        }

        super.tick();
        this.tickLerp();

        if (this.isControlledByLocalInstance())
        {
            if (!(this.getFirstPassenger() instanceof Player))
            {
                this.setPaddleState(false, false);
            }

            this.floatBoat();

            if (this.level.isClientSide)
            {
                this.controlBoat();
                this.level.sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        else
        {
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.tickBubbleColumn();

        for (int i = 0; i <= 1; ++i)
        {
            if (this.getPaddleState(i))
            {
                if (!this.isSilent() && (double)(this.paddlePositions[i] % ((float)Math.PI * 2F)) <= (double)((float)Math.PI / 4F) && ((double)this.paddlePositions[i] + (double)((float)Math.PI / 8F)) % (double)((float)Math.PI * 2F) >= (double)((float)Math.PI / 4F))
                {
                    SoundEvent soundevent = this.getPaddleSound();

                    if (soundevent != null)
                    {
                        Vec3 vec3 = this.getViewVector(1.0F);
                        double d0 = i == 1 ? -vec3.z : vec3.z;
                        double d1 = i == 1 ? vec3.x : -vec3.x;
                        this.level.playSound((Player)null, this.getX() + d0, this.getY(), this.getZ() + d1, soundevent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
                        this.level.gameEvent(this.getControllingPassenger(), GameEvent.SPLASH, new BlockPos(this.getX() + d0, this.getY(), this.getZ() + d1));
                    }
                }

                this.paddlePositions[i] = (float)((double)this.paddlePositions[i] + (double)((float)Math.PI / 8F));
            }
            else
            {
                this.paddlePositions[i] = 0.0F;
            }
        }

        this.checkInsideBlocks();
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate((double)0.2F, (double) - 0.01F, (double)0.2F), EntitySelector.pushableBy(this));

        if (!list.isEmpty())
        {
            boolean flag = !this.level.isClientSide && !(this.getControllingPassenger() instanceof Player);

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity = list.get(j);

                if (!entity.hasPassenger(this))
                {
                    if (flag && this.getPassengers().size() < 2 && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player))
                    {
                        entity.startRiding(this);
                    }
                    else
                    {
                        this.push(entity);
                    }
                }
            }
        }
    }

    private void tickBubbleColumn()
    {
        if (this.level.isClientSide)
        {
            int i = this.getBubbleTime();

            if (i > 0)
            {
                this.bubbleMultiplier += 0.05F;
            }
            else
            {
                this.bubbleMultiplier -= 0.1F;
            }

            this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0F, 1.0F);
            this.bubbleAngleO = this.bubbleAngle;
            this.bubbleAngle = 10.0F * (float)Math.sin((double)(0.5F * (float)this.level.getGameTime())) * this.bubbleMultiplier;
        }
        else
        {
            if (!this.isAboveBubbleColumn)
            {
                this.setBubbleTime(0);
            }

            int k = this.getBubbleTime();

            if (k > 0)
            {
                --k;
                this.setBubbleTime(k);
                int j = 60 - k - 1;

                if (j > 0 && k == 0)
                {
                    this.setBubbleTime(0);
                    Vec3 vec3 = this.getDeltaMovement();

                    if (this.bubbleColumnDirectionIsDown)
                    {
                        this.setDeltaMovement(vec3.add(0.0D, -0.7D, 0.0D));
                        this.ejectPassengers();
                    }
                    else
                    {
                        this.setDeltaMovement(vec3.x, this.hasPassenger((p_150274_) ->
                        {
                            return p_150274_ instanceof Player;
                        }) ? 2.7D : 0.6D, vec3.z);
                    }
                }

                this.isAboveBubbleColumn = false;
            }
        }
    }

    @Nullable
    protected SoundEvent getPaddleSound()
    {
        switch (this.getStatus())
        {
            case IN_WATER:
            case UNDER_WATER:
            case UNDER_FLOWING_WATER:
                return SoundEvents.BOAT_PADDLE_WATER;

            case ON_LAND:
                return SoundEvents.BOAT_PADDLE_LAND;

            case IN_AIR:
            default:
                return null;
        }
    }

    private void tickLerp()
    {
        if (this.isControlledByLocalInstance())
        {
            this.lerpSteps = 0;
            this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0)
        {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            double d3 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float)d3 / (float)this.lerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d1, d2);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    public void setPaddleState(boolean pLeft, boolean pRight)
    {
        this.entityData.set(DATA_ID_PADDLE_LEFT, pLeft);
        this.entityData.set(DATA_ID_PADDLE_RIGHT, pRight);
    }

    public float getRowingTime(int pSide, float pLimbSwing)
    {
        return this.getPaddleState(pSide) ? (float)Mth.clampedLerp((double)this.paddlePositions[pSide] - (double)((float)Math.PI / 8F), (double)this.paddlePositions[pSide], (double)pLimbSwing) : 0.0F;
    }

    private Boat.Status getStatus()
    {
        Boat.Status boat$status = this.isUnderwater();

        if (boat$status != null)
        {
            this.waterLevel = this.getBoundingBox().maxY;
            return boat$status;
        }
        else if (this.checkInWater())
        {
            return Boat.Status.IN_WATER;
        }
        else
        {
            float f = this.getGroundFriction();

            if (f > 0.0F)
            {
                this.landFriction = f;
                return Boat.Status.ON_LAND;
            }
            else
            {
                return Boat.Status.IN_AIR;
            }
        }
    }

    public float getWaterLevelAbove()
    {
        AABB aabb = this.getBoundingBox();
        int i = Mth.floor(aabb.minX);
        int j = Mth.ceil(aabb.maxX);
        int k = Mth.floor(aabb.maxY);
        int l = Mth.ceil(aabb.maxY - this.lastYd);
        int i1 = Mth.floor(aabb.minZ);
        int j1 = Mth.ceil(aabb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        label39:

        for (int k1 = k; k1 < l; ++k1)
        {
            float f = 0.0F;

            for (int l1 = i; l1 < j; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    blockpos$mutableblockpos.set(l1, k1, i2);
                    FluidState fluidstate = this.level.getFluidState(blockpos$mutableblockpos);

                    if (fluidstate.is(FluidTags.WATER))
                    {
                        f = Math.max(f, fluidstate.getHeight(this.level, blockpos$mutableblockpos));
                    }

                    if (f >= 1.0F)
                    {
                        continue label39;
                    }
                }
            }

            if (f < 1.0F)
            {
                return (float)blockpos$mutableblockpos.getY() + f;
            }
        }

        return (float)(l + 1);
    }

    public float getGroundFriction()
    {
        AABB aabb = this.getBoundingBox();
        AABB aabb1 = new AABB(aabb.minX, aabb.minY - 0.001D, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        int i = Mth.floor(aabb1.minX) - 1;
        int j = Mth.ceil(aabb1.maxX) + 1;
        int k = Mth.floor(aabb1.minY) - 1;
        int l = Mth.ceil(aabb1.maxY) + 1;
        int i1 = Mth.floor(aabb1.minZ) - 1;
        int j1 = Mth.ceil(aabb1.maxZ) + 1;
        VoxelShape voxelshape = Shapes.create(aabb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int l1 = i; l1 < j; ++l1)
        {
            for (int i2 = i1; i2 < j1; ++i2)
            {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);

                if (j2 != 2)
                {
                    for (int k2 = k; k2 < l; ++k2)
                    {
                        if (j2 <= 0 || k2 != k && k2 != l - 1)
                        {
                            blockpos$mutableblockpos.set(l1, k2, i2);
                            BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);

                            if (!(blockstate.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, blockpos$mutableblockpos).move((double)l1, (double)k2, (double)i2), voxelshape, BooleanOp.AND))
                            {
                                f += blockstate.getBlock().getFriction();
                                ++k1;
                            }
                        }
                    }
                }
            }
        }

        return f / (float)k1;
    }

    private boolean checkInWater()
    {
        AABB aabb = this.getBoundingBox();
        int i = Mth.floor(aabb.minX);
        int j = Mth.ceil(aabb.maxX);
        int k = Mth.floor(aabb.minY);
        int l = Mth.ceil(aabb.minY + 0.001D);
        int i1 = Mth.floor(aabb.minZ);
        int j1 = Mth.ceil(aabb.maxZ);
        boolean flag = false;
        this.waterLevel = -Double.MAX_VALUE;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    blockpos$mutableblockpos.set(k1, l1, i2);
                    FluidState fluidstate = this.level.getFluidState(blockpos$mutableblockpos);

                    if (fluidstate.is(FluidTags.WATER))
                    {
                        float f = (float)l1 + fluidstate.getHeight(this.level, blockpos$mutableblockpos);
                        this.waterLevel = Math.max((double)f, this.waterLevel);
                        flag |= aabb.minY < (double)f;
                    }
                }
            }
        }

        return flag;
    }

    @Nullable
    private Boat.Status isUnderwater()
    {
        AABB aabb = this.getBoundingBox();
        double d0 = aabb.maxY + 0.001D;
        int i = Mth.floor(aabb.minX);
        int j = Mth.ceil(aabb.maxX);
        int k = Mth.floor(aabb.maxY);
        int l = Mth.ceil(d0);
        int i1 = Mth.floor(aabb.minZ);
        int j1 = Mth.ceil(aabb.maxZ);
        boolean flag = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    blockpos$mutableblockpos.set(k1, l1, i2);
                    FluidState fluidstate = this.level.getFluidState(blockpos$mutableblockpos);

                    if (fluidstate.is(FluidTags.WATER) && d0 < (double)((float)blockpos$mutableblockpos.getY() + fluidstate.getHeight(this.level, blockpos$mutableblockpos)))
                    {
                        if (!fluidstate.isSource())
                        {
                            return Boat.Status.UNDER_FLOWING_WATER;
                        }

                        flag = true;
                    }
                }
            }
        }

        return flag ? Boat.Status.UNDER_WATER : null;
    }

    private void floatBoat()
    {
        double d0 = (double) - 0.04F;
        double d1 = this.isNoGravity() ? 0.0D : (double) - 0.04F;
        double d2 = 0.0D;
        this.invFriction = 0.05F;

        if (this.oldStatus == Boat.Status.IN_AIR && this.status != Boat.Status.IN_AIR && this.status != Boat.Status.ON_LAND)
        {
            this.waterLevel = this.getY(1.0D);
            this.setPos(this.getX(), (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101D, this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
            this.lastYd = 0.0D;
            this.status = Boat.Status.IN_WATER;
        }
        else
        {
            if (this.status == Boat.Status.IN_WATER)
            {
                d2 = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
                this.invFriction = 0.9F;
            }
            else if (this.status == Boat.Status.UNDER_FLOWING_WATER)
            {
                d1 = -7.0E-4D;
                this.invFriction = 0.9F;
            }
            else if (this.status == Boat.Status.UNDER_WATER)
            {
                d2 = (double)0.01F;
                this.invFriction = 0.45F;
            }
            else if (this.status == Boat.Status.IN_AIR)
            {
                this.invFriction = 0.9F;
            }
            else if (this.status == Boat.Status.ON_LAND)
            {
                this.invFriction = this.landFriction;

                if (this.getControllingPassenger() instanceof Player)
                {
                    this.landFriction /= 2.0F;
                }
            }

            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x * (double)this.invFriction, vec3.y + d1, vec3.z * (double)this.invFriction);
            this.deltaRotation *= this.invFriction;

            if (d2 > 0.0D)
            {
                Vec3 vec31 = this.getDeltaMovement();
                this.setDeltaMovement(vec31.x, (vec31.y + d2 * 0.06153846016296973D) * 0.75D, vec31.z);
            }
        }
    }

    private void controlBoat()
    {
        if (this.isVehicle())
        {
            float f = 0.0F;

            if (this.inputLeft)
            {
                --this.deltaRotation;
            }

            if (this.inputRight)
            {
                ++this.deltaRotation;
            }

            if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown)
            {
                f += 0.005F;
            }

            this.setYRot(this.getYRot() + this.deltaRotation);

            if (this.inputUp)
            {
                f += 0.04F;
            }

            if (this.inputDown)
            {
                f -= 0.005F;
            }

            this.setDeltaMovement(this.getDeltaMovement().add((double)(Mth.sin(-this.getYRot() * ((float)Math.PI / 180F)) * f), 0.0D, (double)(Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * f)));
            this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
        }
    }

    public void positionRider(Entity pPassenger)
    {
        if (this.hasPassenger(pPassenger))
        {
            float f = 0.0F;
            float f1 = (float)((this.isRemoved() ? (double)0.01F : this.getPassengersRidingOffset()) + pPassenger.getMyRidingOffset());

            if (this.getPassengers().size() > 1)
            {
                int i = this.getPassengers().indexOf(pPassenger);

                if (i == 0)
                {
                    f = 0.2F;
                }
                else
                {
                    f = -0.6F;
                }

                if (pPassenger instanceof Animal)
                {
                    f = (float)((double)f + 0.2D);
                }
            }

            Vec3 vec3 = (new Vec3((double)f, 0.0D, 0.0D)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
            pPassenger.setPos(this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
            pPassenger.setYRot(pPassenger.getYRot() + this.deltaRotation);
            pPassenger.setYHeadRot(pPassenger.getYHeadRot() + this.deltaRotation);
            this.clampRotation(pPassenger);

            if (pPassenger instanceof Animal && this.getPassengers().size() > 1)
            {
                int j = pPassenger.getId() % 2 == 0 ? 90 : 270;
                pPassenger.setYBodyRot(((Animal)pPassenger).yBodyRot + (float)j);
                pPassenger.setYHeadRot(pPassenger.getYHeadRot() + (float)j);
            }
        }
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity)
    {
        Vec3 vec3 = getCollisionHorizontalEscapeVector((double)(this.getBbWidth() * Mth.SQRT_OF_TWO), (double)pLivingEntity.getBbWidth(), pLivingEntity.getYRot());
        double d0 = this.getX() + vec3.x;
        double d1 = this.getZ() + vec3.z;
        BlockPos blockpos = new BlockPos(d0, this.getBoundingBox().maxY, d1);
        BlockPos blockpos1 = blockpos.below();

        if (!this.level.isWaterAt(blockpos1))
        {
            List<Vec3> list = Lists.newArrayList();
            double d2 = this.level.getBlockFloorHeight(blockpos);

            if (DismountHelper.isBlockFloorValid(d2))
            {
                list.add(new Vec3(d0, (double)blockpos.getY() + d2, d1));
            }

            double d3 = this.level.getBlockFloorHeight(blockpos1);

            if (DismountHelper.isBlockFloorValid(d3))
            {
                list.add(new Vec3(d0, (double)blockpos1.getY() + d3, d1));
            }

            for (Pose pose : pLivingEntity.getDismountPoses())
            {
                for (Vec3 vec31 : list)
                {
                    if (DismountHelper.canDismountTo(this.level, vec31, pLivingEntity, pose))
                    {
                        pLivingEntity.setPose(pose);
                        return vec31;
                    }
                }
            }
        }

        return super.getDismountLocationForPassenger(pLivingEntity);
    }

    protected void clampRotation(Entity pEntityToUpdate)
    {
        pEntityToUpdate.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(pEntityToUpdate.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -105.0F, 105.0F);
        pEntityToUpdate.yRotO += f1 - f;
        pEntityToUpdate.setYRot(pEntityToUpdate.getYRot() + f1 - f);
        pEntityToUpdate.setYHeadRot(pEntityToUpdate.getYRot());
    }

    public void onPassengerTurned(Entity pEntityToUpdate)
    {
        this.clampRotation(pEntityToUpdate);
    }

    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        pCompound.putString("Type", this.getBoatType().getName());
    }

    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        if (pCompound.contains("Type", 8))
        {
            this.setType(Boat.Type.byName(pCompound.getString("Type")));
        }
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand)
    {
        if (pPlayer.isSecondaryUseActive())
        {
            return InteractionResult.PASS;
        }
        else if (this.outOfControlTicks < 60.0F)
        {
            if (!this.level.isClientSide)
            {
                return pPlayer.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            else
            {
                return InteractionResult.SUCCESS;
            }
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    protected void checkFallDamage(double pY, boolean p_38308_, BlockState pOnGround, BlockPos pState)
    {
        this.lastYd = this.getDeltaMovement().y;

        if (!this.isPassenger())
        {
            if (p_38308_)
            {
                if (this.fallDistance > 3.0F)
                {
                    if (this.status != Boat.Status.ON_LAND)
                    {
                        this.fallDistance = 0.0F;
                        return;
                    }

                    this.causeFallDamage(this.fallDistance, 1.0F, DamageSource.FALL);

                    if (!this.level.isClientSide && !this.isRemoved())
                    {
                        this.kill();

                        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
                        {
                            for (int i = 0; i < 3; ++i)
                            {
                                this.spawnAtLocation(this.getBoatType().getPlanks());
                            }

                            for (int j = 0; j < 2; ++j)
                            {
                                this.spawnAtLocation(Items.STICK);
                            }
                        }
                    }
                }

                this.fallDistance = 0.0F;
            }
            else if (!this.level.getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && pY < 0.0D)
            {
                this.fallDistance = (float)((double)this.fallDistance - pY);
            }
        }
    }

    public boolean getPaddleState(int pSide)
    {
        return this.entityData.<Boolean>get(pSide == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) && this.getControllingPassenger() != null;
    }

    public void setDamage(float pDamageTaken)
    {
        this.entityData.set(DATA_ID_DAMAGE, pDamageTaken);
    }

    public float getDamage()
    {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public void setHurtTime(int pTimeSinceHit)
    {
        this.entityData.set(DATA_ID_HURT, pTimeSinceHit);
    }

    public int getHurtTime()
    {
        return this.entityData.get(DATA_ID_HURT);
    }

    private void setBubbleTime(int pTicks)
    {
        this.entityData.set(DATA_ID_BUBBLE_TIME, pTicks);
    }

    private int getBubbleTime()
    {
        return this.entityData.get(DATA_ID_BUBBLE_TIME);
    }

    public float getBubbleAngle(float pPartialTicks)
    {
        return Mth.lerp(pPartialTicks, this.bubbleAngleO, this.bubbleAngle);
    }

    public void setHurtDir(int pForwardDirection)
    {
        this.entityData.set(DATA_ID_HURTDIR, pForwardDirection);
    }

    public int getHurtDir()
    {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    public void setType(Boat.Type pBoatType)
    {
        this.entityData.set(DATA_ID_TYPE, pBoatType.ordinal());
    }

    public Boat.Type getBoatType()
    {
        return Boat.Type.byId(this.entityData.get(DATA_ID_TYPE));
    }

    protected boolean canAddPassenger(Entity pPassenger)
    {
        return this.getPassengers().size() < 2 && !this.isEyeInFluid(FluidTags.WATER);
    }

    @Nullable
    public Entity getControllingPassenger()
    {
        return this.getFirstPassenger();
    }

    public void setInput(boolean pLeftInputDown, boolean pRightInputDown, boolean pForwardInputDown, boolean pBackInputDown)
    {
        this.inputLeft = pLeftInputDown;
        this.inputRight = pRightInputDown;
        this.inputUp = pForwardInputDown;
        this.inputDown = pBackInputDown;
    }

    public Packet<?> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    public boolean isUnderWater()
    {
        return this.status == Boat.Status.UNDER_WATER || this.status == Boat.Status.UNDER_FLOWING_WATER;
    }

    public ItemStack getPickResult()
    {
        return new ItemStack(this.getDropItem());
    }

    public static enum Status
    {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;
    }

    public static enum Type
    {
        OAK(Blocks.OAK_PLANKS, "oak"),
        SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
        BIRCH(Blocks.BIRCH_PLANKS, "birch"),
        JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
        ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
        DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

        private final String name;
        private final Block planks;

        private Type(Block p_38427_, String p_38428_)
        {
            this.name = p_38428_;
            this.planks = p_38427_;
        }

        public String getName()
        {
            return this.name;
        }

        public Block getPlanks()
        {
            return this.planks;
        }

        public String toString()
        {
            return this.name;
        }

        public static Boat.Type byId(int pId)
        {
            Boat.Type[] aboat$type = values();

            if (pId < 0 || pId >= aboat$type.length)
            {
                pId = 0;
            }

            return aboat$type[pId];
        }

        public static Boat.Type byName(String pName)
        {
            Boat.Type[] aboat$type = values();

            for (int i = 0; i < aboat$type.length; ++i)
            {
                if (aboat$type[i].getName().equals(pName))
                {
                    return aboat$type[i];
                }
            }

            return aboat$type[0];
        }
    }
}
