package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PanicGoal extends Goal {
   protected final PathfinderMob mob;
   protected final double speedModifier;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected boolean isRunning;

   public PanicGoal(PathfinderMob p_25691_, double p_25692_) {
      this.mob = p_25691_;
      this.speedModifier = p_25692_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   public boolean canUse() {
      if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
         return false;
      } else {
         if (this.mob.isOnFire()) {
            BlockPos blockpos = this.lookForWater(this.mob.level, this.mob, 5, 4);
            if (blockpos != null) {
               this.posX = (double)blockpos.getX();
               this.posY = (double)blockpos.getY();
               this.posZ = (double)blockpos.getZ();
               return true;
            }
         }

         return this.findRandomPosition();
      }
   }

   protected boolean findRandomPosition() {
      Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 5, 4);
      if (vec3 == null) {
         return false;
      } else {
         this.posX = vec3.x;
         this.posY = vec3.y;
         this.posZ = vec3.z;
         return true;
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void start() {
      this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
      this.isRunning = true;
   }

   public void stop() {
      this.isRunning = false;
   }

   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone();
   }

   @Nullable
   protected BlockPos lookForWater(BlockGetter p_25695_, Entity p_25696_, int p_25697_, int p_25698_) {
      BlockPos blockpos = p_25696_.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      float f = (float)(p_25697_ * p_25697_ * p_25698_ * 2);
      BlockPos blockpos1 = null;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = i - p_25697_; l <= i + p_25697_; ++l) {
         for(int i1 = j - p_25698_; i1 <= j + p_25698_; ++i1) {
            for(int j1 = k - p_25697_; j1 <= k + p_25697_; ++j1) {
               blockpos$mutableblockpos.set(l, i1, j1);
               if (p_25695_.getFluidState(blockpos$mutableblockpos).is(FluidTags.WATER)) {
                  float f1 = (float)((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));
                  if (f1 < f) {
                     f = f1;
                     blockpos1 = new BlockPos(blockpos$mutableblockpos);
                  }
               }
            }
         }
      }

      return blockpos1;
   }
}