package net.minecraft.world.entity.ai.navigation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
   private boolean allowBreaching;

   public WaterBoundPathNavigation(Mob p_26594_, Level p_26595_) {
      super(p_26594_, p_26595_);
   }

   protected PathFinder createPathFinder(int p_26598_) {
      this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
      this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
      return new PathFinder(this.nodeEvaluator, p_26598_);
   }

   protected boolean canUpdatePath() {
      return this.allowBreaching || this.isInLiquid();
   }

   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec3 = this.path.getNextEntityPos(this.mob);
            if (this.mob.getBlockX() == Mth.floor(vec3.x) && this.mob.getBlockY() == Mth.floor(vec3.y) && this.mob.getBlockZ() == Mth.floor(vec3.z)) {
               this.path.advance();
            }
         }

         DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vec3 vec31 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition(vec31.x, vec31.y, vec31.z, this.speedModifier);
         }
      }
   }

   protected void followThePath() {
      if (this.path != null) {
         Vec3 vec3 = this.getTempMobPos();
         float f = this.mob.getBbWidth();
         float f1 = f > 0.75F ? f / 2.0F : 0.75F - f / 2.0F;
         Vec3 vec31 = this.mob.getDeltaMovement();
         if (Math.abs(vec31.x) > 0.2D || Math.abs(vec31.z) > 0.2D) {
            f1 = (float)((double)f1 * vec31.length() * 6.0D);
         }

         int i = 6;
         Vec3 vec32 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
         if (Math.abs(this.mob.getX() - vec32.x) < (double)f1 && Math.abs(this.mob.getZ() - vec32.z) < (double)f1 && Math.abs(this.mob.getY() - vec32.y) < (double)(f1 * 2.0F)) {
            this.path.advance();
         }

         for(int j = Math.min(this.path.getNextNodeIndex() + 6, this.path.getNodeCount() - 1); j > this.path.getNextNodeIndex(); --j) {
            vec32 = this.path.getEntityPosAtNode(this.mob, j);
            if (!(vec32.distanceToSqr(vec3) > 36.0D) && this.canMoveDirectly(vec3, vec32, 0, 0, 0)) {
               this.path.setNextNodeIndex(j);
               break;
            }
         }

         this.doStuckDetection(vec3);
      }
   }

   protected void doStuckDetection(Vec3 p_26600_) {
      if (this.tick - this.lastStuckCheck > 100) {
         if (p_26600_.distanceToSqr(this.lastStuckCheckPos) < 2.25D) {
            this.stop();
         }

         this.lastStuckCheck = this.tick;
         this.lastStuckCheckPos = p_26600_;
      }

      if (this.path != null && !this.path.isDone()) {
         Vec3i vec3i = this.path.getNextNodePos();
         if (vec3i.equals(this.timeoutCachedNode)) {
            this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
         } else {
            this.timeoutCachedNode = vec3i;
            double d0 = p_26600_.distanceTo(Vec3.atCenterOf(this.timeoutCachedNode));
            this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double)this.mob.getSpeed() * 100.0D : 0.0D;
         }

         if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 2.0D) {
            this.timeoutCachedNode = Vec3i.ZERO;
            this.timeoutTimer = 0L;
            this.timeoutLimit = 0.0D;
            this.stop();
         }

         this.lastTimeoutCheck = Util.getMillis();
      }

   }

   protected boolean canMoveDirectly(Vec3 p_26602_, Vec3 p_26603_, int p_26604_, int p_26605_, int p_26606_) {
      Vec3 vec3 = new Vec3(p_26603_.x, p_26603_.y + (double)this.mob.getBbHeight() * 0.5D, p_26603_.z);
      return this.level.clip(new ClipContext(p_26602_, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
   }

   public boolean isStableDestination(BlockPos p_26608_) {
      return !this.level.getBlockState(p_26608_).isSolidRender(this.level, p_26608_);
   }

   public void setCanFloat(boolean p_26612_) {
   }
}