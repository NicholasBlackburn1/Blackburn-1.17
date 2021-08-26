package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T>
{
    int getId(T pValue);

    @Nullable
    T byId(int pValue);
}
