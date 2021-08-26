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

public class VillagerBabiesSensor extends Sensor<LivingEntity>
{
    public Set < MemoryModuleType<? >> requires()
    {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
    }

    protected void doTick(ServerLevel pLevel, LivingEntity pEntity)
    {
        pEntity.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(pEntity));
    }

    private List<LivingEntity> getNearestVillagerBabies(LivingEntity pLivingEntity)
    {
        return this.getVisibleEntities(pLivingEntity).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
    }

    private boolean isVillagerBaby(LivingEntity pLivingEntity)
    {
        return pLivingEntity.getType() == EntityType.VILLAGER && pLivingEntity.isBaby();
    }

    private List<LivingEntity> getVisibleEntities(LivingEntity pLivingEntity)
    {
        return pLivingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
    }
}
