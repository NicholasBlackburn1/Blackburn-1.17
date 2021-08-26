package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class PalettedContainer<T> implements PaletteResize<T>
{
    private static final int SIZE = 4096;
    public static final int GLOBAL_PALETTE_BITS = 9;
    public static final int MIN_PALETTE_SIZE = 4;
    private final Palette<T> globalPalette;
    private final PaletteResize<T> dummyPaletteResize = (p_63139_, p_63140_) ->
    {
        return 0;
    };
    private final IdMapper<T> registry;
    private final Function<CompoundTag, T> reader;
    private final Function<T, CompoundTag> writer;
    private final T defaultValue;
    protected BitStorage storage;
    private Palette<T> palette;
    private int bits;
    private final Semaphore lock = new Semaphore(1);
    @Nullable
    private final DebugBuffer<Pair<Thread, StackTraceElement[]>> traces = null;

    public void acquire()
    {
        if (this.traces != null)
        {
            Thread thread = Thread.currentThread();
            this.traces.push(Pair.of(thread, thread.getStackTrace()));
        }

        ThreadingDetector.checkAndLock(this.lock, this.traces, "PalettedContainer");
    }

    public void release()
    {
        this.lock.release();
    }

    public PalettedContainer(Palette<T> p_63079_, IdMapper<T> p_63080_, Function<CompoundTag, T> p_63081_, Function<T, CompoundTag> p_63082_, T p_63083_)
    {
        this.globalPalette = p_63079_;
        this.registry = p_63080_;
        this.reader = p_63081_;
        this.writer = p_63082_;
        this.defaultValue = p_63083_;
        this.setBits(4);
    }

    private static int getIndex(int pX, int pY, int pZ)
    {
        return pY << 8 | pZ << 4 | pX;
    }

    private void setBits(int pBits)
    {
        if (pBits != this.bits)
        {
            this.bits = pBits;

            if (this.bits <= 4)
            {
                this.bits = 4;
                this.palette = new LinearPalette<>(this.registry, this.bits, this, this.reader);
            }
            else if (this.bits < 9)
            {
                this.palette = new HashMapPalette<>(this.registry, this.bits, this, this.reader, this.writer);
            }
            else
            {
                this.palette = this.globalPalette;
                this.bits = Mth.ceillog2(this.registry.size());
            }

            this.palette.idFor(this.defaultValue);
            this.storage = new BitStorage(this.bits, 4096);
        }
    }

    public int onResize(int p_63142_, T p_63143_)
    {
        BitStorage bitstorage = this.storage;
        Palette<T> palette = this.palette;
        this.setBits(p_63142_);

        for (int i = 0; i < bitstorage.getSize(); ++i)
        {
            T t = palette.valueFor(bitstorage.get(i));

            if (t != null)
            {
                this.set(i, t);
            }
        }

        return this.palette.idFor(p_63143_);
    }

    public T getAndSet(int pX, int pY, int pZ, T pState)
    {
        Object object;

        try
        {
            this.acquire();
            T t = this.getAndSet(getIndex(pX, pY, pZ), pState);
            object = t;
        }
        finally
        {
            this.release();
        }

        return (T)object;
    }

    public T getAndSetUnchecked(int pX, int pY, int pZ, T pState)
    {
        return this.getAndSet(getIndex(pX, pY, pZ), pState);
    }

    private T getAndSet(int pX, T pY)
    {
        int i = this.palette.idFor(pY);
        int j = this.storage.getAndSet(pX, i);
        T t = this.palette.valueFor(j);
        return (T)(t == null ? this.defaultValue : t);
    }

    public void set(int pIndex, int pState, int p_156473_, T p_156474_)
    {
        try
        {
            this.acquire();
            this.set(getIndex(pIndex, pState, p_156473_), p_156474_);
        }
        finally
        {
            this.release();
        }
    }

    private void set(int pIndex, T pState)
    {
        int i = this.palette.idFor(pState);
        this.storage.set(pIndex, i);
    }

    public T get(int pIndex, int p_63089_, int p_63090_)
    {
        return this.get(getIndex(pIndex, p_63089_, p_63090_));
    }

    protected T get(int pIndex)
    {
        T t = this.palette.valueFor(this.storage.get(pIndex));
        return (T)(t == null ? this.defaultValue : t);
    }

    public void read(FriendlyByteBuf pPaletteNbt)
    {
        try
        {
            this.acquire();
            int i = pPaletteNbt.readByte();

            if (this.bits != i)
            {
                this.setBits(i);
            }

            this.palette.read(pPaletteNbt);
            pPaletteNbt.m_130105_(this.storage.getRaw());
        }
        finally
        {
            this.release();
        }
    }

    public void write(FriendlyByteBuf pCompound)
    {
        try
        {
            this.acquire();
            pCompound.writeByte(this.bits);
            this.palette.write(pCompound);
            pCompound.m_130091_(this.storage.getRaw());
        }
        finally
        {
            this.release();
        }
    }

    public void m_63115_(ListTag p_63116_, long[] p_63117_)
    {
        try
        {
            this.acquire();
            int i = Math.max(4, Mth.ceillog2(p_63116_.size()));

            if (i != this.bits)
            {
                this.setBits(i);
            }

            this.palette.read(p_63116_);
            int j = p_63117_.length * 64 / 4096;

            if (this.palette == this.globalPalette)
            {
                Palette<T> palette = new HashMapPalette<>(this.registry, i, this.dummyPaletteResize, this.reader, this.writer);
                palette.read(p_63116_);
                BitStorage bitstorage = new BitStorage(i, 4096, p_63117_);

                for (int k = 0; k < 4096; ++k)
                {
                    this.storage.set(k, this.globalPalette.idFor(palette.valueFor(bitstorage.get(k))));
                }
            }
            else if (j == this.bits)
            {
                System.arraycopy(p_63117_, 0, this.storage.getRaw(), 0, p_63117_.length);
            }
            else
            {
                BitStorage bitstorage1 = new BitStorage(j, 4096, p_63117_);

                for (int l = 0; l < 4096; ++l)
                {
                    this.storage.set(l, bitstorage1.get(l));
                }
            }
        }
        finally
        {
            this.release();
        }
    }

    public void write(CompoundTag pCompound, String pPaletteName, String pPaletteDataName)
    {
        try
        {
            this.acquire();
            HashMapPalette<T> hashmappalette = new HashMapPalette<>(this.registry, this.bits, this.dummyPaletteResize, this.reader, this.writer);
            T t = this.defaultValue;
            int i = hashmappalette.idFor(this.defaultValue);
            int[] aint = new int[4096];

            for (int j = 0; j < 4096; ++j)
            {
                T t1 = this.get(j);

                if (t1 != t)
                {
                    t = t1;
                    i = hashmappalette.idFor(t1);
                }

                aint[j] = i;
            }

            ListTag listtag = new ListTag();
            hashmappalette.write(listtag);
            pCompound.put(pPaletteName, listtag);
            int l = Math.max(4, Mth.ceillog2(listtag.size()));
            BitStorage bitstorage = new BitStorage(l, 4096);

            for (int k = 0; k < aint.length; ++k)
            {
                bitstorage.set(k, aint[k]);
            }

            pCompound.m_128388_(pPaletteDataName, bitstorage.getRaw());
        }
        finally
        {
            this.release();
        }
    }

    public int getSerializedSize()
    {
        return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
    }

    public boolean maybeHas(Predicate<T> p_63110_)
    {
        return this.palette.maybeHas(p_63110_);
    }

    public void count(PalettedContainer.CountConsumer<T> pCountConsumer)
    {
        Int2IntMap int2intmap = new Int2IntOpenHashMap();
        this.storage.getAll((p_156469_) ->
        {
            int2intmap.put(p_156469_, int2intmap.get(p_156469_) + 1);
        });
        int2intmap.int2IntEntrySet().forEach((p_156466_) ->
        {
            pCountConsumer.accept(this.palette.valueFor(p_156466_.getIntKey()), p_156466_.getIntValue());
        });
    }

    @FunctionalInterface
    public interface CountConsumer<T>
    {
        void accept(T p_63145_, int p_63146_);
    }
}
