package net.optifine;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;

public class RandomEntity implements IRandomEntity
{
    private Entity entity;

    public int getId()
    {
        UUID uuid = this.entity.getUUID();
        long i = uuid.getLeastSignificantBits();
        return (int)(i & 2147483647L);
    }

    public BlockPos getSpawnPosition()
    {
        return this.entity.getEntityData().spawnPosition;
    }

    public Biome getSpawnBiome()
    {
        return this.entity.getEntityData().spawnBiome;
    }

    public String getName()
    {
        return this.entity.hasCustomName() ? this.entity.getCustomName().getString() : null;
    }

    public int getHealth()
    {
        if (!(this.entity instanceof LivingEntity))
        {
            return 0;
        }
        else
        {
            LivingEntity livingentity = (LivingEntity)this.entity;
            return (int)livingentity.getHealth();
        }
    }

    public int getMaxHealth()
    {
        if (!(this.entity instanceof LivingEntity))
        {
            return 0;
        }
        else
        {
            LivingEntity livingentity = (LivingEntity)this.entity;
            return (int)livingentity.getMaxHealth();
        }
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public void setEntity(Entity entity)
    {
        this.entity = entity;
    }
}
