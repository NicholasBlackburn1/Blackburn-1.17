package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;

public class SingleBaseStoneSource implements BaseStoneSource {
   private final BlockState state;

   public SingleBaseStoneSource(BlockState p_158905_) {
      this.state = p_158905_;
   }

   public BlockState getBaseBlock(int p_158907_, int p_158908_, int p_158909_) {
      return this.state;
   }
}