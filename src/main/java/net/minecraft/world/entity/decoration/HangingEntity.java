package net.minecraft.world.entity.decoration;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public abstract class HangingEntity extends Entity
{
    protected static final Predicate<Entity> HANGING_ENTITY = (p_31734_) ->
    {
        return p_31734_ instanceof HangingEntity;
    };
    private int checkInterval;
    protected BlockPos pos;
    protected Direction direction = Direction.SOUTH;

    protected HangingEntity(EntityType <? extends HangingEntity > p_31703_, Level p_31704_)
    {
        super(p_31703_, p_31704_);
    }

    protected HangingEntity(EntityType <? extends HangingEntity > p_31706_, Level p_31707_, BlockPos p_31708_)
    {
        this(p_31706_, p_31707_);
        this.pos = p_31708_;
    }

    protected void defineSynchedData()
    {
    }

    protected void setDirection(Direction pFacingDirection)
    {
        Validate.notNull(pFacingDirection);
        Validate.isTrue(pFacingDirection.getAxis().isHorizontal());
        this.direction = pFacingDirection;
        this.setYRot((float)(this.direction.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    protected void recalculateBoundingBox()
    {
        if (this.direction != null)
        {
            double d0 = (double)this.pos.getX() + 0.5D;
            double d1 = (double)this.pos.getY() + 0.5D;
            double d2 = (double)this.pos.getZ() + 0.5D;
            double d3 = 0.46875D;
            double d4 = this.offs(this.getWidth());
            double d5 = this.offs(this.getHeight());
            d0 = d0 - (double)this.direction.getStepX() * 0.46875D;
            d2 = d2 - (double)this.direction.getStepZ() * 0.46875D;
            d1 = d1 + d5;
            Direction direction = this.direction.getCounterClockWise();
            d0 = d0 + d4 * (double)direction.getStepX();
            d2 = d2 + d4 * (double)direction.getStepZ();
            this.setPosRaw(d0, d1, d2);
            double d6 = (double)this.getWidth();
            double d7 = (double)this.getHeight();
            double d8 = (double)this.getWidth();

            if (this.direction.getAxis() == Direction.Axis.Z)
            {
                d8 = 1.0D;
            }
            else
            {
                d6 = 1.0D;
            }

            d6 = d6 / 32.0D;
            d7 = d7 / 32.0D;
            d8 = d8 / 32.0D;
            this.setBoundingBox(new AABB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
        }
    }

    private double offs(int p_31710_)
    {
        return p_31710_ % 32 == 0 ? 0.5D : 0.0D;
    }

    public void tick()
    {
        if (!this.level.isClientSide)
        {
            this.checkOutOfWorld();

            if (this.checkInterval++ == 100)
            {
                this.checkInterval = 0;

                if (!this.isRemoved() && !this.survives())
                {
                    this.discard();
                    this.dropItem((Entity)null);
                }
            }
        }
    }

    public boolean survives()
    {
        if (!this.level.noCollision(this))
        {
            return false;
        }
        else
        {
            int i = Math.max(1, this.getWidth() / 16);
            int j = Math.max(1, this.getHeight() / 16);
            BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
            Direction direction = this.direction.getCounterClockWise();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k = 0; k < i; ++k)
            {
                for (int l = 0; l < j; ++l)
                {
                    int i1 = (i - 1) / -2;
                    int j1 = (j - 1) / -2;
                    blockpos$mutableblockpos.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
                    BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);

                    if (!blockstate.getMaterial().isSolid() && !DiodeBlock.isDiode(blockstate))
                    {
                        return false;
                    }
                }
            }

            return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    public boolean isPickable()
    {
        return true;
    }

    public boolean skipAttackInteraction(Entity pEntity)
    {
        if (pEntity instanceof Player)
        {
            Player player = (Player)pEntity;
            return !this.level.mayInteract(player, this.pos) ? true : this.hurt(DamageSource.playerAttack(player), 0.0F);
        }
        else
        {
            return false;
        }
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else
        {
            if (!this.isRemoved() && !this.level.isClientSide)
            {
                this.kill();
                this.markHurt();
                this.dropItem(pSource.getEntity());
            }

            return true;
        }
    }

    public void move(MoverType pType, Vec3 pPos)
    {
        if (!this.level.isClientSide && !this.isRemoved() && pPos.lengthSqr() > 0.0D)
        {
            this.kill();
            this.dropItem((Entity)null);
        }
    }

    public void push(double pX, double p_31745_, double pY)
    {
        if (!this.level.isClientSide && !this.isRemoved() && pX * pX + p_31745_ * p_31745_ + pY * pY > 0.0D)
        {
            this.kill();
            this.dropItem((Entity)null);
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        BlockPos blockpos = this.getPos();
        pCompound.putInt("TileX", blockpos.getX());
        pCompound.putInt("TileY", blockpos.getY());
        pCompound.putInt("TileZ", blockpos.getZ());
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        this.pos = new BlockPos(pCompound.getInt("TileX"), pCompound.getInt("TileY"), pCompound.getInt("TileZ"));
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void dropItem(@Nullable Entity pBrokenEntity);

    public abstract void playPlacementSound();

    public ItemEntity spawnAtLocation(ItemStack pStack, float pOffsetY)
    {
        ItemEntity itementity = new ItemEntity(this.level, this.getX() + (double)((float)this.direction.getStepX() * 0.15F), this.getY() + (double)pOffsetY, this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F), pStack);
        itementity.setDefaultPickUpDelay();
        this.level.addFreshEntity(itementity);
        return itementity;
    }

    protected boolean repositionEntityAfterLoad()
    {
        return false;
    }

    public void setPos(double pX, double p_31740_, double pY)
    {
        this.pos = new BlockPos(pX, p_31740_, pY);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public float rotate(Rotation pTransformRotation)
    {
        if (this.direction.getAxis() != Direction.Axis.Y)
        {
            switch (pTransformRotation)
            {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;

                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;

                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
            }
        }

        float f = Mth.wrapDegrees(this.getYRot());

        switch (pTransformRotation)
        {
            case CLOCKWISE_180:
                return f + 180.0F;

            case COUNTERCLOCKWISE_90:
                return f + 90.0F;

            case CLOCKWISE_90:
                return f + 270.0F;

            default:
                return f;
        }
    }

    public float mirror(Mirror pTransformMirror)
    {
        return this.rotate(pTransformMirror.getRotation(this.direction));
    }

    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning)
    {
    }

    public void refreshDimensions()
    {
    }
}
