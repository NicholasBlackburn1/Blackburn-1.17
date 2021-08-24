package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface TickList<T> {
   boolean hasScheduledTick(BlockPos p_47312_, T p_47313_);

   default void scheduleTick(BlockPos p_47314_, T p_47315_, int p_47316_) {
      this.scheduleTick(p_47314_, p_47315_, p_47316_, TickPriority.NORMAL);
   }

   void scheduleTick(BlockPos p_47317_, T p_47318_, int p_47319_, TickPriority p_47320_);

   boolean willTickThisTick(BlockPos p_47321_, T p_47322_);

   int size();
}