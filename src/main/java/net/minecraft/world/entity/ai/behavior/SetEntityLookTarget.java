package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetEntityLookTarget extends Behavior<LivingEntity> {
   private final Predicate<LivingEntity> predicate;
   private final float maxDistSqr;

   public SetEntityLookTarget(Tag<EntityType<?>> p_147885_, float p_147886_) {
      this((p_147889_) -> {
         return p_147889_.getType().is(p_147885_);
      }, p_147886_);
   }

   public SetEntityLookTarget(MobCategory p_23897_, float p_23898_) {
      this((p_23923_) -> {
         return p_23897_.equals(p_23923_.getType().getCategory());
      }, p_23898_);
   }

   public SetEntityLookTarget(EntityType<?> p_23894_, float p_23895_) {
      this((p_23911_) -> {
         return p_23894_.equals(p_23911_.getType());
      }, p_23895_);
   }

   public SetEntityLookTarget(float p_23892_) {
      this((p_23913_) -> {
         return true;
      }, p_23892_);
   }

   public SetEntityLookTarget(Predicate<LivingEntity> p_23900_, float p_23901_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
      this.predicate = p_23900_;
      this.maxDistSqr = p_23901_ * p_23901_;
   }

   protected boolean checkExtraStartConditions(ServerLevel p_23903_, LivingEntity p_23904_) {
      return p_23904_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().stream().anyMatch(this.predicate);
   }

   protected void start(ServerLevel p_23906_, LivingEntity p_23907_, long p_23908_) {
      Brain<?> brain = p_23907_.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((p_23920_) -> {
         p_23920_.stream().filter(this.predicate).filter((p_147892_) -> {
            return p_147892_.distanceToSqr(p_23907_) <= (double)this.maxDistSqr;
         }).findFirst().ifPresent((p_147895_) -> {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(p_147895_, true));
         });
      });
   }
}