package net.minecraft.network.syncher;

import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T>
{
    void write(FriendlyByteBuf pBuf, T pValue);

    T read(FriendlyByteBuf pBuf);

default EntityDataAccessor<T> createAccessor(int pId)
    {
        return new EntityDataAccessor<>(pId, this);
    }

    T copy(T pValue);
}
