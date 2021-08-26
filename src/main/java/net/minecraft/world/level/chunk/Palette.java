package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T>
{
    int idFor(T pState);

    boolean maybeHas(Predicate<T> p_63062_);

    @Nullable
    T valueFor(int pIndexKey);

    void read(FriendlyByteBuf pNbt);

    void write(FriendlyByteBuf pBuf);

    int getSerializedSize();

    int getSize();

    void read(ListTag pNbt);
}
