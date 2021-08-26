package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Entity implements Nameable, EntityAccess, CommandSource
{
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final String ID_TAG = "id";
    public static final String PASSENGERS_TAG = "Passengers";
    private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
    private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
    public static final int BOARDING_COOLDOWN = 60;
    public static final int TOTAL_AIR_SUPPLY = 300;
    public static final int MAX_ENTITY_TAG_COUNT = 1024;
    public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW = 0.5000001D;
    public static final float BREATHING_DISTANCE_BELOW_EYES = 0.11111111F;
    public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
    public static final int FREEZE_HURT_FREQUENCY = 40;
    private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static final double WATER_FLOW_SCALE = 0.014D;
    private static final double LAVA_FAST_FLOW_SCALE = 0.007D;
    private static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335D;
    public static final String UUID_TAG = "UUID";
    private static double viewScale = 1.0D;
    private final EntityType<?> type;
    private int id = ENTITY_COUNTER.incrementAndGet();
    public boolean blocksBuilding;
    private ImmutableList<Entity> passengers = ImmutableList.of();
    protected int boardingCooldown;
    @Nullable
    private Entity vehicle;
    public Level level;
    public double xo;
    public double yo;
    public double zo;
    private Vec3 position;
    private BlockPos blockPosition;
    private Vec3 deltaMovement = Vec3.ZERO;
    private float yRot;
    private float xRot;
    public float yRotO;
    public float xRotO;
    private AABB bb = INITIAL_AABB;
    protected boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean hurtMarked;
    protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
    @Nullable
    private Entity.RemovalReason removalReason;
    public static final float DEFAULT_BB_WIDTH = 0.6F;
    public static final float DEFAULT_BB_HEIGHT = 1.8F;
    public float walkDistO;
    public float walkDist;
    public float moveDist;
    public float flyDist;
    public float fallDistance;
    private float nextStep = 1.0F;
    public double xOld;
    public double yOld;
    public double zOld;
    public float maxUpStep;
    public boolean noPhysics;
    protected final Random random = new Random();
    public int tickCount;
    private int remainingFireTicks = -this.getFireImmuneTicks();
    protected boolean wasTouchingWater;
    protected Object2DoubleMap<Tag<Fluid>> fluidHeight = new Object2DoubleArrayMap<>(2);
    protected boolean wasEyeInWater;
    @Nullable
    protected Tag<Fluid> fluidOnEyes;
    public int invulnerableTime;
    protected boolean firstTick = true;
    protected final SynchedEntityData entityData;
    protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
    protected static final int FLAG_ONFIRE = 0;
    private static final int FLAG_SHIFT_KEY_DOWN = 1;
    private static final int FLAG_SPRINTING = 3;
    private static final int FLAG_SWIMMING = 4;
    private static final int FLAG_INVISIBLE = 5;
    protected static final int FLAG_GLOWING = 6;
    protected static final int FLAG_FALL_FLYING = 7;
    private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
    private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
    private static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
    private EntityInLevelCallback levelCallback = EntityInLevelCallback.NULL;
    private Vec3 packetCoordinates;
    public boolean noCulling;
    public boolean hasImpulse;
    private int portalCooldown;
    protected boolean isInsidePortal;
    protected int portalTime;
    protected BlockPos portalEntrancePos;
    private boolean invulnerable;
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    protected String stringUUID = this.uuid.toString();
    private boolean hasGlowingTag;
    private final Set<String> tags = Sets.newHashSet();
    private final double[] pistonDeltas = new double[] {0.0D, 0.0D, 0.0D};
    private long pistonDeltasGameTime;
    private EntityDimensions dimensions;
    private float eyeHeight;
    public boolean isInPowderSnow;
    public boolean wasInPowderSnow;
    public boolean wasOnFire;
    private float crystalSoundIntensity;
    private int lastCrystalSoundPlayTick;
    private boolean hasVisualFire;

    public Entity(EntityType<?> p_19870_, Level p_19871_)
    {
        this.type = p_19870_;
        this.level = p_19871_;
        this.dimensions = p_19870_.getDimensions();
        this.position = Vec3.ZERO;
        this.blockPosition = BlockPos.ZERO;
        this.packetCoordinates = Vec3.ZERO;
        this.entityData = new SynchedEntityData(this);
        this.entityData.define(DATA_SHARED_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
        this.entityData.define(DATA_CUSTOM_NAME_VISIBLE, false);
        this.entityData.define(DATA_CUSTOM_NAME, Optional.empty());
        this.entityData.define(DATA_SILENT, false);
        this.entityData.define(DATA_NO_GRAVITY, false);
        this.entityData.define(DATA_POSE, Pose.STANDING);
        this.entityData.define(DATA_TICKS_FROZEN, 0);
        this.defineSynchedData();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.eyeHeight = this.getEyeHeight(Pose.STANDING, this.dimensions);
    }

    public boolean isColliding(BlockPos p_20040_, BlockState p_20041_)
    {
        VoxelShape voxelshape = p_20041_.getCollisionShape(this.level, p_20040_, CollisionContext.of(this));
        VoxelShape voxelshape1 = voxelshape.move((double)p_20040_.getX(), (double)p_20040_.getY(), (double)p_20040_.getZ());
        return Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(this.getBoundingBox()), BooleanOp.AND);
    }

    public int getTeamColor()
    {
        Team team = this.getTeam();
        return team != null && team.getColor().getColor() != null ? team.getColor().getColor() : 16777215;
    }

    public boolean isSpectator()
    {
        return false;
    }

    public final void unRide()
    {
        if (this.isVehicle())
        {
            this.ejectPassengers();
        }

        if (this.isPassenger())
        {
            this.stopRiding();
        }
    }

    public void setPacketCoordinates(double pX, double p_20169_, double pY)
    {
        this.setPacketCoordinates(new Vec3(pX, p_20169_, pY));
    }

    public void setPacketCoordinates(Vec3 pX)
    {
        this.packetCoordinates = pX;
    }

    public Vec3 getPacketCoordinates()
    {
        return this.packetCoordinates;
    }

    public EntityType<?> getType()
    {
        return this.type;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int pId)
    {
        this.id = pId;
    }

    public Set<String> getTags()
    {
        return this.tags;
    }

    public boolean addTag(String pTag)
    {
        return this.tags.size() >= 1024 ? false : this.tags.add(pTag);
    }

    public boolean removeTag(String pTag)
    {
        return this.tags.remove(pTag);
    }

    public void kill()
    {
        this.remove(Entity.RemovalReason.KILLED);
    }

    public final void discard()
    {
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    protected abstract void defineSynchedData();

    public SynchedEntityData getEntityData()
    {
        return this.entityData;
    }

    public boolean equals(Object p_20245_)
    {
        if (p_20245_ instanceof Entity)
        {
            return ((Entity)p_20245_).id == this.id;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.id;
    }

    public void remove(Entity.RemovalReason p_146834_)
    {
        this.setRemoved(p_146834_);

        if (p_146834_ == Entity.RemovalReason.KILLED)
        {
            this.gameEvent(GameEvent.ENTITY_KILLED);
        }
    }

    public void onClientRemoval()
    {
    }

    public void setPose(Pose pPose)
    {
        this.entityData.set(DATA_POSE, pPose);
    }

    public Pose getPose()
    {
        return this.entityData.get(DATA_POSE);
    }

    public boolean closerThan(Entity pEntity, double pDistance)
    {
        double d0 = pEntity.position.x - this.position.x;
        double d1 = pEntity.position.y - this.position.y;
        double d2 = pEntity.position.z - this.position.z;
        return d0 * d0 + d1 * d1 + d2 * d2 < pDistance * pDistance;
    }

    protected void setRot(float pYaw, float pPitch)
    {
        this.setYRot(pYaw % 360.0F);
        this.setXRot(pPitch % 360.0F);
    }

    public final void setPos(Vec3 pX)
    {
        this.setPos(pX.x(), pX.y(), pX.z());
    }

    public void setPos(double pX, double p_20211_, double pY)
    {
        this.setPosRaw(pX, p_20211_, pY);
        this.setBoundingBox(this.makeBoundingBox());
    }

    protected AABB makeBoundingBox()
    {
        return this.dimensions.makeBoundingBox(this.position);
    }

    protected void reapplyPosition()
    {
        this.setPos(this.position.x, this.position.y, this.position.z);
    }

    public void turn(double pYaw, double p_19886_)
    {
        float f = (float)p_19886_ * 0.15F;
        float f1 = (float)pYaw * 0.15F;
        this.setXRot(this.getXRot() + f);
        this.setYRot(this.getYRot() + f1);
        this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
        this.xRotO += f;
        this.yRotO += f1;
        this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);

        if (this.vehicle != null)
        {
            this.vehicle.onPassengerTurned(this);
        }
    }

    public void tick()
    {
        this.baseTick();
    }

    public void baseTick()
    {
        this.level.getProfiler().push("entityBaseTick");

        if (this.isPassenger() && this.getVehicle().isRemoved())
        {
            this.stopRiding();
        }

        if (this.boardingCooldown > 0)
        {
            --this.boardingCooldown;
        }

        this.walkDistO = this.walkDist;
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.handleNetherPortal();

        if (this.canSpawnSprintParticle())
        {
            this.spawnSprintParticle();
        }

        this.wasInPowderSnow = this.isInPowderSnow;
        this.isInPowderSnow = false;
        this.updateInWaterStateAndDoFluidPushing();
        this.updateFluidOnEyes();
        this.updateSwimming();

        if (this.level.isClientSide)
        {
            this.clearFire();
        }
        else if (this.remainingFireTicks > 0)
        {
            if (this.fireImmune())
            {
                this.setRemainingFireTicks(this.remainingFireTicks - 4);

                if (this.remainingFireTicks < 0)
                {
                    this.clearFire();
                }
            }
            else
            {
                if (this.remainingFireTicks % 20 == 0 && !this.isInLava())
                {
                    this.hurt(DamageSource.ON_FIRE, 1.0F);
                }

                this.setRemainingFireTicks(this.remainingFireTicks - 1);
            }

            if (this.getTicksFrozen() > 0)
            {
                this.setTicksFrozen(0);
                this.level.levelEvent((Player)null, 1009, this.blockPosition, 1);
            }
        }

        if (this.isInLava())
        {
            this.lavaHurt();
            this.fallDistance *= 0.5F;
        }

        this.checkOutOfWorld();

        if (!this.level.isClientSide)
        {
            this.setSharedFlagOnFire(this.remainingFireTicks > 0);
        }

        this.firstTick = false;
        this.level.getProfiler().pop();
    }

    public void setSharedFlagOnFire(boolean p_146869_)
    {
        this.setSharedFlag(0, p_146869_ || this.hasVisualFire);
    }

    public void checkOutOfWorld()
    {
        if (this.getY() < (double)(this.level.getMinBuildHeight() - 64))
        {
            this.outOfWorld();
        }
    }

    public void setPortalCooldown()
    {
        this.portalCooldown = this.getDimensionChangingDelay();
    }

    public boolean isOnPortalCooldown()
    {
        return this.portalCooldown > 0;
    }

    protected void processPortalCooldown()
    {
        if (this.isOnPortalCooldown())
        {
            --this.portalCooldown;
        }
    }

    public int getPortalWaitTime()
    {
        return 0;
    }

    public void lavaHurt()
    {
        if (!this.fireImmune())
        {
            this.setSecondsOnFire(15);

            if (this.hurt(DamageSource.LAVA, 4.0F))
            {
                this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }
        }
    }

    public void setSecondsOnFire(int pSeconds)
    {
        int i = pSeconds * 20;

        if (this instanceof LivingEntity)
        {
            i = ProtectionEnchantment.getFireAfterDampener((LivingEntity)this, i);
        }

        if (this.remainingFireTicks < i)
        {
            this.setRemainingFireTicks(i);
        }
    }

    public void setRemainingFireTicks(int pTicks)
    {
        this.remainingFireTicks = pTicks;
    }

    public int getRemainingFireTicks()
    {
        return this.remainingFireTicks;
    }

    public void clearFire()
    {
        this.setRemainingFireTicks(0);
    }

    protected void outOfWorld()
    {
        this.discard();
    }

    public boolean isFree(double pX, double p_20231_, double pY)
    {
        return this.isFree(this.getBoundingBox().move(pX, p_20231_, pY));
    }

    private boolean isFree(AABB pX)
    {
        return this.level.noCollision(this, pX) && !this.level.containsAnyLiquid(pX);
    }

    public void setOnGround(boolean pGrounded)
    {
        this.onGround = pGrounded;
    }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    public void move(MoverType pType, Vec3 pPos)
    {
        if (this.noPhysics)
        {
            this.setPos(this.getX() + pPos.x, this.getY() + pPos.y, this.getZ() + pPos.z);
        }
        else
        {
            this.wasOnFire = this.isOnFire();

            if (pType == MoverType.PISTON)
            {
                pPos = this.limitPistonMovement(pPos);

                if (pPos.equals(Vec3.ZERO))
                {
                    return;
                }
            }

            this.level.getProfiler().push("move");

            if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7D)
            {
                pPos = pPos.multiply(this.stuckSpeedMultiplier);
                this.stuckSpeedMultiplier = Vec3.ZERO;
                this.setDeltaMovement(Vec3.ZERO);
            }

            pPos = this.maybeBackOffFromEdge(pPos, pType);
            Vec3 vec3 = this.collide(pPos);

            if (vec3.lengthSqr() > 1.0E-7D)
            {
                this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
            }

            this.level.getProfiler().pop();
            this.level.getProfiler().push("rest");
            this.horizontalCollision = !Mth.equal(pPos.x, vec3.x) || !Mth.equal(pPos.z, vec3.z);
            this.verticalCollision = pPos.y != vec3.y;
            this.onGround = this.verticalCollision && pPos.y < 0.0D;
            BlockPos blockpos = this.getOnPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            this.checkFallDamage(vec3.y, this.onGround, blockstate, blockpos);

            if (this.isRemoved())
            {
                this.level.getProfiler().pop();
            }
            else
            {
                Vec3 vec31 = this.getDeltaMovement();

                if (pPos.x != vec3.x)
                {
                    this.setDeltaMovement(0.0D, vec31.y, vec31.z);
                }

                if (pPos.z != vec3.z)
                {
                    this.setDeltaMovement(vec31.x, vec31.y, 0.0D);
                }

                Block block = blockstate.getBlock();

                if (pPos.y != vec3.y)
                {
                    block.updateEntityAfterFallOn(this.level, this);
                }

                if (this.onGround && !this.isSteppingCarefully())
                {
                    block.stepOn(this.level, blockpos, blockstate, this);
                }

                Entity.MovementEmission entity$movementemission = this.getMovementEmission();

                if (entity$movementemission.emitsAnything() && !this.isPassenger())
                {
                    double d0 = vec3.x;
                    double d1 = vec3.y;
                    double d2 = vec3.z;
                    this.flyDist = (float)((double)this.flyDist + vec3.length() * 0.6D);

                    if (!blockstate.is(BlockTags.CLIMBABLE) && !blockstate.is(Blocks.POWDER_SNOW))
                    {
                        d1 = 0.0D;
                    }

                    this.walkDist += (float)vec3.horizontalDistance() * 0.6F;
                    this.moveDist += (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 0.6F;

                    if (this.moveDist > this.nextStep && !blockstate.isAir())
                    {
                        this.nextStep = this.nextStep();

                        if (this.isInWater())
                        {
                            if (entity$movementemission.emitsSounds())
                            {
                                Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                                float f = entity == this ? 0.35F : 0.4F;
                                Vec3 vec32 = entity.getDeltaMovement();
                                float f1 = Math.min(1.0F, (float)Math.sqrt(vec32.x * vec32.x * (double)0.2F + vec32.y * vec32.y + vec32.z * vec32.z * (double)0.2F) * f);
                                this.playSwimSound(f1);
                            }

                            if (entity$movementemission.emitsEvents())
                            {
                                this.gameEvent(GameEvent.SWIM);
                            }
                        }
                        else
                        {
                            if (entity$movementemission.emitsSounds())
                            {
                                this.playAmethystStepSound(blockstate);
                                this.playStepSound(blockpos, blockstate);
                            }

                            if (entity$movementemission.emitsEvents() && !blockstate.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                            {
                                this.gameEvent(GameEvent.STEP);
                            }
                        }
                    }
                    else if (blockstate.isAir())
                    {
                        this.processFlappingMovement();
                    }
                }

                this.tryCheckInsideBlocks();
                float f2 = this.getBlockSpeedFactor();
                this.setDeltaMovement(this.getDeltaMovement().multiply((double)f2, 1.0D, (double)f2));

                if (this.level.getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6D)).noneMatch((p_20127_) ->
            {
                return p_20127_.is(BlockTags.FIRE) || p_20127_.is(Blocks.LAVA);
                }))
                {
                    if (this.remainingFireTicks <= 0)
                    {
                        this.setRemainingFireTicks(-this.getFireImmuneTicks());
                    }

                    if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble()))
                    {
                        this.playEntityOnFireExtinguishedSound();
                    }
                }

                if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble()))
                {
                    this.setRemainingFireTicks(-this.getFireImmuneTicks());
                }

                this.level.getProfiler().pop();
            }
        }
    }

    protected void tryCheckInsideBlocks()
    {
        try
        {
            this.checkInsideBlocks();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    protected void playEntityOnFireExtinguishedSound()
    {
        this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }

    protected void processFlappingMovement()
    {
        if (this.isFlapping())
        {
            this.onFlap();

            if (this.getMovementEmission().emitsEvents())
            {
                this.gameEvent(GameEvent.FLAP);
            }
        }
    }

    public BlockPos getOnPos()
    {
        int i = Mth.floor(this.position.x);
        int j = Mth.floor(this.position.y - (double)0.2F);
        int k = Mth.floor(this.position.z);
        BlockPos blockpos = new BlockPos(i, j, k);

        if (this.level.getBlockState(blockpos).isAir())
        {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = this.level.getBlockState(blockpos1);

            if (blockstate.is(BlockTags.FENCES) || blockstate.is(BlockTags.WALLS) || blockstate.getBlock() instanceof FenceGateBlock)
            {
                return blockpos1;
            }
        }

        return blockpos;
    }

    protected float getBlockJumpFactor()
    {
        float f = this.level.getBlockState(this.blockPosition()).getBlock().getJumpFactor();
        float f1 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
        return (double)f == 1.0D ? f1 : f;
    }

    protected float getBlockSpeedFactor()
    {
        BlockState blockstate = this.level.getBlockState(this.blockPosition());
        float f = blockstate.getBlock().getSpeedFactor();

        if (!blockstate.is(Blocks.WATER) && !blockstate.is(Blocks.BUBBLE_COLUMN))
        {
            return (double)f == 1.0D ? this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
        }
        else
        {
            return f;
        }
    }

    protected BlockPos getBlockPosBelowThatAffectsMyMovement()
    {
        return new BlockPos(this.position.x, this.getBoundingBox().minY - 0.5000001D, this.position.z);
    }

    protected Vec3 maybeBackOffFromEdge(Vec3 pVec, MoverType pMover)
    {
        return pVec;
    }

    protected Vec3 limitPistonMovement(Vec3 pPos)
    {
        if (pPos.lengthSqr() <= 1.0E-7D)
        {
            return pPos;
        }
        else
        {
            long i = this.level.getGameTime();

            if (i != this.pistonDeltasGameTime)
            {
                Arrays.fill(this.pistonDeltas, 0.0D);
                this.pistonDeltasGameTime = i;
            }

            if (pPos.x != 0.0D)
            {
                double d2 = this.applyPistonMovementRestriction(Direction.Axis.X, pPos.x);
                return Math.abs(d2) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(d2, 0.0D, 0.0D);
            }
            else if (pPos.y != 0.0D)
            {
                double d1 = this.applyPistonMovementRestriction(Direction.Axis.Y, pPos.y);
                return Math.abs(d1) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(0.0D, d1, 0.0D);
            }
            else if (pPos.z != 0.0D)
            {
                double d0 = this.applyPistonMovementRestriction(Direction.Axis.Z, pPos.z);
                return Math.abs(d0) <= (double)1.0E-5F ? Vec3.ZERO : new Vec3(0.0D, 0.0D, d0);
            }
            else
            {
                return Vec3.ZERO;
            }
        }
    }

    private double applyPistonMovementRestriction(Direction.Axis pAxis, double pDistance)
    {
        int i = pAxis.ordinal();
        double d0 = Mth.clamp(pDistance + this.pistonDeltas[i], -0.51D, 0.51D);
        pDistance = d0 - this.pistonDeltas[i];
        this.pistonDeltas[i] = d0;
        return pDistance;
    }

    private Vec3 collide(Vec3 pVec)
    {
        AABB aabb = this.getBoundingBox();
        CollisionContext collisioncontext = CollisionContext.of(this);
        VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> stream = Shapes.joinIsNotEmpty(voxelshape, Shapes.create(aabb.deflate(1.0E-7D)), BooleanOp.AND) ? Stream.empty() : Stream.of(voxelshape);
        Stream<VoxelShape> stream1 = this.level.getEntityCollisions(this, aabb.expandTowards(pVec), (p_19949_) ->
        {
            return true;
        });
        RewindableStream<VoxelShape> rewindablestream = new RewindableStream<>(Stream.concat(stream1, stream));
        Vec3 vec3 = pVec.lengthSqr() == 0.0D ? pVec : collideBoundingBoxHeuristically(this, pVec, aabb, this.level, collisioncontext, rewindablestream);
        boolean flag = pVec.x != vec3.x;
        boolean flag1 = pVec.y != vec3.y;
        boolean flag2 = pVec.z != vec3.z;
        boolean flag3 = this.onGround || flag1 && pVec.y < 0.0D;

        if (this.maxUpStep > 0.0F && flag3 && (flag || flag2))
        {
            Vec3 vec31 = collideBoundingBoxHeuristically(this, new Vec3(pVec.x, (double)this.maxUpStep, pVec.z), aabb, this.level, collisioncontext, rewindablestream);
            Vec3 vec32 = collideBoundingBoxHeuristically(this, new Vec3(0.0D, (double)this.maxUpStep, 0.0D), aabb.expandTowards(pVec.x, 0.0D, pVec.z), this.level, collisioncontext, rewindablestream);

            if (vec32.y < (double)this.maxUpStep)
            {
                Vec3 vec33 = collideBoundingBoxHeuristically(this, new Vec3(pVec.x, 0.0D, pVec.z), aabb.move(vec32), this.level, collisioncontext, rewindablestream).add(vec32);

                if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr())
                {
                    vec31 = vec33;
                }
            }

            if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr())
            {
                return vec31.add(collideBoundingBoxHeuristically(this, new Vec3(0.0D, -vec31.y + pVec.y, 0.0D), aabb.move(vec31), this.level, collisioncontext, rewindablestream));
            }
        }

        return vec3;
    }

    public static Vec3 collideBoundingBoxHeuristically(@Nullable Entity pEntity, Vec3 pVec, AABB pCollisionBox, Level pLevel, CollisionContext pContext, RewindableStream<VoxelShape> pPotentialHits)
    {
        boolean flag = pVec.x == 0.0D;
        boolean flag1 = pVec.y == 0.0D;
        boolean flag2 = pVec.z == 0.0D;

        if ((!flag || !flag1) && (!flag || !flag2) && (!flag1 || !flag2))
        {
            RewindableStream<VoxelShape> rewindablestream = new RewindableStream<>(Stream.concat(pPotentialHits.getStream(), pLevel.getBlockCollisions(pEntity, pCollisionBox.expandTowards(pVec))));
            return collideBoundingBoxLegacy(pVec, pCollisionBox, rewindablestream);
        }
        else
        {
            return collideBoundingBox(pVec, pCollisionBox, pLevel, pContext, pPotentialHits);
        }
    }

    public static Vec3 collideBoundingBoxLegacy(Vec3 pVec, AABB pCollisionBox, RewindableStream<VoxelShape> pPotentialHits)
    {
        double d0 = pVec.x;
        double d1 = pVec.y;
        double d2 = pVec.z;

        if (d1 != 0.0D)
        {
            d1 = Shapes.collide(Direction.Axis.Y, pCollisionBox, pPotentialHits.getStream(), d1);

            if (d1 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
            }
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);

        if (flag && d2 != 0.0D)
        {
            d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);

            if (d2 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
            }
        }

        if (d0 != 0.0D)
        {
            d0 = Shapes.collide(Direction.Axis.X, pCollisionBox, pPotentialHits.getStream(), d0);

            if (!flag && d0 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
            }
        }

        if (!flag && d2 != 0.0D)
        {
            d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
        }

        return new Vec3(d0, d1, d2);
    }

    public static Vec3 collideBoundingBox(Vec3 pVec, AABB pCollisionBox, LevelReader pLevel, CollisionContext pSelectionContext, RewindableStream<VoxelShape> pPotentialHits)
    {
        double d0 = pVec.x;
        double d1 = pVec.y;
        double d2 = pVec.z;

        if (d1 != 0.0D)
        {
            d1 = Shapes.collide(Direction.Axis.Y, pCollisionBox, pLevel, d1, pSelectionContext, pPotentialHits.getStream());

            if (d1 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
            }
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);

        if (flag && d2 != 0.0D)
        {
            d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());

            if (d2 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
            }
        }

        if (d0 != 0.0D)
        {
            d0 = Shapes.collide(Direction.Axis.X, pCollisionBox, pLevel, d0, pSelectionContext, pPotentialHits.getStream());

            if (!flag && d0 != 0.0D)
            {
                pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
            }
        }

        if (!flag && d2 != 0.0D)
        {
            d2 = Shapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
        }

        return new Vec3(d0, d1, d2);
    }

    protected float nextStep()
    {
        return (float)((int)this.moveDist + 1);
    }

    protected SoundEvent getSwimSound()
    {
        return SoundEvents.GENERIC_SWIM;
    }

    protected SoundEvent getSwimSplashSound()
    {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected SoundEvent getSwimHighSpeedSplashSound()
    {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected void checkInsideBlocks()
    {
        AABB aabb = this.getBoundingBox();
        BlockPos blockpos = new BlockPos(aabb.minX + 0.001D, aabb.minY + 0.001D, aabb.minZ + 0.001D);
        BlockPos blockpos1 = new BlockPos(aabb.maxX - 0.001D, aabb.maxY - 0.001D, aabb.maxZ - 0.001D);

        if (this.level.hasChunksAt(blockpos, blockpos1))
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i)
            {
                for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j)
                {
                    for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k)
                    {
                        blockpos$mutableblockpos.set(i, j, k);
                        BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);

                        try
                        {
                            blockstate.entityInside(this.level, blockpos$mutableblockpos, this);
                            this.onInsideBlock(blockstate);
                        }
                        catch (Throwable throwable)
                        {
                            CrashReport crashreport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                            CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, blockpos$mutableblockpos, blockstate);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    protected void onInsideBlock(BlockState pState)
    {
    }

    public void gameEvent(GameEvent p_146856_, @Nullable Entity p_146857_, BlockPos p_146858_)
    {
        this.level.gameEvent(p_146857_, p_146856_, p_146858_);
    }

    public void gameEvent(GameEvent p_146853_, @Nullable Entity p_146854_)
    {
        this.gameEvent(p_146853_, p_146854_, this.blockPosition);
    }

    public void gameEvent(GameEvent p_146860_, BlockPos p_146861_)
    {
        this.gameEvent(p_146860_, this, p_146861_);
    }

    public void gameEvent(GameEvent p_146851_)
    {
        this.gameEvent(p_146851_, this.blockPosition);
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock)
    {
        if (!pBlock.getMaterial().isLiquid())
        {
            BlockState blockstate = this.level.getBlockState(pPos.above());
            SoundType soundtype = blockstate.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) ? blockstate.getSoundType() : pBlock.getSoundType();
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        }
    }

    private void playAmethystStepSound(BlockState p_146883_)
    {
        if (p_146883_.is(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.tickCount >= this.lastCrystalSoundPlayTick + 20)
        {
            this.crystalSoundIntensity = (float)((double)this.crystalSoundIntensity * Math.pow((double)0.997F, (double)(this.tickCount - this.lastCrystalSoundPlayTick)));
            this.crystalSoundIntensity = Math.min(1.0F, this.crystalSoundIntensity + 0.07F);
            float f = 0.5F + this.crystalSoundIntensity * this.random.nextFloat() * 1.2F;
            float f1 = 0.1F + this.crystalSoundIntensity * 1.2F;
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, f1, f);
            this.lastCrystalSoundPlayTick = this.tickCount;
        }
    }

    protected void playSwimSound(float pVolume)
    {
        this.playSound(this.getSwimSound(), pVolume, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }

    protected void onFlap()
    {
    }

    protected boolean isFlapping()
    {
        return false;
    }

    public void playSound(SoundEvent pSound, float pVolume, float pPitch)
    {
        if (!this.isSilent())
        {
            this.level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch);
        }
    }

    public boolean isSilent()
    {
        return this.entityData.get(DATA_SILENT);
    }

    public void setSilent(boolean pIsSilent)
    {
        this.entityData.set(DATA_SILENT, pIsSilent);
    }

    public boolean isNoGravity()
    {
        return this.entityData.get(DATA_NO_GRAVITY);
    }

    public void setNoGravity(boolean pNoGravity)
    {
        this.entityData.set(DATA_NO_GRAVITY, pNoGravity);
    }

    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.ALL;
    }

    public boolean occludesVibrations()
    {
        return false;
    }

    protected void checkFallDamage(double pY, boolean p_19912_, BlockState pOnGround, BlockPos pState)
    {
        if (p_19912_)
        {
            if (this.fallDistance > 0.0F)
            {
                pOnGround.getBlock().fallOn(this.level, pOnGround, pState, this, this.fallDistance);

                if (!pOnGround.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                {
                    this.gameEvent(GameEvent.HIT_GROUND);
                }
            }

            this.fallDistance = 0.0F;
        }
        else if (pY < 0.0D)
        {
            this.fallDistance = (float)((double)this.fallDistance - pY);
        }
    }

    public boolean fireImmune()
    {
        return this.getType().fireImmune();
    }

    public boolean causeFallDamage(float p_146828_, float p_146829_, DamageSource p_146830_)
    {
        if (this.isVehicle())
        {
            for (Entity entity : this.getPassengers())
            {
                entity.causeFallDamage(p_146828_, p_146829_, p_146830_);
            }
        }

        return false;
    }

    public boolean isInWater()
    {
        return this.wasTouchingWater;
    }

    private boolean isInRain()
    {
        BlockPos blockpos = this.blockPosition();
        return this.level.isRainingAt(blockpos) || this.level.isRainingAt(new BlockPos((double)blockpos.getX(), this.getBoundingBox().maxY, (double)blockpos.getZ()));
    }

    private boolean isInBubbleColumn()
    {
        return this.level.getBlockState(this.blockPosition()).is(Blocks.BUBBLE_COLUMN);
    }

    public boolean isInWaterOrRain()
    {
        return this.isInWater() || this.isInRain();
    }

    public boolean isInWaterRainOrBubble()
    {
        return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
    }

    public boolean isInWaterOrBubble()
    {
        return this.isInWater() || this.isInBubbleColumn();
    }

    public boolean isUnderWater()
    {
        return this.wasEyeInWater && this.isInWater();
    }

    public void updateSwimming()
    {
        if (this.isSwimming())
        {
            this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
        }
        else
        {
            this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger() && this.level.getFluidState(this.blockPosition).is(FluidTags.WATER));
        }
    }

    protected boolean updateInWaterStateAndDoFluidPushing()
    {
        this.fluidHeight.clear();
        this.updateInWaterStateAndDoWaterCurrentPushing();
        double d0 = this.level.dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
        boolean flag = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d0);
        return this.isInWater() || flag;
    }

    void updateInWaterStateAndDoWaterCurrentPushing()
    {
        if (this.getVehicle() instanceof Boat)
        {
            this.wasTouchingWater = false;
        }
        else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014D))
        {
            if (!this.wasTouchingWater && !this.firstTick)
            {
                this.doWaterSplashEffect();
            }

            this.fallDistance = 0.0F;
            this.wasTouchingWater = true;
            this.clearFire();
        }
        else
        {
            this.wasTouchingWater = false;
        }
    }

    private void updateFluidOnEyes()
    {
        this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
        this.fluidOnEyes = null;
        double d0 = this.getEyeY() - (double)0.11111111F;
        Entity entity = this.getVehicle();

        if (entity instanceof Boat)
        {
            Boat boat = (Boat)entity;

            if (!boat.isUnderWater() && boat.getBoundingBox().maxY >= d0 && boat.getBoundingBox().minY <= d0)
            {
                return;
            }
        }

        BlockPos blockpos = new BlockPos(this.getX(), d0, this.getZ());
        FluidState fluidstate = this.level.getFluidState(blockpos);

        for (Tag<Fluid> tag : FluidTags.getStaticTags())
        {
            if (fluidstate.is(tag))
            {
                double d1 = (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos));

                if (d1 > d0)
                {
                    this.fluidOnEyes = tag;
                }

                return;
            }
        }
    }

    protected void doWaterSplashEffect()
    {
        Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
        float f = entity == this ? 0.2F : 0.9F;
        Vec3 vec3 = entity.getDeltaMovement();
        float f1 = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * (double)0.2F + vec3.y * vec3.y + vec3.z * vec3.z * (double)0.2F) * f);

        if (f1 < 0.25F)
        {
            this.playSound(this.getSwimSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        }
        else
        {
            this.playSound(this.getSwimHighSpeedSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        }

        float f2 = (float)Mth.floor(this.getY());

        for (int i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; ++i)
        {
            double d0 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
            double d1 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
            this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + d0, (double)(f2 + 1.0F), this.getZ() + d1, vec3.x, vec3.y - this.random.nextDouble() * (double)0.2F, vec3.z);
        }

        for (int j = 0; (float)j < 1.0F + this.dimensions.width * 20.0F; ++j)
        {
            double d2 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
            double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
            this.level.addParticle(ParticleTypes.SPLASH, this.getX() + d2, (double)(f2 + 1.0F), this.getZ() + d3, vec3.x, vec3.y, vec3.z);
        }

        this.gameEvent(GameEvent.SPLASH);
    }

    protected BlockState getBlockStateOn()
    {
        return this.level.getBlockState(this.getOnPos());
    }

    public boolean canSpawnSprintParticle()
    {
        return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
    }

    protected void spawnSprintParticle()
    {
        int i = Mth.floor(this.getX());
        int j = Mth.floor(this.getY() - (double)0.2F);
        int k = Mth.floor(this.getZ());
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = this.level.getBlockState(blockpos);

        if (blockstate.getRenderShape() != RenderShape.INVISIBLE)
        {
            Vec3 vec3 = this.getDeltaMovement();
            this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
        }
    }

    public boolean isEyeInFluid(Tag<Fluid> pTag)
    {
        return this.fluidOnEyes == pTag;
    }

    public boolean isInLava()
    {
        return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0D;
    }

    public void moveRelative(float pAmount, Vec3 pRelative)
    {
        Vec3 vec3 = getInputVector(pRelative, pAmount, this.getYRot());
        this.setDeltaMovement(this.getDeltaMovement().add(vec3));
    }

    private static Vec3 getInputVector(Vec3 pRelative, float pMotionScaler, float pFacing)
    {
        double d0 = pRelative.lengthSqr();

        if (d0 < 1.0E-7D)
        {
            return Vec3.ZERO;
        }
        else
        {
            Vec3 vec3 = (d0 > 1.0D ? pRelative.normalize() : pRelative).scale((double)pMotionScaler);
            float f = Mth.sin(pFacing * ((float)Math.PI / 180F));
            float f1 = Mth.cos(pFacing * ((float)Math.PI / 180F));
            return new Vec3(vec3.x * (double)f1 - vec3.z * (double)f, vec3.y, vec3.z * (double)f1 + vec3.x * (double)f);
        }
    }

    public float getBrightness()
    {
        return this.level.hasChunkAt(this.getBlockX(), this.getBlockZ()) ? this.level.getBrightness(new BlockPos(this.getX(), this.getEyeY(), this.getZ())) : 0.0F;
    }

    public void absMoveTo(double pX, double p_19892_, double pY, float p_19894_, float pZ)
    {
        this.absMoveTo(pX, p_19892_, pY);
        this.setYRot(p_19894_ % 360.0F);
        this.setXRot(Mth.clamp(pZ, -90.0F, 90.0F) % 360.0F);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void absMoveTo(double pX, double p_20250_, double pY)
    {
        double d0 = Mth.clamp(pX, -3.0E7D, 3.0E7D);
        double d1 = Mth.clamp(pY, -3.0E7D, 3.0E7D);
        this.xo = d0;
        this.yo = p_20250_;
        this.zo = d1;
        this.setPos(d0, p_20250_, d1);
    }

    public void moveTo(Vec3 pX)
    {
        this.moveTo(pX.x, pX.y, pX.z);
    }

    public void moveTo(double pX, double p_20106_, double pY)
    {
        this.moveTo(pX, p_20106_, pY, this.getYRot(), this.getXRot());
    }

    public void moveTo(BlockPos pX, float p_20037_, float pY)
    {
        this.moveTo((double)pX.getX() + 0.5D, (double)pX.getY(), (double)pX.getZ() + 0.5D, p_20037_, pY);
    }

    public void moveTo(double pX, double p_20109_, double pY, float p_20111_, float pZ)
    {
        this.setPosRaw(pX, p_20109_, pY);
        this.setYRot(p_20111_);
        this.setXRot(pZ);
        this.setOldPosAndRot();
        this.reapplyPosition();
    }

    public final void setOldPosAndRot()
    {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        this.xo = d0;
        this.yo = d1;
        this.zo = d2;
        this.xOld = d0;
        this.yOld = d1;
        this.zOld = d2;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public float distanceTo(Entity pEntity)
    {
        float f = (float)(this.getX() - pEntity.getX());
        float f1 = (float)(this.getY() - pEntity.getY());
        float f2 = (float)(this.getZ() - pEntity.getZ());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public double distanceToSqr(double pX, double p_20277_, double pY)
    {
        double d0 = this.getX() - pX;
        double d1 = this.getY() - p_20277_;
        double d2 = this.getZ() - pY;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceToSqr(Entity pX)
    {
        return this.distanceToSqr(pX.position());
    }

    public double distanceToSqr(Vec3 pX)
    {
        double d0 = this.getX() - pX.x;
        double d1 = this.getY() - pX.y;
        double d2 = this.getZ() - pX.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public void playerTouch(Player pEntity)
    {
    }

    public void push(Entity pX)
    {
        if (!this.isPassengerOfSameVehicle(pX))
        {
            if (!pX.noPhysics && !this.noPhysics)
            {
                double d0 = pX.getX() - this.getX();
                double d1 = pX.getZ() - this.getZ();
                double d2 = Mth.absMax(d0, d1);

                if (d2 >= (double)0.01F)
                {
                    d2 = Math.sqrt(d2);
                    d0 = d0 / d2;
                    d1 = d1 / d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D)
                    {
                        d3 = 1.0D;
                    }

                    d0 = d0 * d3;
                    d1 = d1 * d3;
                    d0 = d0 * (double)0.05F;
                    d1 = d1 * (double)0.05F;

                    if (!this.isVehicle())
                    {
                        this.push(-d0, 0.0D, -d1);
                    }

                    if (!pX.isVehicle())
                    {
                        pX.push(d0, 0.0D, d1);
                    }
                }
            }
        }
    }

    public void push(double pX, double p_20287_, double pY)
    {
        this.setDeltaMovement(this.getDeltaMovement().add(pX, p_20287_, pY));
        this.hasImpulse = true;
    }

    protected void markHurt()
    {
        this.hurtMarked = true;
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else
        {
            this.markHurt();
            return false;
        }
    }

    public final Vec3 getViewVector(float pPartialTicks)
    {
        return this.calculateViewVector(this.getViewXRot(pPartialTicks), this.getViewYRot(pPartialTicks));
    }

    public float getViewXRot(float pPartialTicks)
    {
        return pPartialTicks == 1.0F ? this.getXRot() : Mth.lerp(pPartialTicks, this.xRotO, this.getXRot());
    }

    public float getViewYRot(float pPartialTicks)
    {
        return pPartialTicks == 1.0F ? this.getYRot() : Mth.lerp(pPartialTicks, this.yRotO, this.getYRot());
    }

    protected final Vec3 calculateViewVector(float pPitch, float pYaw)
    {
        float f = pPitch * ((float)Math.PI / 180F);
        float f1 = -pYaw * ((float)Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
    }

    public final Vec3 getUpVector(float pPartialTicks)
    {
        return this.calculateUpVector(this.getViewXRot(pPartialTicks), this.getViewYRot(pPartialTicks));
    }

    protected final Vec3 calculateUpVector(float pPitch, float pYaw)
    {
        return this.calculateViewVector(pPitch - 90.0F, pYaw);
    }

    public final Vec3 getEyePosition()
    {
        return new Vec3(this.getX(), this.getEyeY(), this.getZ());
    }

    public final Vec3 getEyePosition(float pPartialTicks)
    {
        double d0 = Mth.lerp((double)pPartialTicks, this.xo, this.getX());
        double d1 = Mth.lerp((double)pPartialTicks, this.yo, this.getY()) + (double)this.getEyeHeight();
        double d2 = Mth.lerp((double)pPartialTicks, this.zo, this.getZ());
        return new Vec3(d0, d1, d2);
    }

    public Vec3 getLightProbePosition(float pPartialTicks)
    {
        return this.getEyePosition(pPartialTicks);
    }

    public final Vec3 getPosition(float pPartialTicks)
    {
        double d0 = Mth.lerp((double)pPartialTicks, this.xo, this.getX());
        double d1 = Mth.lerp((double)pPartialTicks, this.yo, this.getY());
        double d2 = Mth.lerp((double)pPartialTicks, this.zo, this.getZ());
        return new Vec3(d0, d1, d2);
    }

    public HitResult pick(double pRayTraceDistance, float p_19909_, boolean pPartialTicks)
    {
        Vec3 vec3 = this.getEyePosition(p_19909_);
        Vec3 vec31 = this.getViewVector(p_19909_);
        Vec3 vec32 = vec3.add(vec31.x * pRayTraceDistance, vec31.y * pRayTraceDistance, vec31.z * pRayTraceDistance);
        return this.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, pPartialTicks ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
    }

    public boolean isPickable()
    {
        return false;
    }

    public boolean isPushable()
    {
        return false;
    }

    public void awardKillScore(Entity pKilled, int pScoreValue, DamageSource pDamageSource)
    {
        if (pKilled instanceof ServerPlayer)
        {
            CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)pKilled, this, pDamageSource);
        }
    }

    public boolean shouldRender(double pX, double p_20297_, double pY)
    {
        double d0 = this.getX() - pX;
        double d1 = this.getY() - p_20297_;
        double d2 = this.getZ() - pY;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.shouldRenderAtSqrDistance(d3);
    }

    public boolean shouldRenderAtSqrDistance(double pDistance)
    {
        double d0 = this.getBoundingBox().getSize();

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * viewScale;
        return pDistance < d0 * d0;
    }

    public boolean saveAsPassenger(CompoundTag pCompound)
    {
        if (this.removalReason != null && !this.removalReason.shouldSave())
        {
            return false;
        }
        else
        {
            String s = this.getEncodeId();

            if (s == null)
            {
                return false;
            }
            else
            {
                pCompound.putString("id", s);
                this.saveWithoutId(pCompound);
                return true;
            }
        }
    }

    public boolean save(CompoundTag pCompound)
    {
        return this.isPassenger() ? false : this.saveAsPassenger(pCompound);
    }

    public CompoundTag saveWithoutId(CompoundTag pCompound)
    {
        try
        {
            if (this.vehicle != null)
            {
                pCompound.put("Pos", this.m_20063_(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
            }
            else
            {
                pCompound.put("Pos", this.m_20063_(this.getX(), this.getY(), this.getZ()));
            }

            Vec3 vec3 = this.getDeltaMovement();
            pCompound.put("Motion", this.m_20063_(vec3.x, vec3.y, vec3.z));
            pCompound.put("Rotation", this.m_20065_(this.getYRot(), this.getXRot()));
            pCompound.putFloat("FallDistance", this.fallDistance);
            pCompound.putShort("Fire", (short)this.remainingFireTicks);
            pCompound.putShort("Air", (short)this.getAirSupply());
            pCompound.putBoolean("OnGround", this.onGround);
            pCompound.putBoolean("Invulnerable", this.invulnerable);
            pCompound.putInt("PortalCooldown", this.portalCooldown);
            pCompound.putUUID("UUID", this.getUUID());
            Component component = this.getCustomName();

            if (component != null)
            {
                pCompound.putString("CustomName", Component.Serializer.toJson(component));
            }

            if (this.isCustomNameVisible())
            {
                pCompound.putBoolean("CustomNameVisible", this.isCustomNameVisible());
            }

            if (this.isSilent())
            {
                pCompound.putBoolean("Silent", this.isSilent());
            }

            if (this.isNoGravity())
            {
                pCompound.putBoolean("NoGravity", this.isNoGravity());
            }

            if (this.hasGlowingTag)
            {
                pCompound.putBoolean("Glowing", true);
            }

            int i = this.getTicksFrozen();

            if (i > 0)
            {
                pCompound.putInt("TicksFrozen", this.getTicksFrozen());
            }

            if (this.hasVisualFire)
            {
                pCompound.putBoolean("HasVisualFire", this.hasVisualFire);
            }

            if (!this.tags.isEmpty())
            {
                ListTag listtag = new ListTag();

                for (String s : this.tags)
                {
                    listtag.add(StringTag.valueOf(s));
                }

                pCompound.put("Tags", listtag);
            }

            this.addAdditionalSaveData(pCompound);

            if (this.isVehicle())
            {
                ListTag listtag1 = new ListTag();

                for (Entity entity : this.getPassengers())
                {
                    CompoundTag compoundtag = new CompoundTag();

                    if (entity.saveAsPassenger(compoundtag))
                    {
                        listtag1.add(compoundtag);
                    }
                }

                if (!listtag1.isEmpty())
                {
                    pCompound.put("Passengers", listtag1);
                }
            }

            return pCompound;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Saving entity NBT");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being saved");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    public void load(CompoundTag pCompound)
    {
        try
        {
            ListTag listtag = pCompound.getList("Pos", 6);
            ListTag listtag1 = pCompound.getList("Motion", 6);
            ListTag listtag2 = pCompound.getList("Rotation", 5);
            double d0 = listtag1.getDouble(0);
            double d1 = listtag1.getDouble(1);
            double d2 = listtag1.getDouble(2);
            this.setDeltaMovement(Math.abs(d0) > 10.0D ? 0.0D : d0, Math.abs(d1) > 10.0D ? 0.0D : d1, Math.abs(d2) > 10.0D ? 0.0D : d2);
            this.setPosRaw(listtag.getDouble(0), Mth.clamp(listtag.getDouble(1), -2.0E7D, 2.0E7D), listtag.getDouble(2));
            this.setYRot(listtag2.getFloat(0));
            this.setXRot(listtag2.getFloat(1));
            this.setOldPosAndRot();
            this.setYHeadRot(this.getYRot());
            this.setYBodyRot(this.getYRot());
            this.fallDistance = pCompound.getFloat("FallDistance");
            this.remainingFireTicks = pCompound.getShort("Fire");

            if (pCompound.contains("Air"))
            {
                this.setAirSupply(pCompound.getShort("Air"));
            }

            this.onGround = pCompound.getBoolean("OnGround");
            this.invulnerable = pCompound.getBoolean("Invulnerable");
            this.portalCooldown = pCompound.getInt("PortalCooldown");

            if (pCompound.hasUUID("UUID"))
            {
                this.uuid = pCompound.getUUID("UUID");
                this.stringUUID = this.uuid.toString();
            }

            if (Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ()))
            {
                if (Double.isFinite((double)this.getYRot()) && Double.isFinite((double)this.getXRot()))
                {
                    this.reapplyPosition();
                    this.setRot(this.getYRot(), this.getXRot());

                    if (pCompound.contains("CustomName", 8))
                    {
                        String s = pCompound.getString("CustomName");

                        try
                        {
                            this.setCustomName(Component.Serializer.fromJson(s));
                        }
                        catch (Exception exception)
                        {
                            LOGGER.warn("Failed to parse entity custom name {}", s, exception);
                        }
                    }

                    this.setCustomNameVisible(pCompound.getBoolean("CustomNameVisible"));
                    this.setSilent(pCompound.getBoolean("Silent"));
                    this.setNoGravity(pCompound.getBoolean("NoGravity"));
                    this.setGlowingTag(pCompound.getBoolean("Glowing"));
                    this.setTicksFrozen(pCompound.getInt("TicksFrozen"));
                    this.hasVisualFire = pCompound.getBoolean("HasVisualFire");

                    if (pCompound.contains("Tags", 9))
                    {
                        this.tags.clear();
                        ListTag listtag3 = pCompound.getList("Tags", 8);
                        int i = Math.min(listtag3.size(), 1024);

                        for (int j = 0; j < i; ++j)
                        {
                            this.tags.add(listtag3.getString(j));
                        }
                    }

                    this.readAdditionalSaveData(pCompound);

                    if (this.repositionEntityAfterLoad())
                    {
                        this.reapplyPosition();
                    }
                }
                else
                {
                    throw new IllegalStateException("Entity has invalid rotation");
                }
            }
            else
            {
                throw new IllegalStateException("Entity has invalid position");
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Loading entity NBT");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being loaded");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean repositionEntityAfterLoad()
    {
        return true;
    }

    @Nullable
    protected final String getEncodeId()
    {
        EntityType<?> entitytype = this.getType();
        ResourceLocation resourcelocation = EntityType.getKey(entitytype);
        return entitytype.canSerialize() && resourcelocation != null ? resourcelocation.toString() : null;
    }

    protected abstract void readAdditionalSaveData(CompoundTag pCompound);

    protected abstract void addAdditionalSaveData(CompoundTag pCompound);

    protected ListTag m_20063_(double... p_20064_)
    {
        ListTag listtag = new ListTag();

        for (double d0 : p_20064_)
        {
            listtag.add(DoubleTag.valueOf(d0));
        }

        return listtag;
    }

    protected ListTag m_20065_(float... p_20066_)
    {
        ListTag listtag = new ListTag();

        for (float f : p_20066_)
        {
            listtag.add(FloatTag.valueOf(f));
        }

        return listtag;
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike pStack)
    {
        return this.spawnAtLocation(pStack, 0);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike pStack, int p_20002_)
    {
        return this.spawnAtLocation(new ItemStack(pStack), (float)p_20002_);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack pStack)
    {
        return this.spawnAtLocation(pStack, 0.0F);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack pStack, float p_19986_)
    {
        if (pStack.isEmpty())
        {
            return null;
        }
        else if (this.level.isClientSide)
        {
            return null;
        }
        else
        {
            ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY() + (double)p_19986_, this.getZ(), pStack);
            itementity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itementity);
            return itementity;
        }
    }

    public boolean isAlive()
    {
        return !this.isRemoved();
    }

    public boolean isInWall()
    {
        if (this.noPhysics)
        {
            return false;
        }
        else
        {
            float f = this.dimensions.width * 0.8F;
            AABB aabb = AABB.ofSize(this.getEyePosition(), (double)f, 1.0E-6D, (double)f);
            return this.level.getBlockCollisions(this, aabb, (p_20129_, p_20130_) ->
            {
                return p_20129_.isSuffocating(this.level, p_20130_);
            }).findAny().isPresent();
        }
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand)
    {
        return InteractionResult.PASS;
    }

    public boolean canCollideWith(Entity pEntity)
    {
        return pEntity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(pEntity);
    }

    public boolean canBeCollidedWith()
    {
        return false;
    }

    public void rideTick()
    {
        this.setDeltaMovement(Vec3.ZERO);
        this.tick();

        if (this.isPassenger())
        {
            this.getVehicle().positionRider(this);
        }
    }

    public void positionRider(Entity pPassenger)
    {
        this.positionRider(pPassenger, Entity::setPos);
    }

    private void positionRider(Entity pPassenger, Entity.MoveFunction p_19958_)
    {
        if (this.hasPassenger(pPassenger))
        {
            double d0 = this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset();
            p_19958_.accept(pPassenger, this.getX(), d0, this.getZ());
        }
    }

    public void onPassengerTurned(Entity pEntityToUpdate)
    {
    }

    public double getMyRidingOffset()
    {
        return 0.0D;
    }

    public double getPassengersRidingOffset()
    {
        return (double)this.dimensions.height * 0.75D;
    }

    public boolean startRiding(Entity pEntity)
    {
        return this.startRiding(pEntity, false);
    }

    public boolean showVehicleHealth()
    {
        return this instanceof LivingEntity;
    }

    public boolean startRiding(Entity pEntity, boolean p_19967_)
    {
        if (pEntity == this.vehicle)
        {
            return false;
        }
        else
        {
            for (Entity entity = pEntity; entity.vehicle != null; entity = entity.vehicle)
            {
                if (entity.vehicle == this)
                {
                    return false;
                }
            }

            if (p_19967_ || this.canRide(pEntity) && pEntity.canAddPassenger(this))
            {
                if (this.isPassenger())
                {
                    this.stopRiding();
                }

                this.setPose(Pose.STANDING);
                this.vehicle = pEntity;
                this.vehicle.addPassenger(this);
                pEntity.getIndirectPassengersStream().filter((p_146906_) ->
                {
                    return p_146906_ instanceof ServerPlayer;
                }).forEach((p_146894_) ->
                {
                    CriteriaTriggers.START_RIDING_TRIGGER.trigger((ServerPlayer)p_146894_);
                });
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    protected boolean canRide(Entity pEntity)
    {
        return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
    }

    protected boolean canEnterPose(Pose pPose)
    {
        return this.level.noCollision(this, this.getBoundingBoxForPose(pPose).deflate(1.0E-7D));
    }

    public void ejectPassengers()
    {
        for (int i = this.passengers.size() - 1; i >= 0; --i)
        {
            this.passengers.get(i).stopRiding();
        }
    }

    public void removeVehicle()
    {
        if (this.vehicle != null)
        {
            Entity entity = this.vehicle;
            this.vehicle = null;
            entity.removePassenger(this);
        }
    }

    public void stopRiding()
    {
        this.removeVehicle();
    }

    protected void addPassenger(Entity pPassenger)
    {
        if (pPassenger.getVehicle() != this)
        {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        }
        else
        {
            if (this.passengers.isEmpty())
            {
                this.passengers = ImmutableList.of(pPassenger);
            }
            else
            {
                List<Entity> list = Lists.newArrayList(this.passengers);

                if (!this.level.isClientSide && pPassenger instanceof Player && !(this.getControllingPassenger() instanceof Player))
                {
                    list.add(0, pPassenger);
                }
                else
                {
                    list.add(pPassenger);
                }

                this.passengers = ImmutableList.copyOf(list);
            }
        }
    }

    protected void removePassenger(Entity pPassenger)
    {
        if (pPassenger.getVehicle() == this)
        {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        }
        else
        {
            if (this.passengers.size() == 1 && this.passengers.get(0) == pPassenger)
            {
                this.passengers = ImmutableList.of();
            }
            else
            {
                this.passengers = this.passengers.stream().filter((p_146881_) ->
                {
                    return p_146881_ != pPassenger;
                }).collect(ImmutableList.toImmutableList());
            }

            pPassenger.boardingCooldown = 60;
        }
    }

    protected boolean canAddPassenger(Entity pPassenger)
    {
        return this.passengers.isEmpty();
    }

    public void lerpTo(double pX, double p_19897_, double pY, float p_19899_, float pZ, int p_19901_, boolean pYaw)
    {
        this.setPos(pX, p_19897_, pY);
        this.setRot(p_19899_, pZ);
    }

    public void lerpHeadTo(float pYaw, int pPitch)
    {
        this.setYHeadRot(pYaw);
    }

    public float getPickRadius()
    {
        return 0.0F;
    }

    public Vec3 getLookAngle()
    {
        return this.calculateViewVector(this.getXRot(), this.getYRot());
    }

    public Vec2 getRotationVector()
    {
        return new Vec2(this.getXRot(), this.getYRot());
    }

    public Vec3 getForward()
    {
        return Vec3.directionFromRotation(this.getRotationVector());
    }

    public void handleInsidePortal(BlockPos pPos)
    {
        if (this.isOnPortalCooldown())
        {
            this.setPortalCooldown();
        }
        else
        {
            if (!this.level.isClientSide && !pPos.equals(this.portalEntrancePos))
            {
                this.portalEntrancePos = pPos.immutable();
            }

            this.isInsidePortal = true;
        }
    }

    protected void handleNetherPortal()
    {
        if (this.level instanceof ServerLevel)
        {
            int i = this.getPortalWaitTime();
            ServerLevel serverlevel = (ServerLevel)this.level;

            if (this.isInsidePortal)
            {
                MinecraftServer minecraftserver = serverlevel.getServer();
                ResourceKey<Level> resourcekey = this.level.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
                ServerLevel serverlevel1 = minecraftserver.getLevel(resourcekey);

                if (serverlevel1 != null && minecraftserver.isNetherEnabled() && !this.isPassenger() && this.portalTime++ >= i)
                {
                    this.level.getProfiler().push("portal");
                    this.portalTime = i;
                    this.setPortalCooldown();
                    this.changeDimension(serverlevel1);
                    this.level.getProfiler().pop();
                }

                this.isInsidePortal = false;
            }
            else
            {
                if (this.portalTime > 0)
                {
                    this.portalTime -= 4;
                }

                if (this.portalTime < 0)
                {
                    this.portalTime = 0;
                }
            }

            this.processPortalCooldown();
        }
    }

    public int getDimensionChangingDelay()
    {
        return 300;
    }

    public void lerpMotion(double pX, double p_20307_, double pY)
    {
        this.setDeltaMovement(pX, p_20307_, pY);
    }

    public void handleEntityEvent(byte pId)
    {
        switch (pId)
        {
            case 53:
                HoneyBlock.showSlideParticles(this);

            default:
        }
    }

    public void animateHurt()
    {
    }

    public Iterable<ItemStack> getHandSlots()
    {
        return EMPTY_LIST;
    }

    public Iterable<ItemStack> getArmorSlots()
    {
        return EMPTY_LIST;
    }

    public Iterable<ItemStack> getAllSlots()
    {
        return Iterables.concat(this.getHandSlots(), this.getArmorSlots());
    }

    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack)
    {
    }

    public boolean isOnFire()
    {
        boolean flag = this.level != null && this.level.isClientSide;
        return !this.fireImmune() && (this.remainingFireTicks > 0 || flag && this.getSharedFlag(0));
    }

    public boolean isPassenger()
    {
        return this.getVehicle() != null;
    }

    public boolean isVehicle()
    {
        return !this.passengers.isEmpty();
    }

    public boolean rideableUnderWater()
    {
        return true;
    }

    public void setShiftKeyDown(boolean pKeyDown)
    {
        this.setSharedFlag(1, pKeyDown);
    }

    public boolean isShiftKeyDown()
    {
        return this.getSharedFlag(1);
    }

    public boolean isSteppingCarefully()
    {
        return this.isShiftKeyDown();
    }

    public boolean isSuppressingBounce()
    {
        return this.isShiftKeyDown();
    }

    public boolean isDiscrete()
    {
        return this.isShiftKeyDown();
    }

    public boolean isDescending()
    {
        return this.isShiftKeyDown();
    }

    public boolean isCrouching()
    {
        return this.getPose() == Pose.CROUCHING;
    }

    public boolean isSprinting()
    {
        return this.getSharedFlag(3);
    }

    public void setSprinting(boolean pSprinting)
    {
        this.setSharedFlag(3, pSprinting);
    }

    public boolean isSwimming()
    {
        return this.getSharedFlag(4);
    }

    public boolean isVisuallySwimming()
    {
        return this.getPose() == Pose.SWIMMING;
    }

    public boolean isVisuallyCrawling()
    {
        return this.isVisuallySwimming() && !this.isInWater();
    }

    public void setSwimming(boolean pSwimming)
    {
        this.setSharedFlag(4, pSwimming);
    }

    public final boolean hasGlowingTag()
    {
        return this.hasGlowingTag;
    }

    public final void setGlowingTag(boolean p_146916_)
    {
        this.hasGlowingTag = p_146916_;
        this.setSharedFlag(6, this.isCurrentlyGlowing());
    }

    public boolean isCurrentlyGlowing()
    {
        return this.level.isClientSide() ? this.getSharedFlag(6) : this.hasGlowingTag;
    }

    public boolean isInvisible()
    {
        return this.getSharedFlag(5);
    }

    public boolean isInvisibleTo(Player pPlayer)
    {
        if (pPlayer.isSpectator())
        {
            return false;
        }
        else
        {
            Team team = this.getTeam();
            return team != null && pPlayer != null && pPlayer.getTeam() == team && team.canSeeFriendlyInvisibles() ? false : this.isInvisible();
        }
    }

    @Nullable
    public GameEventListenerRegistrar getGameEventListenerRegistrar()
    {
        return null;
    }

    @Nullable
    public Team getTeam()
    {
        return this.level.getScoreboard().getPlayersTeam(this.getScoreboardName());
    }

    public boolean isAlliedTo(Entity pEntity)
    {
        return this.isAlliedTo(pEntity.getTeam());
    }

    public boolean isAlliedTo(Team pEntity)
    {
        return this.getTeam() != null ? this.getTeam().isAlliedTo(pEntity) : false;
    }

    public void setInvisible(boolean pInvisible)
    {
        this.setSharedFlag(5, pInvisible);
    }

    protected boolean getSharedFlag(int pFlag)
    {
        return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << pFlag) != 0;
    }

    protected void setSharedFlag(int pFlag, boolean pSet)
    {
        byte b0 = this.entityData.get(DATA_SHARED_FLAGS_ID);

        if (pSet)
        {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 | 1 << pFlag));
        }
        else
        {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 & ~(1 << pFlag)));
        }
    }

    public int getMaxAirSupply()
    {
        return 300;
    }

    public int getAirSupply()
    {
        return this.entityData.get(DATA_AIR_SUPPLY_ID);
    }

    public void setAirSupply(int pAir)
    {
        this.entityData.set(DATA_AIR_SUPPLY_ID, pAir);
    }

    public int getTicksFrozen()
    {
        return this.entityData.get(DATA_TICKS_FROZEN);
    }

    public void setTicksFrozen(int p_146918_)
    {
        this.entityData.set(DATA_TICKS_FROZEN, p_146918_);
    }

    public float getPercentFrozen()
    {
        int i = this.getTicksRequiredToFreeze();
        return (float)Math.min(this.getTicksFrozen(), i) / (float)i;
    }

    public boolean isFullyFrozen()
    {
        return this.getTicksFrozen() >= this.getTicksRequiredToFreeze();
    }

    public int getTicksRequiredToFreeze()
    {
        return 140;
    }

    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning)
    {
        this.setRemainingFireTicks(this.remainingFireTicks + 1);

        if (this.remainingFireTicks == 0)
        {
            this.setSecondsOnFire(8);
        }

        this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
    }

    public void onAboveBubbleCol(boolean pDownwards)
    {
        Vec3 vec3 = this.getDeltaMovement();
        double d0;

        if (pDownwards)
        {
            d0 = Math.max(-0.9D, vec3.y - 0.03D);
        }
        else
        {
            d0 = Math.min(1.8D, vec3.y + 0.1D);
        }

        this.setDeltaMovement(vec3.x, d0, vec3.z);
    }

    public void onInsideBubbleColumn(boolean pDownwards)
    {
        Vec3 vec3 = this.getDeltaMovement();
        double d0;

        if (pDownwards)
        {
            d0 = Math.max(-0.3D, vec3.y - 0.03D);
        }
        else
        {
            d0 = Math.min(0.7D, vec3.y + 0.06D);
        }

        this.setDeltaMovement(vec3.x, d0, vec3.z);
        this.fallDistance = 0.0F;
    }

    public void killed(ServerLevel pLevel, LivingEntity pKilledEntity)
    {
    }

    protected void moveTowardsClosestSpace(double pX, double p_20316_, double pY)
    {
        BlockPos blockpos = new BlockPos(pX, p_20316_, pY);
        Vec3 vec3 = new Vec3(pX - (double)blockpos.getX(), p_20316_ - (double)blockpos.getY(), pY - (double)blockpos.getZ());
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        Direction direction = Direction.UP;
        double d0 = Double.MAX_VALUE;

        for (Direction direction1 : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP})
        {
            blockpos$mutableblockpos.setWithOffset(blockpos, direction1);

            if (!this.level.getBlockState(blockpos$mutableblockpos).isCollisionShapeFullBlock(this.level, blockpos$mutableblockpos))
            {
                double d1 = vec3.get(direction1.getAxis());
                double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;

                if (d2 < d0)
                {
                    d0 = d2;
                    direction = direction1;
                }
            }
        }
        float f = this.random.nextFloat() * 0.2F + 0.1F;
        float f1 = (float)direction.getAxisDirection().getStep();
        Vec3 vec31 = this.getDeltaMovement().scale(0.75D);

        if (direction.getAxis() == Direction.Axis.X)
        {
            this.setDeltaMovement((double)(f1 * f), vec31.y, vec31.z);
        }
        else if (direction.getAxis() == Direction.Axis.Y)
        {
            this.setDeltaMovement(vec31.x, (double)(f1 * f), vec31.z);
        }
        else if (direction.getAxis() == Direction.Axis.Z)
        {
            this.setDeltaMovement(vec31.x, vec31.y, (double)(f1 * f));
        }
    }

    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier)
    {
        this.fallDistance = 0.0F;
        this.stuckSpeedMultiplier = pMotionMultiplier;
    }

    private static Component removeAction(Component pName)
    {
        MutableComponent mutablecomponent = pName.plainCopy().setStyle(pName.getStyle().withClickEvent((ClickEvent)null));

        for (Component component : pName.getSiblings())
        {
            mutablecomponent.append(removeAction(component));
        }

        return mutablecomponent;
    }

    public Component getName()
    {
        Component component = this.getCustomName();
        return component != null ? removeAction(component) : this.getTypeName();
    }

    protected Component getTypeName()
    {
        return this.type.getDescription();
    }

    public boolean is(Entity pEntity)
    {
        return this == pEntity;
    }

    public float getYHeadRot()
    {
        return 0.0F;
    }

    public void setYHeadRot(float pRotation)
    {
    }

    public void setYBodyRot(float pOffset)
    {
    }

    public boolean isAttackable()
    {
        return true;
    }

    public boolean skipAttackInteraction(Entity pEntity)
    {
        return false;
    }

    public String toString()
    {
        return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, this.level == null ? "~NULL~" : this.level.toString(), this.getX(), this.getY(), this.getZ());
    }

    public boolean isInvulnerableTo(DamageSource pSource)
    {
        return this.isRemoved() || this.invulnerable && pSource != DamageSource.OUT_OF_WORLD && !pSource.isCreativePlayer();
    }

    public boolean isInvulnerable()
    {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean pIsInvulnerable)
    {
        this.invulnerable = pIsInvulnerable;
    }

    public void copyPosition(Entity pEntity)
    {
        this.moveTo(pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEntity.getYRot(), pEntity.getXRot());
    }

    public void restoreFrom(Entity pEntity)
    {
        CompoundTag compoundtag = pEntity.saveWithoutId(new CompoundTag());
        compoundtag.remove("Dimension");
        this.load(compoundtag);
        this.portalCooldown = pEntity.portalCooldown;
        this.portalEntrancePos = pEntity.portalEntrancePos;
    }

    @Nullable
    public Entity changeDimension(ServerLevel pServer)
    {
        if (this.level instanceof ServerLevel && !this.isRemoved())
        {
            this.level.getProfiler().push("changeDimension");
            this.unRide();
            this.level.getProfiler().push("reposition");
            PortalInfo portalinfo = this.findDimensionEntryPoint(pServer);

            if (portalinfo == null)
            {
                return null;
            }
            else
            {
                this.level.getProfiler().popPush("reloading");
                Entity entity = this.getType().create(pServer);

                if (entity != null)
                {
                    entity.restoreFrom(this);
                    entity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, entity.getXRot());
                    entity.setDeltaMovement(portalinfo.speed);
                    pServer.addDuringTeleport(entity);

                    if (pServer.dimension() == Level.END)
                    {
                        ServerLevel.makeObsidianPlatform(pServer);
                    }
                }

                this.removeAfterChangingDimensions();
                this.level.getProfiler().pop();
                ((ServerLevel)this.level).resetEmptyTime();
                pServer.resetEmptyTime();
                this.level.getProfiler().pop();
                return entity;
            }
        }
        else
        {
            return null;
        }
    }

    protected void removeAfterChangingDimensions()
    {
        this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    @Nullable
    protected PortalInfo findDimensionEntryPoint(ServerLevel p_19923_)
    {
        boolean flag = this.level.dimension() == Level.END && p_19923_.dimension() == Level.OVERWORLD;
        boolean flag1 = p_19923_.dimension() == Level.END;

        if (!flag && !flag1)
        {
            boolean flag2 = p_19923_.dimension() == Level.NETHER;

            if (this.level.dimension() != Level.NETHER && !flag2)
            {
                return null;
            }
            else
            {
                WorldBorder worldborder = p_19923_.getWorldBorder();
                double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
                double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
                double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
                double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
                double d4 = DimensionType.getTeleportationScale(this.level.dimensionType(), p_19923_.dimensionType());
                BlockPos blockpos1 = new BlockPos(Mth.clamp(this.getX() * d4, d0, d2), this.getY(), Mth.clamp(this.getZ() * d4, d1, d3));
                return this.getExitPortal(p_19923_, blockpos1, flag2).map((p_146833_) ->
                {
                    BlockState blockstate = this.level.getBlockState(this.portalEntrancePos);
                    Direction.Axis direction$axis;
                    Vec3 vec3;

                    if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                    {
                        direction$axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                        BlockUtil.FoundRectangle blockutil$foundrectangle = BlockUtil.getLargestRectangleAround(this.portalEntrancePos, direction$axis, 21, Direction.Axis.Y, 21, (p_146847_) ->
                        {
                            return this.level.getBlockState(p_146847_) == blockstate;
                        });
                        vec3 = this.getRelativePortalPosition(direction$axis, blockutil$foundrectangle);
                    }
                    else {
                        direction$axis = Direction.Axis.X;
                        vec3 = new Vec3(0.5D, 0.0D, 0.0D);
                    }

                    return PortalShape.createPortalInfo(p_19923_, p_146833_, direction$axis, vec3, this.getDimensions(this.getPose()), this.getDeltaMovement(), this.getYRot(), this.getXRot());
                }).orElse((PortalInfo)null);
            }
        }
        else
        {
            BlockPos blockpos;

            if (flag1)
            {
                blockpos = ServerLevel.END_SPAWN_POINT;
            }
            else
            {
                blockpos = p_19923_.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p_19923_.getSharedSpawnPos());
            }

            return new PortalInfo(new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), this.getDeltaMovement(), this.getYRot(), this.getXRot());
        }
    }

    protected Vec3 getRelativePortalPosition(Direction.Axis p_20045_, BlockUtil.FoundRectangle p_20046_)
    {
        return PortalShape.getRelativePosition(p_20046_, p_20045_, this.position(), this.getDimensions(this.getPose()));
    }

    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel p_19931_, BlockPos p_19932_, boolean p_19933_)
    {
        return p_19931_.getPortalForcer().findPortalAround(p_19932_, p_19933_);
    }

    public boolean canChangeDimensions()
    {
        return true;
    }

    public float getBlockExplosionResistance(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower)
    {
        return pExplosionPower;
    }

    public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower)
    {
        return true;
    }

    public int getMaxFallDistance()
    {
        return 3;
    }

    public boolean isIgnoringBlockTriggers()
    {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory pCategory)
    {
        pCategory.setDetail("Entity Type", () ->
        {
            return EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")";
        });
        pCategory.setDetail("Entity ID", this.id);
        pCategory.setDetail("Entity Name", () ->
        {
            return this.getName().getString();
        });
        pCategory.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
        pCategory.setDetail("Entity's Block location", CrashReportCategory.formatLocation(this.level, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())));
        Vec3 vec3 = this.getDeltaMovement();
        pCategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3.x, vec3.y, vec3.z));
        pCategory.setDetail("Entity's Passengers", () ->
        {
            return this.getPassengers().toString();
        });
        pCategory.setDetail("Entity's Vehicle", () ->
        {
            return String.valueOf((Object)this.getVehicle());
        });
    }

    public boolean displayFireAnimation()
    {
        return this.isOnFire() && !this.isSpectator();
    }

    public void setUUID(UUID pUniqueId)
    {
        this.uuid = pUniqueId;
        this.stringUUID = this.uuid.toString();
    }

    public UUID getUUID()
    {
        return this.uuid;
    }

    public String getStringUUID()
    {
        return this.stringUUID;
    }

    public String getScoreboardName()
    {
        return this.stringUUID;
    }

    public boolean isPushedByFluid()
    {
        return true;
    }

    public static double getViewScale()
    {
        return viewScale;
    }

    public static void setViewScale(double pRenderDistWeight)
    {
        viewScale = pRenderDistWeight;
    }

    public Component getDisplayName()
    {
        return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName()).withStyle((p_146865_) ->
        {
            return p_146865_.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID());
        });
    }

    public void setCustomName(@Nullable Component pName)
    {
        this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(pName));
    }

    @Nullable
    public Component getCustomName()
    {
        return this.entityData.get(DATA_CUSTOM_NAME).orElse((Component)null);
    }

    public boolean hasCustomName()
    {
        return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
    }

    public void setCustomNameVisible(boolean pAlwaysRenderNameTag)
    {
        this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, pAlwaysRenderNameTag);
    }

    public boolean isCustomNameVisible()
    {
        return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
    }

    public final void teleportToWithTicket(double pX, double p_20326_, double pY)
    {
        if (this.level instanceof ServerLevel)
        {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(pX, p_20326_, pY));
            ((ServerLevel)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0, this.getId());
            this.level.getChunk(chunkpos.x, chunkpos.z);
            this.teleportTo(pX, p_20326_, pY);
        }
    }

    public void dismountTo(double p_146825_, double p_146826_, double p_146827_)
    {
        this.teleportTo(p_146825_, p_146826_, p_146827_);
    }

    public void teleportTo(double pX, double p_19888_, double pY)
    {
        if (this.level instanceof ServerLevel)
        {
            this.moveTo(pX, p_19888_, pY, this.getYRot(), this.getXRot());
            this.getSelfAndPassengers().forEach((p_146878_) ->
            {
                for (Entity entity : p_146878_.passengers)
                {
                    p_146878_.positionRider(entity, Entity::moveTo);
                }
            });
        }
    }

    public boolean shouldShowName()
    {
        return this.isCustomNameVisible();
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey)
    {
        if (DATA_POSE.equals(pKey))
        {
            this.refreshDimensions();
        }
    }

    public void refreshDimensions()
    {
        EntityDimensions entitydimensions = this.dimensions;
        Pose pose = this.getPose();
        EntityDimensions entitydimensions1 = this.getDimensions(pose);
        this.dimensions = entitydimensions1;
        this.eyeHeight = this.getEyeHeight(pose, entitydimensions1);
        this.reapplyPosition();
        boolean flag = (double)entitydimensions1.width <= 4.0D && (double)entitydimensions1.height <= 4.0D;

        if (!this.level.isClientSide && !this.firstTick && !this.noPhysics && flag && (entitydimensions1.width > entitydimensions.width || entitydimensions1.height > entitydimensions.height) && !(this instanceof Player))
        {
            Vec3 vec3 = this.position().add(0.0D, (double)entitydimensions.height / 2.0D, 0.0D);
            double d0 = (double)Math.max(0.0F, entitydimensions1.width - entitydimensions.width) + 1.0E-6D;
            double d1 = (double)Math.max(0.0F, entitydimensions1.height - entitydimensions.height) + 1.0E-6D;
            VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec3, d0, d1, d0));
            this.level.findFreePosition(this, voxelshape, vec3, (double)entitydimensions1.width, (double)entitydimensions1.height, (double)entitydimensions1.width).ifPresent((p_146842_) ->
            {
                this.setPos(p_146842_.add(0.0D, (double)(-entitydimensions1.height) / 2.0D, 0.0D));
            });
        }
    }

    public Direction getDirection()
    {
        return Direction.fromYRot((double)this.getYRot());
    }

    public Direction getMotionDirection()
    {
        return this.getDirection();
    }

    protected HoverEvent createHoverEvent()
    {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(this.getType(), this.getUUID(), this.getName()));
    }

    public boolean broadcastToPlayer(ServerPlayer pPlayer)
    {
        return true;
    }

    public final AABB getBoundingBox()
    {
        return this.bb;
    }

    public AABB getBoundingBoxForCulling()
    {
        return this.getBoundingBox();
    }

    protected AABB getBoundingBoxForPose(Pose pPose)
    {
        EntityDimensions entitydimensions = this.getDimensions(pPose);
        float f = entitydimensions.width / 2.0F;
        Vec3 vec3 = new Vec3(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
        Vec3 vec31 = new Vec3(this.getX() + (double)f, this.getY() + (double)entitydimensions.height, this.getZ() + (double)f);
        return new AABB(vec3, vec31);
    }

    public final void setBoundingBox(AABB pBb)
    {
        this.bb = pBb;
    }

    protected float getEyeHeight(Pose pPose, EntityDimensions p_19977_)
    {
        return p_19977_.height * 0.85F;
    }

    public float getEyeHeight(Pose pPose)
    {
        return this.getEyeHeight(pPose, this.getDimensions(pPose));
    }

    public final float getEyeHeight()
    {
        return this.eyeHeight;
    }

    public Vec3 getLeashOffset()
    {
        return new Vec3(0.0D, (double)this.getEyeHeight(), (double)(this.getBbWidth() * 0.4F));
    }

    public SlotAccess getSlot(int p_146919_)
    {
        return SlotAccess.NULL;
    }

    public void sendMessage(Component pComponent, UUID pSenderUUID)
    {
    }

    public Level getCommandSenderWorld()
    {
        return this.level;
    }

    @Nullable
    public MinecraftServer getServer()
    {
        return this.level.getServer();
    }

    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand)
    {
        return InteractionResult.PASS;
    }

    public boolean ignoreExplosion()
    {
        return false;
    }

    public void doEnchantDamageEffects(LivingEntity pLivingEntity, Entity pEntity)
    {
        if (pEntity instanceof LivingEntity)
        {
            EnchantmentHelper.doPostHurtEffects((LivingEntity)pEntity, pLivingEntity);
        }

        EnchantmentHelper.doPostDamageEffects(pLivingEntity, pEntity);
    }

    public void startSeenByPlayer(ServerPlayer pPlayer)
    {
    }

    public void stopSeenByPlayer(ServerPlayer pPlayer)
    {
    }

    public float rotate(Rotation pTransformRotation)
    {
        float f = Mth.wrapDegrees(this.getYRot());

        switch (pTransformRotation)
        {
            case CLOCKWISE_180:
                return f + 180.0F;

            case COUNTERCLOCKWISE_90:
                return f + 270.0F;

            case CLOCKWISE_90:
                return f + 90.0F;

            default:
                return f;
        }
    }

    public float mirror(Mirror pTransformMirror)
    {
        float f = Mth.wrapDegrees(this.getYRot());

        switch (pTransformMirror)
        {
            case LEFT_RIGHT:
                return -f;

            case FRONT_BACK:
                return 180.0F - f;

            default:
                return f;
        }
    }

    public boolean onlyOpCanSetNbt()
    {
        return false;
    }

    @Nullable
    public Entity getControllingPassenger()
    {
        return null;
    }

    public final List<Entity> getPassengers()
    {
        return this.passengers;
    }

    @Nullable
    public Entity getFirstPassenger()
    {
        return this.passengers.isEmpty() ? null : this.passengers.get(0);
    }

    public boolean hasPassenger(Entity pEntity)
    {
        return this.passengers.contains(pEntity);
    }

    public boolean hasPassenger(Predicate<Entity> pEntity)
    {
        for (Entity entity : this.passengers)
        {
            if (pEntity.test(entity))
            {
                return true;
            }
        }

        return false;
    }

    private Stream<Entity> getIndirectPassengersStream()
    {
        return this.passengers.stream().flatMap(Entity::getSelfAndPassengers);
    }

    public Stream<Entity> getSelfAndPassengers()
    {
        return Stream.concat(Stream.of(this), this.getIndirectPassengersStream());
    }

    public Stream<Entity> getPassengersAndSelf()
    {
        return Stream.concat(this.passengers.stream().flatMap(Entity::getPassengersAndSelf), Stream.of(this));
    }

    public Iterable<Entity> getIndirectPassengers()
    {
        return () ->
        {
            return this.getIndirectPassengersStream().iterator();
        };
    }

    public boolean hasExactlyOnePlayerPassenger()
    {
        return this.getIndirectPassengersStream().filter((p_146836_) ->
        {
            return p_146836_ instanceof Player;
        }).count() == 1L;
    }

    public Entity getRootVehicle()
    {
        Entity entity;

        for (entity = this; entity.isPassenger(); entity = entity.getVehicle())
        {
        }

        return entity;
    }

    public boolean isPassengerOfSameVehicle(Entity pEntity)
    {
        return this.getRootVehicle() == pEntity.getRootVehicle();
    }

    public boolean hasIndirectPassenger(Entity pEntity)
    {
        return this.getIndirectPassengersStream().anyMatch((p_146839_) ->
        {
            return p_146839_ == pEntity;
        });
    }

    public boolean isControlledByLocalInstance()
    {
        Entity entity = this.getControllingPassenger();

        if (entity instanceof Player)
        {
            return ((Player)entity).isLocalPlayer();
        }
        else
        {
            return !this.level.isClientSide;
        }
    }

    protected static Vec3 getCollisionHorizontalEscapeVector(double pMountWidth, double p_19905_, float pRiderWidth)
    {
        double d0 = (pMountWidth + p_19905_ + (double)1.0E-5F) / 2.0D;
        float f = -Mth.sin(pRiderWidth * ((float)Math.PI / 180F));
        float f1 = Mth.cos(pRiderWidth * ((float)Math.PI / 180F));
        float f2 = Math.max(Math.abs(f), Math.abs(f1));
        return new Vec3((double)f * d0 / (double)f2, 0.0D, (double)f1 * d0 / (double)f2);
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity)
    {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Nullable
    public Entity getVehicle()
    {
        return this.vehicle;
    }

    public PushReaction getPistonPushReaction()
    {
        return PushReaction.NORMAL;
    }

    public SoundSource getSoundSource()
    {
        return SoundSource.NEUTRAL;
    }

    protected int getFireImmuneTicks()
    {
        return 1;
    }

    public CommandSourceStack createCommandSourceStack()
    {
        return new CommandSourceStack(this, this.position(), this.getRotationVector(), this.level instanceof ServerLevel ? (ServerLevel)this.level : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.level.getServer(), this);
    }

    protected int getPermissionLevel()
    {
        return 0;
    }

    public boolean hasPermissions(int pLevel)
    {
        return this.getPermissionLevel() >= pLevel;
    }

    public boolean acceptsSuccess()
    {
        return this.level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
    }

    public boolean acceptsFailure()
    {
        return true;
    }

    public boolean shouldInformAdmins()
    {
        return true;
    }

    public void lookAt(EntityAnchorArgument.Anchor pAnchor, Vec3 pTarget)
    {
        Vec3 vec3 = pAnchor.apply(this);
        double d0 = pTarget.x - vec3.x;
        double d1 = pTarget.y - vec3.y;
        double d2 = pTarget.z - vec3.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI)))));
        this.setYRot(Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F));
        this.setYHeadRot(this.getYRot());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> pFluidTag, double pMotionScale)
    {
        if (this.touchingUnloadedChunk())
        {
            return false;
        }
        else
        {
            AABB aabb = this.getBoundingBox().deflate(0.001D);
            int i = Mth.floor(aabb.minX);
            int j = Mth.ceil(aabb.maxX);
            int k = Mth.floor(aabb.minY);
            int l = Mth.ceil(aabb.maxY);
            int i1 = Mth.floor(aabb.minZ);
            int j1 = Mth.ceil(aabb.maxZ);
            double d0 = 0.0D;
            boolean flag = this.isPushedByFluid();
            boolean flag1 = false;
            Vec3 vec3 = Vec3.ZERO;
            int k1 = 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int l1 = i; l1 < j; ++l1)
            {
                for (int i2 = k; i2 < l; ++i2)
                {
                    for (int j2 = i1; j2 < j1; ++j2)
                    {
                        blockpos$mutableblockpos.set(l1, i2, j2);
                        FluidState fluidstate = this.level.getFluidState(blockpos$mutableblockpos);

                        if (fluidstate.is(pFluidTag))
                        {
                            double d1 = (double)((float)i2 + fluidstate.getHeight(this.level, blockpos$mutableblockpos));

                            if (d1 >= aabb.minY)
                            {
                                flag1 = true;
                                d0 = Math.max(d1 - aabb.minY, d0);

                                if (flag)
                                {
                                    Vec3 vec31 = fluidstate.getFlow(this.level, blockpos$mutableblockpos);

                                    if (d0 < 0.4D)
                                    {
                                        vec31 = vec31.scale(d0);
                                    }

                                    vec3 = vec3.add(vec31);
                                    ++k1;
                                }
                            }
                        }
                    }
                }
            }

            if (vec3.length() > 0.0D)
            {
                if (k1 > 0)
                {
                    vec3 = vec3.scale(1.0D / (double)k1);
                }

                if (!(this instanceof Player))
                {
                    vec3 = vec3.normalize();
                }

                Vec3 vec32 = this.getDeltaMovement();
                vec3 = vec3.scale(pMotionScale * 1.0D);
                double d2 = 0.003D;

                if (Math.abs(vec32.x) < 0.003D && Math.abs(vec32.z) < 0.003D && vec3.length() < 0.0045000000000000005D)
                {
                    vec3 = vec3.normalize().scale(0.0045000000000000005D);
                }

                this.setDeltaMovement(this.getDeltaMovement().add(vec3));
            }

            this.fluidHeight.put(pFluidTag, d0);
            return flag1;
        }
    }

    public boolean touchingUnloadedChunk()
    {
        AABB aabb = this.getBoundingBox().inflate(1.0D);
        int i = Mth.floor(aabb.minX);
        int j = Mth.ceil(aabb.maxX);
        int k = Mth.floor(aabb.minZ);
        int l = Mth.ceil(aabb.maxZ);
        return !this.level.hasChunksAt(i, k, j, l);
    }

    public double getFluidHeight(Tag<Fluid> p_20121_)
    {
        return this.fluidHeight.getDouble(p_20121_);
    }

    public double getFluidJumpThreshold()
    {
        return (double)this.getEyeHeight() < 0.4D ? 0.0D : 0.4D;
    }

    public final float getBbWidth()
    {
        return this.dimensions.width;
    }

    public final float getBbHeight()
    {
        return this.dimensions.height;
    }

    public abstract Packet<?> getAddEntityPacket();

    public EntityDimensions getDimensions(Pose pPose)
    {
        return this.type.getDimensions();
    }

    public Vec3 position()
    {
        return this.position;
    }

    public BlockPos blockPosition()
    {
        return this.blockPosition;
    }

    public BlockState getFeetBlockState()
    {
        return this.level.getBlockState(this.blockPosition());
    }

    public BlockPos eyeBlockPosition()
    {
        return new BlockPos(this.getEyePosition(1.0F));
    }

    public ChunkPos chunkPosition()
    {
        return new ChunkPos(this.blockPosition);
    }

    public Vec3 getDeltaMovement()
    {
        return this.deltaMovement;
    }

    public void setDeltaMovement(Vec3 pX)
    {
        this.deltaMovement = pX;
    }

    public void setDeltaMovement(double pX, double p_20336_, double pY)
    {
        this.setDeltaMovement(new Vec3(pX, p_20336_, pY));
    }

    public final int getBlockX()
    {
        return this.blockPosition.getX();
    }

    public final double getX()
    {
        return this.position.x;
    }

    public double getX(double pScale)
    {
        return this.position.x + (double)this.getBbWidth() * pScale;
    }

    public double getRandomX(double pScale)
    {
        return this.getX((2.0D * this.random.nextDouble() - 1.0D) * pScale);
    }

    public final int getBlockY()
    {
        return this.blockPosition.getY();
    }

    public final double getY()
    {
        return this.position.y;
    }

    public double getY(double pScale)
    {
        return this.position.y + (double)this.getBbHeight() * pScale;
    }

    public double getRandomY()
    {
        return this.getY(this.random.nextDouble());
    }

    public double getEyeY()
    {
        return this.position.y + (double)this.eyeHeight;
    }

    public final int getBlockZ()
    {
        return this.blockPosition.getZ();
    }

    public final double getZ()
    {
        return this.position.z;
    }

    public double getZ(double pScale)
    {
        return this.position.z + (double)this.getBbWidth() * pScale;
    }

    public double getRandomZ(double pScale)
    {
        return this.getZ((2.0D * this.random.nextDouble() - 1.0D) * pScale);
    }

    public final void setPosRaw(double pX, double p_20345_, double pY)
    {
        if (this.position.x != pX || this.position.y != p_20345_ || this.position.z != pY)
        {
            this.position = new Vec3(pX, p_20345_, pY);
            int i = Mth.floor(pX);
            int j = Mth.floor(p_20345_);
            int k = Mth.floor(pY);

            if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ())
            {
                this.blockPosition = new BlockPos(i, j, k);
            }

            this.levelCallback.onMove();
            GameEventListenerRegistrar gameeventlistenerregistrar = this.getGameEventListenerRegistrar();

            if (gameeventlistenerregistrar != null)
            {
                gameeventlistenerregistrar.onListenerMove(this.level);
            }
        }
    }

    public void checkDespawn()
    {
    }

    public Vec3 getRopeHoldPosition(float pPartialTicks)
    {
        return this.getPosition(pPartialTicks).add(0.0D, (double)this.eyeHeight * 0.7D, 0.0D);
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_146866_)
    {
        int i = p_146866_.getId();
        double d0 = p_146866_.getX();
        double d1 = p_146866_.getY();
        double d2 = p_146866_.getZ();
        this.setPacketCoordinates(d0, d1, d2);
        this.moveTo(d0, d1, d2);
        this.setXRot((float)(p_146866_.getxRot() * 360) / 256.0F);
        this.setYRot((float)(p_146866_.getyRot() * 360) / 256.0F);
        this.setId(i);
        this.setUUID(p_146866_.getUUID());
    }

    @Nullable
    public ItemStack getPickResult()
    {
        return null;
    }

    public void setIsInPowderSnow(boolean p_146925_)
    {
        this.isInPowderSnow = p_146925_;
    }

    public boolean canFreeze()
    {
        return !EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES.contains(this.getType());
    }

    public float getYRot()
    {
        return this.yRot;
    }

    public void setYRot(float p_146923_)
    {
        if (!Float.isFinite(p_146923_))
        {
            Util.logAndPauseIfInIde("Invalid entity rotation: " + p_146923_ + ", discarding.");
        }
        else
        {
            this.yRot = p_146923_;
        }
    }

    public float getXRot()
    {
        return this.xRot;
    }

    public void setXRot(float p_146927_)
    {
        if (!Float.isFinite(p_146927_))
        {
            Util.logAndPauseIfInIde("Invalid entity rotation: " + p_146927_ + ", discarding.");
        }
        else
        {
            this.xRot = p_146927_;
        }
    }

    public final boolean isRemoved()
    {
        return this.removalReason != null;
    }

    @Nullable
    public Entity.RemovalReason getRemovalReason()
    {
        return this.removalReason;
    }

    public final void setRemoved(Entity.RemovalReason p_146876_)
    {
        if (this.removalReason == null)
        {
            this.removalReason = p_146876_;
        }

        if (this.removalReason.shouldDestroy())
        {
            this.stopRiding();
        }

        this.getPassengers().forEach(Entity::stopRiding);
        this.levelCallback.onRemove(p_146876_);
    }

    protected void unsetRemoved()
    {
        this.removalReason = null;
    }

    public void setLevelCallback(EntityInLevelCallback p_146849_)
    {
        this.levelCallback = p_146849_;
    }

    public boolean shouldBeSaved()
    {
        if (this.removalReason != null && !this.removalReason.shouldSave())
        {
            return false;
        }
        else if (this.isPassenger())
        {
            return false;
        }
        else
        {
            return !this.isVehicle() || !this.hasExactlyOnePlayerPassenger();
        }
    }

    public boolean isAlwaysTicking()
    {
        return false;
    }

    public boolean mayInteract(Level p_146843_, BlockPos p_146844_)
    {
        return true;
    }

    @FunctionalInterface
    public interface MoveFunction
    {
        void accept(Entity p_20373_, double p_20374_, double p_20375_, double p_20376_);
    }

    public static enum MovementEmission
    {
        NONE(false, false),
        SOUNDS(true, false),
        EVENTS(false, true),
        ALL(true, true);

        final boolean sounds;
        final boolean events;

        private MovementEmission(boolean p_146942_, boolean p_146943_)
        {
            this.sounds = p_146942_;
            this.events = p_146943_;
        }

        public boolean emitsAnything()
        {
            return this.events || this.sounds;
        }

        public boolean emitsEvents()
        {
            return this.events;
        }

        public boolean emitsSounds()
        {
            return this.sounds;
        }
    }

    public static enum RemovalReason
    {
        KILLED(true, false),
        DISCARDED(true, false),
        UNLOADED_TO_CHUNK(false, true),
        UNLOADED_WITH_PLAYER(false, false),
        CHANGED_DIMENSION(false, false);

        private final boolean destroy;
        private final boolean save;

        private RemovalReason(boolean p_146963_, boolean p_146964_)
        {
            this.destroy = p_146963_;
            this.save = p_146964_;
        }

        public boolean shouldDestroy()
        {
            return this.destroy;
        }

        public boolean shouldSave()
        {
            return this.save;
        }
    }
}
