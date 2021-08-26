package net.minecraft.util.profiling;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public class InactiveProfiler implements ProfileCollector
{
    public static final InactiveProfiler INSTANCE = new InactiveProfiler();

    private InactiveProfiler()
    {
    }

    public void startTick()
    {
    }

    public void endTick()
    {
    }

    public void push(String pName)
    {
    }

    public void push(Supplier<String> pName)
    {
    }

    public void markForCharting(MetricCategory p_145951_)
    {
    }

    public void pop()
    {
    }

    public void popPush(String pName)
    {
    }

    public void popPush(Supplier<String> pName)
    {
    }

    public void incrementCounter(String p_18569_)
    {
    }

    public void incrementCounter(Supplier<String> p_18571_)
    {
    }

    public ProfileResults getResults()
    {
        return EmptyProfileResults.EMPTY;
    }

    @Nullable
    public ActiveProfiler.PathEntry getEntry(String p_145953_)
    {
        return null;
    }

    public Set<Pair<String, MetricCategory>> getChartedPaths()
    {
        return ImmutableSet.of();
    }
}
