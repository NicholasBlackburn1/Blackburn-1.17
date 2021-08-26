package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter
{
    List<Entity> getEntities(@Nullable Entity pEntity, AABB pBb, Predicate <? super Entity > p_45938_);

    <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntity, AABB pBb, Predicate <? super T > p_151466_);

default <T extends Entity> List<T> getEntitiesOfClass(Class<T> pClazz, AABB pAabb, Predicate<? super T> pFilter)
    {
        return this.getEntities(EntityTypeTest.forClass(pClazz), pAabb, pFilter);
    }

    List <? extends Player > players();

default List<Entity> getEntities(@Nullable Entity pEntity, AABB pBb)
    {
        return this.getEntities(pEntity, pBb, EntitySelector.NO_SPECTATORS);
    }

default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape)
    {
        if (pShape.isEmpty())
        {
            return true;
        }
        else
        {
            for (Entity entity : this.getEntities(pEntity, pShape.bounds()))
            {
                if (!entity.isRemoved() && entity.blocksBuilding && (pEntity == null || !entity.isPassengerOfSameVehicle(pEntity)) && Shapes.joinIsNotEmpty(pShape, Shapes.create(entity.getBoundingBox()), BooleanOp.AND))
                {
                    return false;
                }
            }

            return true;
        }
    }

default <T extends Entity> List<T> getEntitiesOfClass(Class<T> pClazz, AABB pAabb)
    {
        return this.getEntitiesOfClass(pClazz, pAabb, EntitySelector.NO_SPECTATORS);
    }

default Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_46005_, AABB p_46006_, Predicate<Entity> p_46007_)
    {
        if (p_46006_.getSize() < 1.0E-7D)
        {
            return Stream.empty();
        }
        else
        {
            AABB aabb = p_46006_.inflate(1.0E-7D);
            return this.getEntities(p_46005_, aabb, p_46007_.and((p_45962_) ->
            {
                if (p_45962_.getBoundingBox().intersects(aabb))
                {
                    if (p_46005_ == null)
                    {
                        if (p_45962_.canBeCollidedWith())
                        {
                            return true;
                        }
                    }
                    else if (p_46005_.canCollideWith(p_45962_))
                    {
                        return true;
                    }
                }

                return false;
            })).stream().map(Entity::getBoundingBox).map(Shapes::create);
        }
    }

    @Nullable

default Player getNearestPlayer(double pX, double p_45920_, double pY, double p_45922_, @Nullable Predicate<Entity> pZ)
    {
        double d0 = -1.0D;
        Player player = null;

        for (Player player1 : this.players())
        {
            if (pZ == null || pZ.test(player1))
            {
                double d1 = player1.distanceToSqr(pX, p_45920_, pY);

                if ((p_45922_ < 0.0D || d1 < p_45922_ * p_45922_) && (d0 == -1.0D || d1 < d0))
                {
                    d0 = d1;
                    player = player1;
                }
            }
        }

        return player;
    }

    @Nullable

default Player getNearestPlayer(Entity pX, double p_45932_)
    {
        return this.getNearestPlayer(pX.getX(), pX.getY(), pX.getZ(), p_45932_, false);
    }

    @Nullable

default Player getNearestPlayer(double pX, double p_45926_, double pY, double p_45928_, boolean pZ)
    {
        Predicate<Entity> predicate = pZ ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
        return this.getNearestPlayer(pX, p_45926_, pY, p_45928_, predicate);
    }

default boolean hasNearbyAlivePlayer(double pX, double p_45916_, double pY, double p_45918_)
    {
        for (Player player : this.players())
        {
            if (EntitySelector.NO_SPECTATORS.test(player) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player))
            {
                double d0 = player.distanceToSqr(pX, p_45916_, pY);

                if (p_45918_ < 0.0D || d0 < p_45918_ * p_45918_)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable

default Player getNearestPlayer(TargetingConditions pX, LivingEntity p_45948_)
    {
        return this.getNearestEntity(this.players(), pX, p_45948_, p_45948_.getX(), p_45948_.getY(), p_45948_.getZ());
    }

    @Nullable

default Player getNearestPlayer(TargetingConditions pX, LivingEntity p_45951_, double pY, double p_45953_, double pZ)
    {
        return this.getNearestEntity(this.players(), pX, p_45951_, pY, p_45953_, pZ);
    }

    @Nullable

default Player getNearestPlayer(TargetingConditions pX, double p_45943_, double pY, double p_45945_)
    {
        return this.getNearestEntity(this.players(), pX, (LivingEntity)null, p_45943_, pY, p_45945_);
    }

    @Nullable

default <T extends LivingEntity> T getNearestEntity(Class<? extends T> pEntityClazz, TargetingConditions p_45965_, @Nullable LivingEntity pTarget, double pX, double p_45968_, double pY, AABB p_45970_)
    {
        return this.getNearestEntity(this.getEntitiesOfClass(pEntityClazz, p_45970_, (p_151468_) ->
        {
            return true;
        }), p_45965_, pTarget, pX, p_45968_, pY);
    }

    @Nullable

default <T extends LivingEntity> T getNearestEntity(List<? extends T> pEntityClazz, TargetingConditions p_45984_, @Nullable LivingEntity pTarget, double pX, double p_45987_, double pY)
    {
        double d0 = -1.0D;
        T t = null;

        for (T t1 : pEntityClazz)
        {
            if (p_45984_.test(pTarget, t1))
            {
                double d1 = t1.distanceToSqr(pX, p_45987_, pY);

                if (d0 == -1.0D || d1 < d0)
                {
                    d0 = d1;
                    t = t1;
                }
            }
        }

        return t;
    }

default List<Player> getNearbyPlayers(TargetingConditions pPredicate, LivingEntity pTarget, AABB pBox)
    {
        List<Player> list = Lists.newArrayList();

        for (Player player : this.players())
        {
            if (pBox.contains(player.getX(), player.getY(), player.getZ()) && pPredicate.test(pTarget, player))
            {
                list.add(player);
            }
        }

        return list;
    }

default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> pEntityClazz, TargetingConditions pEntityPredicate, LivingEntity pEntity, AABB p_45975_)
    {
        List<T> list = this.getEntitiesOfClass(pEntityClazz, p_45975_, (p_151463_) ->
        {
            return true;
        });
        List<T> list1 = Lists.newArrayList();

        for (T t : list)
        {
            if (pEntityPredicate.test(pEntity, t))
            {
                list1.add(t);
            }
        }

        return list1;
    }

    @Nullable

default Player getPlayerByUUID(UUID pUniqueId)
    {
        for (int i = 0; i < this.players().size(); ++i)
        {
            Player player = this.players().get(i);

            if (pUniqueId.equals(player.getUUID()))
            {
                return player;
            }
        }

        return null;
    }
}
