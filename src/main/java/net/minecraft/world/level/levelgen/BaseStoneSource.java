package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BaseStoneSource {
   default BlockState getBaseBlock(BlockPos p_158055_) {
      return this.getBaseBlock(p_158055_.getX(), p_158055_.getY(), p_158055_.getZ());
   }

   BlockState getBaseBlock(int p_158056_, int p_158057_, int p_158058_);
}