package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetLookAndInteract extends Behavior<LivingEntity> {
   private final EntityType<?> type;
   private final int interactionRangeSqr;
   private final Predicate<LivingEntity> targetFilter;
   private final Predicate<LivingEntity> selfFilter;

   public SetLookAndInteract(EntityType<?> p_23945_, int p_23946_, Predicate<LivingEntity> p_23947_, Predicate<LivingEntity> p_23948_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
      this.type = p_23945_;
      this.interactionRangeSqr = p_23946_ * p_23946_;
      this.targetFilter = p_23948_;
      this.selfFilter = p_23947_;
   }

   public SetLookAndInteract(EntityType<?> p_23942_, int p_23943_) {
      this(p_23942_, p_23943_, (p_23973_) -> {
         return true;
      }, (p_23971_) -> {
         return true;
      });
   }

   public boolean checkExtraStartConditions(ServerLevel p_23950_, LivingEntity p_23951_) {
      return this.selfFilter.test(p_23951_) && this.getVisibleEntities(p_23951_).stream().anyMatch(this::isMatchingTarget);
   }

   public void start(ServerLevel p_23953_, LivingEntity p_23954_, long p_23955_) {
      super.start(p_23953_, p_23954_, p_23955_);
      Brain<?> brain = p_23954_.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((p_23964_) -> {
         p_23964_.stream().filter((p_147899_) -> {
            return p_147899_.distanceToSqr(p_23954_) <= (double)this.interactionRangeSqr;
         }).filter(this::isMatchingTarget).findFirst().ifPresent((p_147902_) -> {
            brain.setMemory(MemoryModuleType.INTERACTION_TARGET, p_147902_);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(p_147902_, true));
         });
      });
   }

   private boolean isMatchingTarget(LivingEntity p_23957_) {
      return this.type.equals(p_23957_.getType()) && this.targetFilter.test(p_23957_);
   }

   private List<LivingEntity> getVisibleEntities(LivingEntity p_23969_) {
      return p_23969_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
   }
}