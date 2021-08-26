package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile
{
    public ThrownEnderpearl(EntityType <? extends ThrownEnderpearl > p_37491_, Level p_37492_)
    {
        super(p_37491_, p_37492_);
    }

    public ThrownEnderpearl(Level p_37499_, LivingEntity p_37500_)
    {
        super(EntityType.ENDER_PEARL, p_37500_, p_37499_);
    }

    protected Item getDefaultItem()
    {
        return Items.ENDER_PEARL;
    }

    protected void onHitEntity(EntityHitResult pResult)
    {
        super.onHitEntity(pResult);
        pResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
    }

    protected void onHit(HitResult pResult)
    {
        super.onHit(pResult);

        for (int i = 0; i < 32; ++i)
        {
            this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        if (!this.level.isClientSide && !this.isRemoved())
        {
            Entity entity = this.getOwner();

            if (entity instanceof ServerPlayer)
            {
                ServerPlayer serverplayer = (ServerPlayer)entity;

                if (serverplayer.connection.getConnection().isConnected() && serverplayer.level == this.level && !serverplayer.isSleeping())
                {
                    if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING))
                    {
                        Endermite endermite = EntityType.ENDERMITE.create(this.level);
                        endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                        this.level.addFreshEntity(endermite);
                    }

                    if (entity.isPassenger())
                    {
                        serverplayer.dismountTo(this.getX(), this.getY(), this.getZ());
                    }
                    else
                    {
                        entity.teleportTo(this.getX(), this.getY(), this.getZ());
                    }

                    entity.fallDistance = 0.0F;
                    entity.hurt(DamageSource.FALL, 5.0F);
                }
            }
            else if (entity != null)
            {
                entity.teleportTo(this.getX(), this.getY(), this.getZ());
                entity.fallDistance = 0.0F;
            }

            this.discard();
        }
    }

    public void tick()
    {
        Entity entity = this.getOwner();

        if (entity instanceof Player && !entity.isAlive())
        {
            this.discard();
        }
        else
        {
            super.tick();
        }
    }

    @Nullable
    public Entity changeDimension(ServerLevel pServer)
    {
        Entity entity = this.getOwner();

        if (entity != null && entity.level.dimension() != pServer.dimension())
        {
            this.setOwner((Entity)null);
        }

        return super.changeDimension(pServer);
    }
}
