package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag
{
    private static final int SELF_SIZE_IN_BITS = 128;
    public static final TagType<LongTag> TYPE = new TagType<LongTag>()
    {
        public LongTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(128L);
            return LongTag.valueOf(pInput.readLong());
        }
        public String getName()
        {
            return "LONG";
        }
        public String getPrettyName()
        {
            return "TAG_Long";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    private final long data;

    LongTag(long p_128877_)
    {
        this.data = p_128877_;
    }

    public static LongTag valueOf(long pValue)
    {
        return pValue >= -128L && pValue <= 1024L ? LongTag.Cache.cache[(int)pValue - -128] : new LongTag(pValue);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeLong(this.data);
    }

    public byte getId()
    {
        return 4;
    }

    public TagType<LongTag> getType()
    {
        return TYPE;
    }

    public LongTag copy()
    {
        return this;
    }

    public boolean equals(Object p_128894_)
    {
        if (this == p_128894_)
        {
            return true;
        }
        else
        {
            return p_128894_ instanceof LongTag && this.data == ((LongTag)p_128894_).data;
        }
    }

    public int hashCode()
    {
        return (int)(this.data ^ this.data >>> 32);
    }

    public void accept(TagVisitor p_177998_)
    {
        p_177998_.visitLong(this);
    }

    public long getAsLong()
    {
        return this.data;
    }

    public int getAsInt()
    {
        return (int)(this.data & -1L);
    }

    public short getAsShort()
    {
        return (short)((int)(this.data & 65535L));
    }

    public byte getAsByte()
    {
        return (byte)((int)(this.data & 255L));
    }

    public double getAsDouble()
    {
        return (double)this.data;
    }

    public float getAsFloat()
    {
        return (float)this.data;
    }

    public Number getAsNumber()
    {
        return this.data;
    }

    static class Cache
    {
        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final LongTag[] cache = new LongTag[1153];

        private Cache()
        {
        }

        static
        {
            for (int i = 0; i < cache.length; ++i)
            {
                cache[i] = new LongTag((long)(-128 + i));
            }
        }
    }
}
