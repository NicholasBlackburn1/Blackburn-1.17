package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   protected void doTick(ServerLevel p_26834_, LivingEntity p_26835_) {
      p_26835_.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(p_26835_));
   }

   private List<LivingEntity> getNearestVillagerBabies(LivingEntity p_26837_) {
      return this.getVisibleEntities(p_26837_).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
   }

   private boolean isVillagerBaby(LivingEntity p_26839_) {
      return p_26839_.getType() == EntityType.VILLAGER && p_26839_.isBaby();
   }

   private List<LivingEntity> getVisibleEntities(LivingEntity p_26841_) {
      return p_26841_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
   }
}