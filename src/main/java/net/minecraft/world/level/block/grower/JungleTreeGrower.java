package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class JungleTreeGrower extends AbstractMegaTreeGrower
{
    @Nullable
    protected ConfiguredFeature < TreeConfiguration, ? > getConfiguredFeature(Random pRandom, boolean pLargeHive)
    {
        return Features.JUNGLE_TREE_NO_VINE;
    }

    @Nullable
    protected ConfiguredFeature < TreeConfiguration, ? > getConfiguredMegaFeature(Random pRand)
    {
        return Features.MEGA_JUNGLE_TREE;
    }
}
