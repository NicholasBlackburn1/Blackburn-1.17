package net.minecraft.server.level;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;

public class WorldGenTickList<T> implements TickList<T> {
   private final Function<BlockPos, TickList<T>> index;

   public WorldGenTickList(Function<BlockPos, TickList<T>> p_9605_) {
      this.index = p_9605_;
   }

   public boolean hasScheduledTick(BlockPos p_9607_, T p_9608_) {
      return this.index.apply(p_9607_).hasScheduledTick(p_9607_, p_9608_);
   }

   public void scheduleTick(BlockPos p_9610_, T p_9611_, int p_9612_, TickPriority p_9613_) {
      this.index.apply(p_9610_).scheduleTick(p_9610_, p_9611_, p_9612_, p_9613_);
   }

   public boolean willTickThisTick(BlockPos p_9615_, T p_9616_) {
      return false;
   }

   public int size() {
      return 0;
   }
}