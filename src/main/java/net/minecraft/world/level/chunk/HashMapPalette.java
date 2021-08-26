package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T>
{
    private final IdMapper<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final int bits;

    public HashMapPalette(IdMapper<T> p_62664_, int p_62665_, PaletteResize<T> p_62666_, Function<CompoundTag, T> p_62667_, Function<T, CompoundTag> p_62668_)
    {
        this.registry = p_62664_;
        this.bits = p_62665_;
        this.resizeHandler = p_62666_;
        this.reader = p_62667_;
        this.writer = p_62668_;
        this.values = new CrudeIncrementalIntIdentityHashBiMap<>(1 << p_62665_);
    }

    public int idFor(T pState)
    {
        int i = this.values.getId(pState);

        if (i == -1)
        {
            i = this.values.add(pState);

            if (i >= 1 << this.bits)
            {
                i = this.resizeHandler.onResize(this.bits + 1, pState);
            }
        }

        return i;
    }

    public boolean maybeHas(Predicate<T> p_62675_)
    {
        for (int i = 0; i < this.getSize(); ++i)
        {
            if (p_62675_.test(this.values.byId(i)))
            {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public T valueFor(int pIndexKey)
    {
        return this.values.byId(pIndexKey);
    }

    public void read(FriendlyByteBuf pNbt)
    {
        this.values.clear();
        int i = pNbt.readVarInt();

        for (int j = 0; j < i; ++j)
        {
            this.values.add(this.registry.byId(pNbt.readVarInt()));
        }
    }

    public void write(FriendlyByteBuf pPaletteList)
    {
        int i = this.getSize();
        pPaletteList.writeVarInt(i);

        for (int j = 0; j < i; ++j)
        {
            pPaletteList.writeVarInt(this.registry.getId(this.values.byId(j)));
        }
    }

    public int getSerializedSize()
    {
        int i = FriendlyByteBuf.getVarIntSize(this.getSize());

        for (int j = 0; j < this.getSize(); ++j)
        {
            i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
        }

        return i;
    }

    public int getSize()
    {
        return this.values.size();
    }

    public void read(ListTag pNbt)
    {
        this.values.clear();

        for (int i = 0; i < pNbt.size(); ++i)
        {
            this.values.add(this.reader.apply(pNbt.getCompound(i)));
        }
    }

    public void write(ListTag pPaletteList)
    {
        for (int i = 0; i < this.getSize(); ++i)
        {
            pPaletteList.add(this.writer.apply(this.values.byId(i)));
        }
    }
}
