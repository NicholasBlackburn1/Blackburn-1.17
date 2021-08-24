package net.minecraft.world.level.levelgen;

public interface RandomSource {
   void setSeed(long p_158879_);

   int nextInt();

   int nextInt(int p_158878_);

   long nextLong();

   boolean nextBoolean();

   float nextFloat();

   double nextDouble();

   double nextGaussian();

   default void consumeCount(int p_158877_) {
      for(int i = 0; i < p_158877_; ++i) {
         this.nextInt();
      }

   }
}