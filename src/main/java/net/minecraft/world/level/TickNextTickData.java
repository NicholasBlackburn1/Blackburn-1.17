package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;

public class TickNextTickData<T>
{
    private static long counter;
    private final T type;
    public final BlockPos pos;
    public final long triggerTick;
    public final TickPriority priority;
    private final long c;

    public TickNextTickData(BlockPos p_47330_, T p_47331_)
    {
        this(p_47330_, p_47331_, 0L, TickPriority.NORMAL);
    }

    public TickNextTickData(BlockPos p_47333_, T p_47334_, long p_47335_, TickPriority p_47336_)
    {
        this.c = (long)(counter++);
        this.pos = p_47333_.immutable();
        this.type = p_47334_;
        this.triggerTick = p_47335_;
        this.priority = p_47336_;
    }

    public boolean equals(Object p_47346_)
    {
        if (!(p_47346_ instanceof TickNextTickData))
        {
            return false;
        }
        else
        {
            TickNextTickData<?> ticknexttickdata = (TickNextTickData)p_47346_;
            return this.pos.equals(ticknexttickdata.pos) && this.type == ticknexttickdata.type;
        }
    }

    public int hashCode()
    {
        return this.pos.hashCode();
    }

    public static <T> Comparator<TickNextTickData<T>> createTimeComparator()
    {
        return Comparator.<TickNextTickData<T>>comparingLong((p_47344_) ->
        {
            return p_47344_.triggerTick;
        }).thenComparing((p_47342_) ->
        {
            return p_47342_.priority;
        }).thenComparingLong((p_47339_) ->
        {
            return p_47339_.c;
        });
    }

    public String toString()
    {
        return this.type + ": " + this.pos + ", " + this.triggerTick + ", " + this.priority + ", " + this.c;
    }

    public T getType()
    {
        return this.type;
    }
}
