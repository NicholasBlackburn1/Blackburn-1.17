package net.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeighedRandom
{
    static final Logger LOGGER = LogManager.getLogger();

    public static int getTotalWeight(List <? extends WeighedRandom.WeighedRandomItem > pCollection)
    {
        long i = 0L;

        for (WeighedRandom.WeighedRandomItem weighedrandom$weighedrandomitem : pCollection)
        {
            i += (long)weighedrandom$weighedrandomitem.weight;
        }

        if (i > 2147483647L)
        {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        else
        {
            return (int)i;
        }
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random p_145038_, List<T> p_145039_, int p_145040_)
    {
        if (p_145040_ < 0)
        {
            throw(IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        else if (p_145040_ == 0)
        {
            return Optional.empty();
        }
        else
        {
            int i = p_145038_.nextInt(p_145040_);
            return getWeightedItem(p_145039_, i);
        }
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getWeightedItem(List<T> p_145032_, int p_145033_)
    {
        for (T t : p_145032_)
        {
            p_145033_ -= t.weight;

            if (p_145033_ < 0)
            {
                return Optional.of(t);
            }
        }

        return Optional.empty();
    }

    public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random p_145035_, List<T> p_145036_)
    {
        return getRandomItem(p_145035_, p_145036_, getTotalWeight(p_145036_));
    }

    public static class WeighedRandomItem
    {
        protected final int weight;

        public WeighedRandomItem(int p_14484_)
        {
            if (p_14484_ < 0)
            {
                throw(IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
            }
            else
            {
                if (p_14484_ == 0 && SharedConstants.IS_RUNNING_IN_IDE)
                {
                    WeighedRandom.LOGGER.warn("Found 0 weight, make sure this is intentional!");
                }

                this.weight = p_14484_;
            }
        }
    }
}
