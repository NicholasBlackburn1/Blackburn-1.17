package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData
{
    void setXSpawn(int pX);

    void setYSpawn(int pY);

    void setZSpawn(int pZ);

    void setSpawnAngle(float pAngle);

default void setSpawn(BlockPos pSpawnPoint, float pAngle)
    {
        this.setXSpawn(pSpawnPoint.getX());
        this.setYSpawn(pSpawnPoint.getY());
        this.setZSpawn(pSpawnPoint.getZ());
        this.setSpawnAngle(pAngle);
    }
}
