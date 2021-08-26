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

public class SetLookAndInteract extends Behavior<LivingEntity>
{
    private final EntityType<?> type;
    private final int interactionRangeSqr;
    private final Predicate<LivingEntity> targetFilter;
    private final Predicate<LivingEntity> selfFilter;

    public SetLookAndInteract(EntityType<?> p_23945_, int p_23946_, Predicate<LivingEntity> p_23947_, Predicate<LivingEntity> p_23948_)
    {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.type = p_23945_;
        this.interactionRangeSqr = p_23946_ * p_23946_;
        this.targetFilter = p_23948_;
        this.selfFilter = p_23947_;
    }

    public SetLookAndInteract(EntityType<?> p_23942_, int p_23943_)
    {
        this(p_23942_, p_23943_, (p_23973_) ->
        {
            return true;
        }, (p_23971_) ->
        {
            return true;
        });
    }

    public boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner)
    {
        return this.selfFilter.test(pOwner) && this.getVisibleEntities(pOwner).stream().anyMatch(this::isMatchingTarget);
    }

    public void start(ServerLevel pLevel, LivingEntity pEntity, long pGameTime)
    {
        super.start(pLevel, pEntity, pGameTime);
        Brain<?> brain = pEntity.getBrain();
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((p_23964_) ->
        {
            p_23964_.stream().filter((p_147899_) -> {
                return p_147899_.distanceToSqr(pEntity) <= (double)this.interactionRangeSqr;
            }).filter(this::isMatchingTarget).findFirst().ifPresent((p_147902_) -> {
                brain.setMemory(MemoryModuleType.INTERACTION_TARGET, p_147902_);
                brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(p_147902_, true));
            });
        });
    }

    private boolean isMatchingTarget(LivingEntity pLivingEntity)
    {
        return this.type.equals(pLivingEntity.getType()) && this.targetFilter.test(pLivingEntity);
    }

    private List<LivingEntity> getVisibleEntities(LivingEntity pLivingEntity)
    {
        return pLivingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
    }
}
