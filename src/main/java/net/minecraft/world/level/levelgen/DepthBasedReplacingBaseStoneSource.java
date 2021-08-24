package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
   private static final int ALWAYS_REPLACE_BELOW_Y = -8;
   private static final int NEVER_REPLACE_ABOVE_Y = 0;
   private final WorldgenRandom random;
   private final long seed;
   private final BlockState normalBlock;
   private final BlockState replacementBlock;
   private final NoiseGeneratorSettings settings;

   public DepthBasedReplacingBaseStoneSource(long p_158261_, BlockState p_158262_, BlockState p_158263_, NoiseGeneratorSettings p_158264_) {
      this.random = new WorldgenRandom(p_158261_);
      this.seed = p_158261_;
      this.normalBlock = p_158262_;
      this.replacementBlock = p_158263_;
      this.settings = p_158264_;
   }

   public BlockState getBaseBlock(int p_158266_, int p_158267_, int p_158268_) {
      if (!this.settings.isDeepslateEnabled()) {
         return this.normalBlock;
      } else if (p_158267_ < -8) {
         return this.replacementBlock;
      } else if (p_158267_ > 0) {
         return this.normalBlock;
      } else {
         double d0 = Mth.map((double)p_158267_, -8.0D, 0.0D, 1.0D, 0.0D);
         this.random.setBaseStoneSeed(this.seed, p_158266_, p_158267_, p_158268_);
         return (double)this.random.nextFloat() < d0 ? this.replacementBlock : this.normalBlock;
      }
   }
}