package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldGenerationContext
{
    private final int f_182504_;
    private final int f_182505_;

    public WorldGenerationContext(ChunkGenerator p_182507_, LevelHeightAccessor p_182508_)
    {
        this.f_182504_ = Math.max(p_182508_.getMinBuildHeight(), p_182507_.getMinY());
        this.f_182505_ = Math.min(p_182508_.getHeight(), p_182507_.getGenDepth());
    }

    public int getMinGenY()
    {
        return this.f_182504_;
    }

    public int getGenDepth()
    {
        return this.f_182505_;
    }
}
