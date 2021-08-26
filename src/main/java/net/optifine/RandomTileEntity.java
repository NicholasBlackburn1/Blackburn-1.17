package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.util.TileEntityUtils;

public class RandomTileEntity implements IRandomEntity
{
    private BlockEntity tileEntity;

    public int getId()
    {
        return Config.getRandom(this.tileEntity.getBlockPos(), 0);
    }

    public BlockPos getSpawnPosition()
    {
        return this.tileEntity.getBlockPos();
    }

    public String getName()
    {
        return TileEntityUtils.getTileEntityName(this.tileEntity);
    }

    public Biome getSpawnBiome()
    {
        return this.tileEntity.getLevel().getBiome(this.tileEntity.getBlockPos());
    }

    public int getHealth()
    {
        return -1;
    }

    public int getMaxHealth()
    {
        return -1;
    }

    public BlockEntity getTileEntity()
    {
        return this.tileEntity;
    }

    public void setTileEntity(BlockEntity tileEntity)
    {
        this.tileEntity = tileEntity;
    }
}
