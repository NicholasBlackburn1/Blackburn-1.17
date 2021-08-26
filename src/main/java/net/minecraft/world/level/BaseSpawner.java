package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseSpawner
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EVENT_SPAWN = 1;
    private static WeightedRandomList<SpawnData> EMPTY_POTENTIALS = WeightedRandomList.create();
    private int spawnDelay = 20;
    private WeightedRandomList<SpawnData> spawnPotentials = EMPTY_POTENTIALS;
    private SpawnData nextSpawnData = new SpawnData();
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;
    private final Random random = new Random();

    @Nullable
    private ResourceLocation getEntityId(@Nullable Level p_151333_, BlockPos p_151334_)
    {
        String s = this.nextSpawnData.getTag().getString("id");

        try
        {
            return StringUtil.isNullOrEmpty(s) ? null : new ResourceLocation(s);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            LOGGER.warn("Invalid entity id '{}' at spawner {}:[{},{},{}]", s, p_151333_ != null ? p_151333_.dimension().location() : "<null>", p_151334_.getX(), p_151334_.getY(), p_151334_.getZ());
            return null;
        }
    }

    public void setEntityId(EntityType<?> pType)
    {
        this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(pType).toString());
    }

    private boolean isNearPlayer(Level p_151344_, BlockPos p_151345_)
    {
        return p_151344_.hasNearbyAlivePlayer((double)p_151345_.getX() + 0.5D, (double)p_151345_.getY() + 0.5D, (double)p_151345_.getZ() + 0.5D, (double)this.requiredPlayerRange);
    }

    public void clientTick(Level p_151320_, BlockPos p_151321_)
    {
        if (!this.isNearPlayer(p_151320_, p_151321_))
        {
            this.oSpin = this.spin;
        }
        else
        {
            double d0 = (double)p_151321_.getX() + p_151320_.random.nextDouble();
            double d1 = (double)p_151321_.getY() + p_151320_.random.nextDouble();
            double d2 = (double)p_151321_.getZ() + p_151320_.random.nextDouble();
            p_151320_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            p_151320_.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);

            if (this.spawnDelay > 0)
            {
                --this.spawnDelay;
            }

            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0D;
        }
    }

    public void serverTick(ServerLevel p_151312_, BlockPos p_151313_)
    {
        if (this.isNearPlayer(p_151312_, p_151313_))
        {
            if (this.spawnDelay == -1)
            {
                this.delay(p_151312_, p_151313_);
            }

            if (this.spawnDelay > 0)
            {
                --this.spawnDelay;
            }
            else
            {
                boolean flag = false;

                for (int i = 0; i < this.spawnCount; ++i)
                {
                    CompoundTag compoundtag = this.nextSpawnData.getTag();
                    Optional < EntityType<? >> optional = EntityType.by(compoundtag);

                    if (!optional.isPresent())
                    {
                        this.delay(p_151312_, p_151313_);
                        return;
                    }

                    ListTag listtag = compoundtag.getList("Pos", 6);
                    int j = listtag.size();
                    double d0 = j >= 1 ? listtag.getDouble(0) : (double)p_151313_.getX() + (p_151312_.random.nextDouble() - p_151312_.random.nextDouble()) * (double)this.spawnRange + 0.5D;
                    double d1 = j >= 2 ? listtag.getDouble(1) : (double)(p_151313_.getY() + p_151312_.random.nextInt(3) - 1);
                    double d2 = j >= 3 ? listtag.getDouble(2) : (double)p_151313_.getZ() + (p_151312_.random.nextDouble() - p_151312_.random.nextDouble()) * (double)this.spawnRange + 0.5D;

                    if (p_151312_.noCollision(optional.get().getAABB(d0, d1, d2)) && SpawnPlacements.checkSpawnRules(optional.get(), p_151312_, MobSpawnType.SPAWNER, new BlockPos(d0, d1, d2), p_151312_.getRandom()))
                    {
                        Entity entity = EntityType.loadEntityRecursive(compoundtag, p_151312_, (p_151310_) ->
                        {
                            p_151310_.moveTo(d0, d1, d2, p_151310_.getYRot(), p_151310_.getXRot());
                            return p_151310_;
                        });

                        if (entity == null)
                        {
                            this.delay(p_151312_, p_151313_);
                            return;
                        }

                        int k = p_151312_.getEntitiesOfClass(entity.getClass(), (new AABB((double)p_151313_.getX(), (double)p_151313_.getY(), (double)p_151313_.getZ(), (double)(p_151313_.getX() + 1), (double)(p_151313_.getY() + 1), (double)(p_151313_.getZ() + 1))).inflate((double)this.spawnRange)).size();

                        if (k >= this.maxNearbyEntities)
                        {
                            this.delay(p_151312_, p_151313_);
                            return;
                        }

                        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), p_151312_.random.nextFloat() * 360.0F, 0.0F);

                        if (entity instanceof Mob)
                        {
                            Mob mob = (Mob)entity;

                            if (!mob.checkSpawnRules(p_151312_, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(p_151312_))
                            {
                                continue;
                            }

                            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8))
                            {
                                ((Mob)entity).finalizeSpawn(p_151312_, p_151312_.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
                            }
                        }

                        if (!p_151312_.tryAddFreshEntityWithPassengers(entity))
                        {
                            this.delay(p_151312_, p_151313_);
                            return;
                        }

                        p_151312_.levelEvent(2004, p_151313_, 0);

                        if (entity instanceof Mob)
                        {
                            ((Mob)entity).spawnAnim();
                        }

                        flag = true;
                    }
                }

                if (flag)
                {
                    this.delay(p_151312_, p_151313_);
                }
            }
        }
    }

    private void delay(Level p_151351_, BlockPos p_151352_)
    {
        if (this.maxSpawnDelay <= this.minSpawnDelay)
        {
            this.spawnDelay = this.minSpawnDelay;
        }
        else
        {
            this.spawnDelay = this.minSpawnDelay + this.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }

        this.spawnPotentials.getRandom(this.random).ifPresent((p_151349_) ->
        {
            this.setNextSpawnData(p_151351_, p_151352_, p_151349_);
        });
        this.broadcastEvent(p_151351_, p_151352_, 1);
    }

    public void load(@Nullable Level p_151329_, BlockPos p_151330_, CompoundTag p_151331_)
    {
        this.spawnDelay = p_151331_.getShort("Delay");
        List<SpawnData> list = Lists.newArrayList();

        if (p_151331_.contains("SpawnPotentials", 9))
        {
            ListTag listtag = p_151331_.getList("SpawnPotentials", 10);

            for (int i = 0; i < listtag.size(); ++i)
            {
                list.add(new SpawnData(listtag.getCompound(i)));
            }
        }

        this.spawnPotentials = WeightedRandomList.create(list);

        if (p_151331_.contains("SpawnData", 10))
        {
            this.setNextSpawnData(p_151329_, p_151330_, new SpawnData(1, p_151331_.getCompound("SpawnData")));
        }
        else if (!list.isEmpty())
        {
            this.spawnPotentials.getRandom(this.random).ifPresent((p_151338_) ->
            {
                this.setNextSpawnData(p_151329_, p_151330_, p_151338_);
            });
        }

        if (p_151331_.contains("MinSpawnDelay", 99))
        {
            this.minSpawnDelay = p_151331_.getShort("MinSpawnDelay");
            this.maxSpawnDelay = p_151331_.getShort("MaxSpawnDelay");
            this.spawnCount = p_151331_.getShort("SpawnCount");
        }

        if (p_151331_.contains("MaxNearbyEntities", 99))
        {
            this.maxNearbyEntities = p_151331_.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = p_151331_.getShort("RequiredPlayerRange");
        }

        if (p_151331_.contains("SpawnRange", 99))
        {
            this.spawnRange = p_151331_.getShort("SpawnRange");
        }

        this.displayEntity = null;
    }

    public CompoundTag save(@Nullable Level p_151340_, BlockPos p_151341_, CompoundTag p_151342_)
    {
        ResourceLocation resourcelocation = this.getEntityId(p_151340_, p_151341_);

        if (resourcelocation == null)
        {
            return p_151342_;
        }
        else
        {
            p_151342_.putShort("Delay", (short)this.spawnDelay);
            p_151342_.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
            p_151342_.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
            p_151342_.putShort("SpawnCount", (short)this.spawnCount);
            p_151342_.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
            p_151342_.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
            p_151342_.putShort("SpawnRange", (short)this.spawnRange);
            p_151342_.put("SpawnData", this.nextSpawnData.getTag().copy());
            ListTag listtag = new ListTag();

            if (this.spawnPotentials.isEmpty())
            {
                listtag.add(this.nextSpawnData.save());
            }
            else
            {
                for (SpawnData spawndata : this.spawnPotentials.unwrap())
                {
                    listtag.add(spawndata.save());
                }
            }

            p_151342_.put("SpawnPotentials", listtag);
            return p_151342_;
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level p_151315_)
    {
        if (this.displayEntity == null)
        {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), p_151315_, Function.identity());

            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && this.displayEntity instanceof Mob)
            {
            }
        }

        return this.displayEntity;
    }

    public boolean onEventTriggered(Level p_151317_, int p_151318_)
    {
        if (p_151318_ == 1)
        {
            if (p_151317_.isClientSide)
            {
                this.spawnDelay = this.minSpawnDelay;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void setNextSpawnData(@Nullable Level p_151325_, BlockPos p_151326_, SpawnData p_151327_)
    {
        this.nextSpawnData = p_151327_;
    }

    public abstract void broadcastEvent(Level p_151322_, BlockPos p_151323_, int p_151324_);

    public double getSpin()
    {
        return this.spin;
    }

    public double getoSpin()
    {
        return this.oSpin;
    }
}
