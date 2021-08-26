package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;

public class Swim extends Behavior<Mob>
{
    private final float chance;

    public Swim(float p_24383_)
    {
        super(ImmutableMap.of());
        this.chance = p_24383_;
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, Mob pOwner)
    {
        return pOwner.isInWater() && pOwner.getFluidHeight(FluidTags.WATER) > pOwner.getFluidJumpThreshold() || pOwner.isInLava();
    }

    protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime)
    {
        return this.checkExtraStartConditions(pLevel, pEntity);
    }

    protected void tick(ServerLevel pLevel, Mob pOwner, long pGameTime)
    {
        if (pOwner.getRandom().nextFloat() < this.chance)
        {
            pOwner.getJumpControl().jump();
        }
    }
}
