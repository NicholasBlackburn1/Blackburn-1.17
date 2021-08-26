package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BirchTreeGrower extends AbstractTreeGrower
{
    @Nullable
    protected ConfiguredFeature < TreeConfiguration, ? > getConfiguredFeature(Random pRandom, boolean pLargeHive)
    {
        return pLargeHive ? Features.BIRCH_BEES_005 : Features.BIRCH;
    }
}
