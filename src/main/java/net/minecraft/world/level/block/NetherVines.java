package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class NetherVines
{
    private static final double BONEMEAL_GROW_PROBABILITY_DECREASE_RATE = 0.826D;
    public static final double GROW_PER_TICK_PROBABILITY = 0.1D;

    public static boolean isValidGrowthState(BlockState pState)
    {
        return pState.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(Random pRand)
    {
        double d0 = 1.0D;
        int i;

        for (i = 0; pRand.nextDouble() < d0; ++i)
        {
            d0 *= 0.826D;
        }

        return i;
    }
}
