package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter extends BlockGetter
{
    float getShade(Direction p_45522_, boolean p_45523_);

    LevelLightEngine getLightEngine();

    int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver);

default int getBrightness(LightLayer pLightType, BlockPos pBlockPos)
    {
        return this.getLightEngine().getLayerListener(pLightType).getLightValue(pBlockPos);
    }

default int getRawBrightness(BlockPos pBlockPos, int pAmount)
    {
        return this.getLightEngine().getRawBrightness(pBlockPos, pAmount);
    }

default boolean canSeeSky(BlockPos pBlockPos)
    {
        return this.getBrightness(LightLayer.SKY, pBlockPos) >= this.getMaxLightLevel();
    }
}
