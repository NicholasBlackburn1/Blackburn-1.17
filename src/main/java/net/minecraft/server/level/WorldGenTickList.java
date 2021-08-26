package net.minecraft.server.level;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;

public class WorldGenTickList<T> implements TickList<T>
{
    private final Function<BlockPos, TickList<T>> index;

    public WorldGenTickList(Function<BlockPos, TickList<T>> p_9605_)
    {
        this.index = p_9605_;
    }

    public boolean hasScheduledTick(BlockPos pPos, T pItem)
    {
        return this.index.apply(pPos).hasScheduledTick(pPos, pItem);
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority)
    {
        this.index.apply(pPos).scheduleTick(pPos, pItem, pScheduledTime, pPriority);
    }

    public boolean willTickThisTick(BlockPos pPos, T pObj)
    {
        return false;
    }

    public int size()
    {
        return 0;
    }
}
