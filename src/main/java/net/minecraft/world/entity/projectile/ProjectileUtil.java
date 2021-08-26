package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ProjectileUtil
{
    public static HitResult getHitResult(Entity p_37295_, Predicate<Entity> p_37296_)
    {
        Vec3 vec3 = p_37295_.getDeltaMovement();
        Level level = p_37295_.level;
        Vec3 vec31 = p_37295_.position();
        Vec3 vec32 = vec31.add(vec3);
        HitResult hitresult = level.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_37295_));

        if (hitresult.getType() != HitResult.Type.MISS)
        {
            vec32 = hitresult.getLocation();
        }

        HitResult hitresult1 = getEntityHitResult(level, p_37295_, vec31, vec32, p_37295_.getBoundingBox().expandTowards(p_37295_.getDeltaMovement()).inflate(1.0D), p_37296_);

        if (hitresult1 != null)
        {
            hitresult = hitresult1;
        }

        return hitresult;
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity pShooter, Vec3 pStartVec, Vec3 pEndVec, AABB pBoundingBox, Predicate<Entity> pFilter, double pDistance)
    {
        Level level = pShooter.level;
        double d0 = pDistance;
        Entity entity = null;
        Vec3 vec3 = null;

        for (Entity entity1 : level.getEntities(pShooter, pBoundingBox, pFilter))
        {
            AABB aabb = entity1.getBoundingBox().inflate((double)entity1.getPickRadius());
            Optional<Vec3> optional = aabb.clip(pStartVec, pEndVec);

            if (aabb.contains(pStartVec))
            {
                if (d0 >= 0.0D)
                {
                    entity = entity1;
                    vec3 = optional.orElse(pStartVec);
                    d0 = 0.0D;
                }
            }
            else if (optional.isPresent())
            {
                Vec3 vec31 = optional.get();
                double d1 = pStartVec.distanceToSqr(vec31);

                if (d1 < d0 || d0 == 0.0D)
                {
                    if (entity1.getRootVehicle() == pShooter.getRootVehicle())
                    {
                        if (d0 == 0.0D)
                        {
                            entity = entity1;
                            vec3 = vec31;
                        }
                    }
                    else
                    {
                        entity = entity1;
                        vec3 = vec31;
                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity, vec3);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level pShooter, Entity pStartVec, Vec3 pEndVec, Vec3 pBoundingBox, AABB pFilter, Predicate<Entity> pDistance)
    {
        return getEntityHitResult(pShooter, pStartVec, pEndVec, pBoundingBox, pFilter, pDistance, 0.3F);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level pShooter, Entity pStartVec, Vec3 pEndVec, Vec3 pBoundingBox, AABB pFilter, Predicate<Entity> pDistance, float p_150182_)
    {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;

        for (Entity entity1 : pShooter.getEntities(pStartVec, pFilter, pDistance))
        {
            AABB aabb = entity1.getBoundingBox().inflate((double)p_150182_);
            Optional<Vec3> optional = aabb.clip(pEndVec, pBoundingBox);

            if (optional.isPresent())
            {
                double d1 = pEndVec.distanceToSqr(optional.get());

                if (d1 < d0)
                {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity);
    }

    public static void rotateTowardsMovement(Entity pProjectile, float pRotationSpeed)
    {
        Vec3 vec3 = pProjectile.getDeltaMovement();

        if (vec3.lengthSqr() != 0.0D)
        {
            double d0 = vec3.horizontalDistance();
            pProjectile.setYRot((float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) + 90.0F);
            pProjectile.setXRot((float)(Mth.atan2(d0, vec3.y) * (double)(180F / (float)Math.PI)) - 90.0F);

            while (pProjectile.getXRot() - pProjectile.xRotO < -180.0F)
            {
                pProjectile.xRotO -= 360.0F;
            }

            while (pProjectile.getXRot() - pProjectile.xRotO >= 180.0F)
            {
                pProjectile.xRotO += 360.0F;
            }

            while (pProjectile.getYRot() - pProjectile.yRotO < -180.0F)
            {
                pProjectile.yRotO -= 360.0F;
            }

            while (pProjectile.getYRot() - pProjectile.yRotO >= 180.0F)
            {
                pProjectile.yRotO += 360.0F;
            }

            pProjectile.setXRot(Mth.lerp(pRotationSpeed, pProjectile.xRotO, pProjectile.getXRot()));
            pProjectile.setYRot(Mth.lerp(pRotationSpeed, pProjectile.yRotO, pProjectile.getYRot()));
        }
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity pLiving, Item pItem)
    {
        return pLiving.getMainHandItem().is(pItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity pShooter, ItemStack pArrowStack, float pDistanceFactor)
    {
        ArrowItem arrowitem = (ArrowItem)(pArrowStack.getItem() instanceof ArrowItem ? pArrowStack.getItem() : Items.ARROW);
        AbstractArrow abstractarrow = arrowitem.createArrow(pShooter.level, pArrowStack, pShooter);
        abstractarrow.setEnchantmentEffectsFromEntity(pShooter, pDistanceFactor);

        if (pArrowStack.is(Items.TIPPED_ARROW) && abstractarrow instanceof Arrow)
        {
            ((Arrow)abstractarrow).setEffectsFromItem(pArrowStack);
        }

        return abstractarrow;
    }
}
