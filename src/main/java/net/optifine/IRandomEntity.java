package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public interface IRandomEntity
{
    int getId();

    BlockPos getSpawnPosition();

    Biome getSpawnBiome();

    String getName();

    int getHealth();

    int getMaxHealth();
}
