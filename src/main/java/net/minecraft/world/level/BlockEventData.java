package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class BlockEventData {
   private final BlockPos pos;
   private final Block block;
   private final int paramA;
   private final int paramB;

   public BlockEventData(BlockPos p_45534_, Block p_45535_, int p_45536_, int p_45537_) {
      this.pos = p_45534_;
      this.block = p_45535_;
      this.paramA = p_45536_;
      this.paramB = p_45537_;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Block getBlock() {
      return this.block;
   }

   public int getParamA() {
      return this.paramA;
   }

   public int getParamB() {
      return this.paramB;
   }

   public boolean equals(Object p_45543_) {
      if (!(p_45543_ instanceof BlockEventData)) {
         return false;
      } else {
         BlockEventData blockeventdata = (BlockEventData)p_45543_;
         return this.pos.equals(blockeventdata.pos) && this.paramA == blockeventdata.paramA && this.paramB == blockeventdata.paramB && this.block == blockeventdata.block;
      }
   }

   public int hashCode() {
      int i = this.pos.hashCode();
      i = 31 * i + this.block.hashCode();
      i = 31 * i + this.paramA;
      return 31 * i + this.paramB;
   }

   public String toString() {
      return "TE(" + this.pos + ")," + this.paramA + "," + this.paramB + "," + this.block;
   }
}