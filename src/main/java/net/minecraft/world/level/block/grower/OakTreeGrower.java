package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower
{
    @Nullable
    protected ConfiguredFeature < TreeConfiguration, ? > getConfiguredFeature(Random pRandom, boolean pLargeHive)
    {
        if (pRandom.nextInt(10) == 0)
        {
            return pLargeHive ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK;
        }
        else
        {
            return pLargeHive ? Features.OAK_BEES_005 : Features.OAK;
        }
    }
}
