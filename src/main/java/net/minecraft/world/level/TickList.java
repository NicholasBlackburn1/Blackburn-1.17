package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface TickList<T>
{
    boolean hasScheduledTick(BlockPos pPos, T pItem);

default void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime)
    {
        this.scheduleTick(pPos, pItem, pScheduledTime, TickPriority.NORMAL);
    }

    void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority p_47320_);

    boolean willTickThisTick(BlockPos pPos, T pObj);

    int size();
}
