package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class LootContextParamSet
{
    private final Set < LootContextParam<? >> required;
    private final Set < LootContextParam<? >> all;

    LootContextParamSet(Set < LootContextParam<? >> p_81388_, Set < LootContextParam<? >> p_81389_)
    {
        this.required = ImmutableSet.copyOf(p_81388_);
        this.all = ImmutableSet.copyOf(Sets.union(p_81388_, p_81389_));
    }

    public boolean isAllowed(LootContextParam<?> p_165476_)
    {
        return this.all.contains(p_165476_);
    }

    public Set < LootContextParam<? >> getRequired()
    {
        return this.required;
    }

    public Set < LootContextParam<? >> getAllowed()
    {
        return this.all;
    }

    public String toString()
    {
        return "[" + Joiner.on(", ").join(this.all.stream().map((p_81400_) ->
        {
            return (this.required.contains(p_81400_) ? "!" : "") + p_81400_.getName();
        }).iterator()) + "]";
    }

    public void validateUser(ValidationContext p_81396_, LootContextUser p_81397_)
    {
        Set < LootContextParam<? >> set = p_81397_.getReferencedContextParams();
        Set < LootContextParam<? >> set1 = Sets.difference(set, this.all);

        if (!set1.isEmpty())
        {
            p_81396_.reportProblem("Parameters " + set1 + " are not provided in this context");
        }
    }

    public static LootContextParamSet.Builder builder()
    {
        return new LootContextParamSet.Builder();
    }

    public static class Builder
    {
        private final Set < LootContextParam<? >> required = Sets.newIdentityHashSet();
        private final Set < LootContextParam<? >> optional = Sets.newIdentityHashSet();

        public LootContextParamSet.Builder required(LootContextParam<?> pParameter)
        {
            if (this.optional.contains(pParameter))
            {
                throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already optional");
            }
            else
            {
                this.required.add(pParameter);
                return this;
            }
        }

        public LootContextParamSet.Builder optional(LootContextParam<?> pParameter)
        {
            if (this.required.contains(pParameter))
            {
                throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already required");
            }
            else
            {
                this.optional.add(pParameter);
                return this;
            }
        }

        public LootContextParamSet build()
        {
            return new LootContextParamSet(this.required, this.optional);
        }
    }
}
