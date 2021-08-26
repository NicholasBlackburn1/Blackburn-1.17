package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile extends Entity
{
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;

    Projectile(EntityType <? extends Projectile > p_37248_, Level p_37249_)
    {
        super(p_37248_, p_37249_);
    }

    public void setOwner(@Nullable Entity pEntity)
    {
        if (pEntity != null)
        {
            this.ownerUUID = pEntity.getUUID();
            this.cachedOwner = pEntity;
        }
    }

    @Nullable
    public Entity getOwner()
    {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved())
        {
            return this.cachedOwner;
        }
        else if (this.ownerUUID != null && this.level instanceof ServerLevel)
        {
            this.cachedOwner = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        else
        {
            return null;
        }
    }

    public Entity getEffectSource()
    {
        return MoreObjects.firstNonNull(this.getOwner(), this);
    }

    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        if (this.ownerUUID != null)
        {
            pCompound.putUUID("Owner", this.ownerUUID);
        }

        if (this.leftOwner)
        {
            pCompound.putBoolean("LeftOwner", true);
        }

        pCompound.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity p_150172_)
    {
        return p_150172_.getUUID().equals(this.ownerUUID);
    }

    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        if (pCompound.hasUUID("Owner"))
        {
            this.ownerUUID = pCompound.getUUID("Owner");
        }

        this.leftOwner = pCompound.getBoolean("LeftOwner");
        this.hasBeenShot = pCompound.getBoolean("HasBeenShot");
    }

    public void tick()
    {
        if (!this.hasBeenShot)
        {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
            this.hasBeenShot = true;
        }

        if (!this.leftOwner)
        {
            this.leftOwner = this.checkLeftOwner();
        }

        super.tick();
    }

    private boolean checkLeftOwner()
    {
        Entity entity = this.getOwner();

        if (entity != null)
        {
            for (Entity entity1 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (p_37272_) ->
        {
            return !p_37272_.isSpectator() && p_37272_.isPickable();
            }))
            {
                if (entity1.getRootVehicle() == entity.getRootVehicle())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public void shoot(double pX, double p_37267_, double pY, float p_37269_, float pZ)
    {
        Vec3 vec3 = (new Vec3(pX, p_37267_, pY)).normalize().add(this.random.nextGaussian() * (double)0.0075F * (double)pZ, this.random.nextGaussian() * (double)0.0075F * (double)pZ, this.random.nextGaussian() * (double)0.0075F * (double)pZ).scale((double)p_37269_);
        this.setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity pProjectile, float pX, float pY, float pZ, float pVelocity, float pInaccuracy)
    {
        float f = -Mth.sin(pY * ((float)Math.PI / 180F)) * Mth.cos(pX * ((float)Math.PI / 180F));
        float f1 = -Mth.sin((pX + pZ) * ((float)Math.PI / 180F));
        float f2 = Mth.cos(pY * ((float)Math.PI / 180F)) * Mth.cos(pX * ((float)Math.PI / 180F));
        this.shoot((double)f, (double)f1, (double)f2, pVelocity, pInaccuracy);
        Vec3 vec3 = pProjectile.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, pProjectile.isOnGround() ? 0.0D : vec3.y, vec3.z));
    }

    protected void onHit(HitResult pResult)
    {
        HitResult.Type hitresult$type = pResult.getType();

        if (hitresult$type == HitResult.Type.ENTITY)
        {
            this.onHitEntity((EntityHitResult)pResult);
        }
        else if (hitresult$type == HitResult.Type.BLOCK)
        {
            this.onHitBlock((BlockHitResult)pResult);
        }

        if (hitresult$type != HitResult.Type.MISS)
        {
            this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }
    }

    protected void onHitEntity(EntityHitResult pResult)
    {
    }

    protected void onHitBlock(BlockHitResult p_37258_)
    {
        BlockState blockstate = this.level.getBlockState(p_37258_.getBlockPos());
        blockstate.onProjectileHit(this.level, blockstate, p_37258_, this);
    }

    public void lerpMotion(double pX, double p_37280_, double pY)
    {
        this.setDeltaMovement(pX, p_37280_, pY);

        if (this.xRotO == 0.0F && this.yRotO == 0.0F)
        {
            double d0 = Math.sqrt(pX * pX + pY * pY);
            this.setXRot((float)(Mth.atan2(p_37280_, d0) * (double)(180F / (float)Math.PI)));
            this.setYRot((float)(Mth.atan2(pX, pY) * (double)(180F / (float)Math.PI)));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    protected boolean canHitEntity(Entity p_37250_)
    {
        if (!p_37250_.isSpectator() && p_37250_.isAlive() && p_37250_.isPickable())
        {
            Entity entity = this.getOwner();
            return entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(p_37250_);
        }
        else
        {
            return false;
        }
    }

    protected void updateRotation()
    {
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = vec3.horizontalDistance();
        this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI))));
        this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI))));
    }

    protected static float lerpRotation(float p_37274_, float p_37275_)
    {
        while (p_37275_ - p_37274_ < -180.0F)
        {
            p_37274_ -= 360.0F;
        }

        while (p_37275_ - p_37274_ >= 180.0F)
        {
            p_37274_ += 360.0F;
        }

        return Mth.lerp(0.2F, p_37274_, p_37275_);
    }

    public Packet<?> getAddEntityPacket()
    {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_150170_)
    {
        super.recreateFromPacket(p_150170_);
        Entity entity = this.level.getEntity(p_150170_.getData());

        if (entity != null)
        {
            this.setOwner(entity);
        }
    }

    public boolean mayInteract(Level p_150167_, BlockPos p_150168_)
    {
        Entity entity = this.getOwner();

        if (entity instanceof Player)
        {
            return entity.mayInteract(p_150167_, p_150168_);
        }
        else
        {
            return entity == null || p_150167_.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
    }
}
