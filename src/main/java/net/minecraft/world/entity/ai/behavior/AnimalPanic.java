package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic extends Behavior<PathfinderMob> {
   private static final int PANIC_MIN_DURATION = 100;
   private static final int PANIC_MAX_DURATION = 120;
   private static final int PANIC_DISTANCE_HORIZANTAL = 5;
   private static final int PANIC_DISTANCE_VERTICAL = 4;
   private final float speedMultiplier;

   public AnimalPanic(float p_147385_) {
      super(ImmutableMap.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
      this.speedMultiplier = p_147385_;
   }

   protected boolean canStillUse(ServerLevel p_147391_, PathfinderMob p_147392_, long p_147393_) {
      return true;
   }

   protected void start(ServerLevel p_147399_, PathfinderMob p_147400_, long p_147401_) {
      p_147400_.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
   }

   protected void tick(ServerLevel p_147403_, PathfinderMob p_147404_, long p_147405_) {
      if (p_147404_.getNavigation().isDone()) {
         Vec3 vec3 = LandRandomPos.getPos(p_147404_, 5, 4);
         if (vec3 != null) {
            p_147404_.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
         }
      }

   }
}