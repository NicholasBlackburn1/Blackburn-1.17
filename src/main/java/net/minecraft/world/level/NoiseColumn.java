package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class NoiseColumn {
   private final int minY;
   private final BlockState[] column;

   public NoiseColumn(int p_151623_, BlockState[] p_151624_) {
      this.minY = p_151623_;
      this.column = p_151624_;
   }

   public BlockState getBlockState(BlockPos p_47157_) {
      int i = p_47157_.getY() - this.minY;
      return i >= 0 && i < this.column.length ? this.column[i] : Blocks.AIR.defaultBlockState();
   }
}