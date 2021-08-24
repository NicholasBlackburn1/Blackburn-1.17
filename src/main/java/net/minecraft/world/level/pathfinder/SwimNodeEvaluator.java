package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
   private final boolean allowBreaching;

   public SwimNodeEvaluator(boolean p_77457_) {
      this.allowBreaching = p_77457_;
   }

   public Node getStart() {
      return super.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
   }

   public Target getGoal(double p_77459_, double p_77460_, double p_77461_) {
      return new Target(super.getNode(Mth.floor(p_77459_ - (double)(this.mob.getBbWidth() / 2.0F)), Mth.floor(p_77460_ + 0.5D), Mth.floor(p_77461_ - (double)(this.mob.getBbWidth() / 2.0F))));
   }

   public int getNeighbors(Node[] p_77483_, Node p_77484_) {
      int i = 0;

      for(Direction direction : Direction.values()) {
         Node node = this.getWaterNode(p_77484_.x + direction.getStepX(), p_77484_.y + direction.getStepY(), p_77484_.z + direction.getStepZ());
         if (node != null && !node.closed) {
            p_77483_[i++] = node;
         }
      }

      return i;
   }

   public BlockPathTypes getBlockPathType(BlockGetter p_77472_, int p_77473_, int p_77474_, int p_77475_, Mob p_77476_, int p_77477_, int p_77478_, int p_77479_, boolean p_77480_, boolean p_77481_) {
      return this.getBlockPathType(p_77472_, p_77473_, p_77474_, p_77475_);
   }

   public BlockPathTypes getBlockPathType(BlockGetter p_77467_, int p_77468_, int p_77469_, int p_77470_) {
      BlockPos blockpos = new BlockPos(p_77468_, p_77469_, p_77470_);
      FluidState fluidstate = p_77467_.getFluidState(blockpos);
      BlockState blockstate = p_77467_.getBlockState(blockpos);
      if (fluidstate.isEmpty() && blockstate.isPathfindable(p_77467_, blockpos.below(), PathComputationType.WATER) && blockstate.isAir()) {
         return BlockPathTypes.BREACH;
      } else {
         return fluidstate.is(FluidTags.WATER) && blockstate.isPathfindable(p_77467_, blockpos, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
      }
   }

   @Nullable
   private Node getWaterNode(int p_77487_, int p_77488_, int p_77489_) {
      BlockPathTypes blockpathtypes = this.isFree(p_77487_, p_77488_, p_77489_);
      return (!this.allowBreaching || blockpathtypes != BlockPathTypes.BREACH) && blockpathtypes != BlockPathTypes.WATER ? null : this.getNode(p_77487_, p_77488_, p_77489_);
   }

   @Nullable
   protected Node getNode(int p_77463_, int p_77464_, int p_77465_) {
      Node node = null;
      BlockPathTypes blockpathtypes = this.getBlockPathType(this.mob.level, p_77463_, p_77464_, p_77465_);
      float f = this.mob.getPathfindingMalus(blockpathtypes);
      if (f >= 0.0F) {
         node = super.getNode(p_77463_, p_77464_, p_77465_);
         node.type = blockpathtypes;
         node.costMalus = Math.max(node.costMalus, f);
         if (this.level.getFluidState(new BlockPos(p_77463_, p_77464_, p_77465_)).isEmpty()) {
            node.costMalus += 8.0F;
         }
      }

      return blockpathtypes == BlockPathTypes.OPEN ? node : node;
   }

   private BlockPathTypes isFree(int p_77491_, int p_77492_, int p_77493_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = p_77491_; i < p_77491_ + this.entityWidth; ++i) {
         for(int j = p_77492_; j < p_77492_ + this.entityHeight; ++j) {
            for(int k = p_77493_; k < p_77493_ + this.entityDepth; ++k) {
               FluidState fluidstate = this.level.getFluidState(blockpos$mutableblockpos.set(i, j, k));
               BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(i, j, k));
               if (fluidstate.isEmpty() && blockstate.isPathfindable(this.level, blockpos$mutableblockpos.below(), PathComputationType.WATER) && blockstate.isAir()) {
                  return BlockPathTypes.BREACH;
               }

               if (!fluidstate.is(FluidTags.WATER)) {
                  return BlockPathTypes.BLOCKED;
               }
            }
         }
      }

      BlockState blockstate1 = this.level.getBlockState(blockpos$mutableblockpos);
      return blockstate1.isPathfindable(this.level, blockpos$mutableblockpos, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
   }
}