package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T> extends ObjectiveCriteria
{
    private final StatFormatter formatter;
    private final T value;
    private final StatType<T> type;

    protected Stat(StatType<T> p_12856_, T p_12857_, StatFormatter p_12858_)
    {
        super(buildName(p_12856_, p_12857_));
        this.type = p_12856_;
        this.formatter = p_12858_;
        this.value = p_12857_;
    }

    public static <T> String buildName(StatType<T> pType, T pValue)
    {
        return locationToKey(Registry.STAT_TYPE.getKey(pType)) + ":" + locationToKey(pType.getRegistry().getKey(pValue));
    }

    private static <T> String locationToKey(@Nullable ResourceLocation pId)
    {
        return pId.toString().replace(':', '.');
    }

    public StatType<T> getType()
    {
        return this.type;
    }

    public T getValue()
    {
        return this.value;
    }

    public String format(int pNumber)
    {
        return this.formatter.format(pNumber);
    }

    public boolean equals(Object p_12869_)
    {
        return this == p_12869_ || p_12869_ instanceof Stat && Objects.equals(this.getName(), ((Stat)p_12869_).getName());
    }

    public int hashCode()
    {
        return this.getName().hashCode();
    }

    public String toString()
    {
        return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + "}";
    }
}
