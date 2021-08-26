package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor extends Sensor<Hoglin>
{
    public Set < MemoryModuleType<? >> requires()
    {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT);
    }

    protected void doTick(ServerLevel pLevel, Hoglin pEntity)
    {
        Brain<?> brain = pEntity.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(pLevel, pEntity));
        Optional<Piglin> optional = Optional.empty();
        int i = 0;
        List<Hoglin> list = Lists.newArrayList();

        for (LivingEntity livingentity : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList()))
        {
            if (livingentity instanceof Piglin && !livingentity.isBaby())
            {
                ++i;

                if (!optional.isPresent())
                {
                    optional = Optional.of((Piglin)livingentity);
                }
            }

            if (livingentity instanceof Hoglin && !livingentity.isBaby())
            {
                list.add((Hoglin)livingentity);
            }
        }

        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel pLevel, Hoglin pHoglin)
    {
        return BlockPos.findClosestMatch(pHoglin.blockPosition(), 8, 4, (p_26663_) ->
        {
            return pLevel.getBlockState(p_26663_).is(BlockTags.HOGLIN_REPELLENTS);
        });
    }
}
