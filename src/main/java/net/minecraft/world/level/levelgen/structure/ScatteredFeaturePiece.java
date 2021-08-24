package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
   protected final int width;
   protected final int height;
   protected final int depth;
   protected int heightPosition = -1;

   protected ScatteredFeaturePiece(StructurePieceType p_163188_, int p_163189_, int p_163190_, int p_163191_, int p_163192_, int p_163193_, int p_163194_, Direction p_163195_) {
      super(p_163188_, 0, StructurePiece.makeBoundingBox(p_163189_, p_163190_, p_163191_, p_163195_, p_163192_, p_163193_, p_163194_));
      this.width = p_163192_;
      this.height = p_163193_;
      this.depth = p_163194_;
      this.setOrientation(p_163195_);
   }

   protected ScatteredFeaturePiece(StructurePieceType p_72801_, CompoundTag p_72802_) {
      super(p_72801_, p_72802_);
      this.width = p_72802_.getInt("Width");
      this.height = p_72802_.getInt("Height");
      this.depth = p_72802_.getInt("Depth");
      this.heightPosition = p_72802_.getInt("HPos");
   }

   protected void addAdditionalSaveData(ServerLevel p_163197_, CompoundTag p_163198_) {
      p_163198_.putInt("Width", this.width);
      p_163198_.putInt("Height", this.height);
      p_163198_.putInt("Depth", this.depth);
      p_163198_.putInt("HPos", this.heightPosition);
   }

   protected boolean updateAverageGroundHeight(LevelAccessor p_72804_, BoundingBox p_72805_, int p_72806_) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int i = 0;
         int j = 0;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); ++k) {
            for(int l = this.boundingBox.minX(); l <= this.boundingBox.maxX(); ++l) {
               blockpos$mutableblockpos.set(l, 64, k);
               if (p_72805_.isInside(blockpos$mutableblockpos)) {
                  i += p_72804_.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutableblockpos).getY();
                  ++j;
               }
            }
         }

         if (j == 0) {
            return false;
         } else {
            this.heightPosition = i / j;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + p_72806_, 0);
            return true;
         }
      }
   }
}