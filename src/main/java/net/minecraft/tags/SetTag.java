package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T>
{
    private final ImmutableList<T> valuesList;
    private final Set<T> values;
    @VisibleForTesting
    protected final Class<?> closestCommonSuperType;

    protected SetTag(Set<T> p_13214_, Class<?> p_13215_)
    {
        this.closestCommonSuperType = p_13215_;
        this.values = p_13214_;
        this.valuesList = ImmutableList.copyOf(p_13214_);
    }

    public static <T> SetTag<T> empty()
    {
        return new SetTag<>(ImmutableSet.of(), Void.class);
    }

    public static <T> SetTag<T> create(Set<T> pContents)
    {
        return new SetTag<>(pContents, findCommonSuperClass(pContents));
    }

    public boolean contains(T pElement)
    {
        return this.closestCommonSuperType.isInstance(pElement) && this.values.contains(pElement);
    }

    public List<T> getValues()
    {
        return this.valuesList;
    }

    private static <T> Class<?> findCommonSuperClass(Set<T> pContents)
    {
        if (pContents.isEmpty())
        {
            return Void.class;
        }
        else
        {
            Class<?> oclass = null;

            for (T t : pContents)
            {
                if (oclass == null)
                {
                    oclass = t.getClass();
                }
                else
                {
                    oclass = findClosestAncestor(oclass, t.getClass());
                }
            }

            return oclass;
        }
    }

    private static Class<?> findClosestAncestor(Class<?> pInput, Class<?> pComparison)
    {
        while (!pInput.isAssignableFrom(pComparison))
        {
            pInput = pInput.getSuperclass();
        }

        return pInput;
    }
}
