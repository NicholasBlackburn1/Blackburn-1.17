package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public class EmptyTickList<T> implements TickList<T>
{
    private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

    public static <T> EmptyTickList<T> empty()
    {
        return (EmptyTickList<T>)INSTANCE;
    }

    public boolean hasScheduledTick(BlockPos pPos, T pItem)
    {
        return false;
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime)
    {
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority p_45887_)
    {
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
