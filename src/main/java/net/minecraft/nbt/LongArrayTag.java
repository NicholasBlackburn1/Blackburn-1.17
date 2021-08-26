package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag extends CollectionTag<LongTag>
{
    private static final int SELF_SIZE_IN_BITS = 192;
    public static final TagType<LongArrayTag> TYPE = new TagType<LongArrayTag>()
    {
        public LongArrayTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(192L);
            int i = pInput.readInt();
            pAccounter.accountBits(64L * (long)i);
            long[] along = new long[i];

            for (int j = 0; j < i; ++j)
            {
                along[j] = pInput.readLong();
            }

            return new LongArrayTag(along);
        }
        public String getName()
        {
            return "LONG[]";
        }
        public String getPrettyName()
        {
            return "TAG_Long_Array";
        }
    };
    private long[] data;

    public LongArrayTag(long[] p_128808_)
    {
        this.data = p_128808_;
    }

    public LongArrayTag(LongSet p_128804_)
    {
        this.data = p_128804_.toLongArray();
    }

    public LongArrayTag(List<Long> p_128806_)
    {
        this(toArray(p_128806_));
    }

    private static long[] toArray(List<Long> pLongs)
    {
        long[] along = new long[pLongs.size()];

        for (int i = 0; i < pLongs.size(); ++i)
        {
            Long olong = pLongs.get(i);
            along[i] = olong == null ? 0L : olong;
        }

        return along;
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeInt(this.data.length);

        for (long i : this.data)
        {
            pOutput.writeLong(i);
        }
    }

    public byte getId()
    {
        return 12;
    }

    public TagType<LongArrayTag> getType()
    {
        return TYPE;
    }

    public String toString()
    {
        return this.getAsString();
    }

    public LongArrayTag copy()
    {
        long[] along = new long[this.data.length];
        System.arraycopy(this.data, 0, along, 0, this.data.length);
        return new LongArrayTag(along);
    }

    public boolean equals(Object p_128850_)
    {
        if (this == p_128850_)
        {
            return true;
        }
        else
        {
            return p_128850_ instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)p_128850_).data);
        }
    }

    public int hashCode()
    {
        return Arrays.hashCode(this.data);
    }

    public void accept(TagVisitor p_177995_)
    {
        p_177995_.visitLongArray(this);
    }

    public long[] getAsLongArray()
    {
        return this.data;
    }

    public int size()
    {
        return this.data.length;
    }

    public LongTag get(int p_128811_)
    {
        return LongTag.valueOf(this.data[p_128811_]);
    }

    public LongTag set(int p_128813_, LongTag p_128814_)
    {
        long i = this.data[p_128813_];
        this.data[p_128813_] = p_128814_.getAsLong();
        return LongTag.valueOf(i);
    }

    public void add(int p_128832_, LongTag p_128833_)
    {
        this.data = ArrayUtils.add(this.data, p_128832_, p_128833_.getAsLong());
    }

    public boolean setTag(int pIndex, Tag pNbt)
    {
        if (pNbt instanceof NumericTag)
        {
            this.data[pIndex] = ((NumericTag)pNbt).getAsLong();
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean addTag(int pIndex, Tag pNbt)
    {
        if (pNbt instanceof NumericTag)
        {
            this.data = ArrayUtils.add(this.data, pIndex, ((NumericTag)pNbt).getAsLong());
            return true;
        }
        else
        {
            return false;
        }
    }

    public LongTag remove(int p_128830_)
    {
        long i = this.data[p_128830_];
        this.data = ArrayUtils.remove(this.data, p_128830_);
        return LongTag.valueOf(i);
    }

    public byte getElementType()
    {
        return 4;
    }

    public void clear()
    {
        this.data = new long[0];
    }
}
