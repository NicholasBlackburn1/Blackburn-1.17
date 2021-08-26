package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener
{
    void checkBlock(BlockPos p_164454_);

    void onBlockEmissionIncrease(BlockPos p_164455_, int p_164456_);

    boolean hasLightWork();

    int runUpdates(int p_164449_, boolean p_164450_, boolean p_164451_);

default void updateSectionStatus(BlockPos pPos, boolean pIsEmpty)
    {
        this.updateSectionStatus(SectionPos.of(pPos), pIsEmpty);
    }

    void updateSectionStatus(SectionPos pPos, boolean pIsEmpty);

    void enableLightSources(ChunkPos p_164452_, boolean p_164453_);
}
