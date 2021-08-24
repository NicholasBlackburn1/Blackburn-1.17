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
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity> extends Behavior<E> {
   private final int maxDist;
   private final float speedModifier;
   private final EntityType<? extends T> type;
   private final int interactionRangeSqr;
   private final Predicate<T> targetFilter;
   private final Predicate<E> selfFilter;
   private final MemoryModuleType<T> memory;

   public InteractWith(EntityType<? extends T> p_23246_, int p_23247_, Predicate<E> p_23248_, Predicate<T> p_23249_, MemoryModuleType<T> p_23250_, float p_23251_, int p_23252_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
      this.type = p_23246_;
      this.speedModifier = p_23251_;
      this.interactionRangeSqr = p_23247_ * p_23247_;
      this.maxDist = p_23252_;
      this.targetFilter = p_23249_;
      this.selfFilter = p_23248_;
      this.memory = p_23250_;
   }

   public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(EntityType<? extends T> p_23261_, int p_23262_, MemoryModuleType<T> p_23263_, float p_23264_, int p_23265_) {
      return new InteractWith<>(p_23261_, p_23262_, (p_23287_) -> {
         return true;
      }, (p_23285_) -> {
         return true;
      }, p_23263_, p_23264_, p_23265_);
   }

   public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(EntityType<? extends T> p_147567_, int p_147568_, Predicate<T> p_147569_, MemoryModuleType<T> p_147570_, float p_147571_, int p_147572_) {
      return new InteractWith<>(p_147567_, p_147568_, (p_147584_) -> {
         return true;
      }, p_147569_, p_147570_, p_147571_, p_147572_);
   }

   protected boolean checkExtraStartConditions(ServerLevel p_23254_, E p_23255_) {
      return this.selfFilter.test(p_23255_) && this.seesAtLeastOneValidTarget(p_23255_);
   }

   private boolean seesAtLeastOneValidTarget(E p_23267_) {
      List<LivingEntity> list = p_23267_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
      return list.stream().anyMatch(this::isTargetValid);
   }

   private boolean isTargetValid(LivingEntity p_23279_) {
      return this.type.equals(p_23279_.getType()) && this.targetFilter.test((T)p_23279_);
   }

   protected void start(ServerLevel p_23257_, E p_23258_, long p_23259_) {
      Brain<?> brain = p_23258_.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((p_23274_) -> {
         p_23274_.stream().filter((p_147582_) -> {
            return this.type.equals(p_147582_.getType());
         }).map((p_147580_) -> {
            return (T) p_147580_;
         }).filter((p_147575_) -> {
            return p_147575_.distanceToSqr(p_23258_) <= (double)this.interactionRangeSqr;
         }).filter(this.targetFilter).findFirst().ifPresent((p_147578_) -> {
            brain.setMemory(this.memory, (T)p_147578_);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(p_147578_, true));
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(p_147578_, false), this.speedModifier, this.maxDist));
         });
      });
   }
}
