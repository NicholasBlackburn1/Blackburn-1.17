package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag
{
    private static final int SELF_SIZE_IN_BITS = 80;
    public static final TagType<ShortTag> TYPE = new TagType<ShortTag>()
    {
        public ShortTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(80L);
            return ShortTag.valueOf(pInput.readShort());
        }
        public String getName()
        {
            return "SHORT";
        }
        public String getPrettyName()
        {
            return "TAG_Short";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    private final short data;

    ShortTag(short p_129248_)
    {
        this.data = p_129248_;
    }

    public static ShortTag valueOf(short pValue)
    {
        return pValue >= -128 && pValue <= 1024 ? ShortTag.Cache.cache[pValue - -128] : new ShortTag(pValue);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeShort(this.data);
    }

    public byte getId()
    {
        return 2;
    }

    public TagType<ShortTag> getType()
    {
        return TYPE;
    }

    public ShortTag copy()
    {
        return this;
    }

    public boolean equals(Object p_129265_)
    {
        if (this == p_129265_)
        {
            return true;
        }
        else
        {
            return p_129265_ instanceof ShortTag && this.data == ((ShortTag)p_129265_).data;
        }
    }

    public int hashCode()
    {
        return this.data;
    }

    public void accept(TagVisitor p_178084_)
    {
        p_178084_.visitShort(this);
    }

    public long getAsLong()
    {
        return (long)this.data;
    }

    public int getAsInt()
    {
        return this.data;
    }

    public short getAsShort()
    {
        return this.data;
    }

    public byte getAsByte()
    {
        return (byte)(this.data & 255);
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
        static final ShortTag[] cache = new ShortTag[1153];

        private Cache()
        {
        }

        static
        {
            for (int i = 0; i < cache.length; ++i)
            {
                cache[i] = new ShortTag((short)(-128 + i));
            }
        }
    }
}
