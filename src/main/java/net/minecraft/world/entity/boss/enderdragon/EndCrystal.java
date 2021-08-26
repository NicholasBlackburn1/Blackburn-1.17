package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndCrystal extends Entity
{
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    public int time;

    public EndCrystal(EntityType <? extends EndCrystal > p_31037_, Level p_31038_)
    {
        super(p_31037_, p_31038_);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level p_31040_, double p_31041_, double p_31042_, double p_31043_)
    {
        this(EntityType.END_CRYSTAL, p_31040_);
        this.setPos(p_31041_, p_31042_, p_31043_);
    }

    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    protected void defineSynchedData()
    {
        this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
        this.getEntityData().define(DATA_SHOW_BOTTOM, true);
    }

    public void tick()
    {
        ++this.time;

        if (this.level instanceof ServerLevel)
        {
            BlockPos blockpos = this.blockPosition();

            if (((ServerLevel)this.level).dragonFight() != null && this.level.getBlockState(blockpos).isAir())
            {
                this.level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level, blockpos));
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        if (this.getBeamTarget() != null)
        {
            pCompound.put("BeamTarget", NbtUtils.writeBlockPos(this.getBeamTarget()));
        }

        pCompound.putBoolean("ShowBottom", this.showsBottom());
    }

    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        if (pCompound.contains("BeamTarget", 10))
        {
            this.setBeamTarget(NbtUtils.readBlockPos(pCompound.getCompound("BeamTarget")));
        }

        if (pCompound.contains("ShowBottom", 1))
        {
            this.setShowBottom(pCompound.getBoolean("ShowBottom"));
        }
    }

    public boolean isPickable()
    {
        return true;
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        if (this.isInvulnerableTo(pSource))
        {
            return false;
        }
        else if (pSource.getEntity() instanceof EnderDragon)
        {
            return false;
        }
        else
        {
            if (!this.isRemoved() && !this.level.isClientSide)
            {
                this.remove(Entity.RemovalReason.KILLED);

                if (!pSource.isExplosion())
                {
                    this.level.explode((Entity)null, this.getX(), this.getY(), this.getZ(), 6.0F, Explosion.BlockInteraction.DESTROY);
                }

                this.onDestroyedBy(pSource);
            }

            return true;
        }
    }

    public void kill()
    {
        this.onDestroyedBy(DamageSource.GENERIC);
        super.kill();
    }

    private void onDestroyedBy(DamageSource pSource)
    {
        if (this.level instanceof ServerLevel)
        {
            EndDragonFight enddragonfight = ((ServerLevel)this.level).dragonFight();

            if (enddragonfight != null)
            {
                enddragonfight.onCrystalDestroyed(this, pSource);
            }
        }
    }

    public void setBeamTarget(@Nullable BlockPos pBeamTarget)
    {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(pBeamTarget));
    }

    @Nullable
    public BlockPos getBeamTarget()
    {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse((BlockPos)null);
    }

    public void setShowBottom(boolean pShowBottom)
    {
        this.getEntityData().set(DATA_SHOW_BOTTOM, pShowBottom);
    }

    public boolean showsBottom()
    {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    public boolean shouldRenderAtSqrDistance(double pDistance)
    {
        return super.shouldRenderAtSqrDistance(pDistance) || this.getBeamTarget() != null;
    }

    public ItemStack getPickResult()
    {
        return new ItemStack(Items.END_CRYSTAL);
    }

    public Packet<?> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }
}
