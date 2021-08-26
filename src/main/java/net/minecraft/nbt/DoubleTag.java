package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class DoubleTag extends NumericTag
{
    private static final int SELF_SIZE_IN_BITS = 128;
    public static final DoubleTag ZERO = new DoubleTag(0.0D);
    public static final TagType<DoubleTag> TYPE = new TagType<DoubleTag>()
    {
        public DoubleTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(128L);
            return DoubleTag.valueOf(pInput.readDouble());
        }
        public String getName()
        {
            return "DOUBLE";
        }
        public String getPrettyName()
        {
            return "TAG_Double";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    private final double data;

    private DoubleTag(double p_128498_)
    {
        this.data = p_128498_;
    }

    public static DoubleTag valueOf(double pValue)
    {
        return pValue == 0.0D ? ZERO : new DoubleTag(pValue);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeDouble(this.data);
    }

    public byte getId()
    {
        return 6;
    }

    public TagType<DoubleTag> getType()
    {
        return TYPE;
    }

    public DoubleTag copy()
    {
        return this;
    }

    public boolean equals(Object p_128512_)
    {
        if (this == p_128512_)
        {
            return true;
        }
        else
        {
            return p_128512_ instanceof DoubleTag && this.data == ((DoubleTag)p_128512_).data;
        }
    }

    public int hashCode()
    {
        long i = Double.doubleToLongBits(this.data);
        return (int)(i ^ i >>> 32);
    }

    public void accept(TagVisitor p_177860_)
    {
        p_177860_.visitDouble(this);
    }

    public long getAsLong()
    {
        return (long)Math.floor(this.data);
    }

    public int getAsInt()
    {
        return Mth.floor(this.data);
    }

    public short getAsShort()
    {
        return (short)(Mth.floor(this.data) & 65535);
    }

    public byte getAsByte()
    {
        return (byte)(Mth.floor(this.data) & 255);
    }

    public double getAsDouble()
    {
        return this.data;
    }

    public float getAsFloat()
    {
        return (float)this.data;
    }

    public Number getAsNumber()
    {
        return this.data;
    }
}
