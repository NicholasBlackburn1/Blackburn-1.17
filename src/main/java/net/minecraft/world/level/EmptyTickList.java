package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public class EmptyTickList<T> implements TickList<T> {
   private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

   public static <T> EmptyTickList<T> empty() {
      return (EmptyTickList<T>)INSTANCE;
   }

   public boolean hasScheduledTick(BlockPos p_45877_, T p_45878_) {
      return false;
   }

   public void scheduleTick(BlockPos p_45880_, T p_45881_, int p_45882_) {
   }

   public void scheduleTick(BlockPos p_45884_, T p_45885_, int p_45886_, TickPriority p_45887_) {
   }

   public boolean willTickThisTick(BlockPos p_45890_, T p_45891_) {
      return false;
   }

   public int size() {
      return 0;
   }
}
