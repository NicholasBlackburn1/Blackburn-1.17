package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer
{
    protected final LootPoolEntryContainer[] children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(LootPoolEntryContainer[] p_79431_, LootItemCondition[] p_79432_)
    {
        super(p_79432_);
        this.children = p_79431_;
        this.composedChildren = this.m_5690_(p_79431_);
    }

    public void validate(ValidationContext p_79434_)
    {
        super.validate(p_79434_);

        if (this.children.length == 0)
        {
            p_79434_.reportProblem("Empty children list");
        }

        for (int i = 0; i < this.children.length; ++i)
        {
            this.children[i].validate(p_79434_.forChild(".entry[" + i + "]"));
        }
    }

    protected abstract ComposableEntryContainer m_5690_(ComposableEntryContainer[] p_79437_);

    public final boolean expand(LootContext p_79439_, Consumer<LootPoolEntry> p_79440_)
    {
        return !this.canRun(p_79439_) ? false : this.composedChildren.expand(p_79439_, p_79440_);
    }

    public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryBase.CompositeEntryConstructor<T> pFactory)
    {
        return new LootPoolEntryContainer.Serializer<T>()
        {
            public void serializeCustom(JsonObject pObject, T pContext, JsonSerializationContext pConditions)
            {
                pObject.add("children", pConditions.serialize(pContext.children));
            }
            public final T m_5921_(JsonObject p_79445_, JsonDeserializationContext p_79446_, LootItemCondition[] p_79447_)
            {
                LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(p_79445_, "children", p_79446_, LootPoolEntryContainer[].class);
                return pFactory.m_79460_(alootpoolentrycontainer, p_79447_);
            }
        };
    }

    @FunctionalInterface
    public interface CompositeEntryConstructor<T extends CompositeEntryBase>
    {
        T m_79460_(LootPoolEntryContainer[] p_79461_, LootItemCondition[] p_79462_);
    }
}
