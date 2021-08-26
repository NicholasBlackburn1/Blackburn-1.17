package net.minecraft.core;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class IdMapper<T> implements IdMap<T>
{
    public static final int DEFAULT = -1;
    private int nextId;
    private final IdentityHashMap<T, Integer> tToId;
    private final List<T> idToT;

    public IdMapper()
    {
        this(512);
    }

    public IdMapper(int p_122658_)
    {
        this.idToT = Lists.newArrayListWithExpectedSize(p_122658_);
        this.tToId = new IdentityHashMap<>(p_122658_);
    }

    public void addMapping(T pKey, int pValue)
    {
        this.tToId.put(pKey, pValue);

        while (this.idToT.size() <= pValue)
        {
            this.idToT.add((T)null);
        }

        this.idToT.set(pValue, pKey);

        if (this.nextId <= pValue)
        {
            this.nextId = pValue + 1;
        }
    }

    public void add(T pKey)
    {
        this.addMapping(pKey, this.nextId);
    }

    public int getId(T pValue)
    {
        Integer integer = this.tToId.get(pValue);
        return integer == null ? -1 : integer;
    }

    @Nullable
    public final T byId(int pValue)
    {
        return (T)(pValue >= 0 && pValue < this.idToT.size() ? this.idToT.get(pValue) : null);
    }

    public Iterator<T> iterator()
    {
        return Iterators.filter(this.idToT.iterator(), Predicates.notNull());
    }

    public boolean contains(int p_175381_)
    {
        return this.byId(p_175381_) != null;
    }

    public int size()
    {
        return this.tToId.size();
    }
}
