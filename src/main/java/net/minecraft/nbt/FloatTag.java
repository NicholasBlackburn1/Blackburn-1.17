package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag
{
    private static final int SELF_SIZE_IN_BITS = 96;
    public static final FloatTag ZERO = new FloatTag(0.0F);
    public static final TagType<FloatTag> TYPE = new TagType<FloatTag>()
    {
        public FloatTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(96L);
            return FloatTag.valueOf(pInput.readFloat());
        }
        public String getName()
        {
            return "FLOAT";
        }
        public String getPrettyName()
        {
            return "TAG_Float";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    private final float data;

    private FloatTag(float p_128564_)
    {
        this.data = p_128564_;
    }

    public static FloatTag valueOf(float pValue)
    {
        return pValue == 0.0F ? ZERO : new FloatTag(pValue);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeFloat(this.data);
    }

    public byte getId()
    {
        return 5;
    }

    public TagType<FloatTag> getType()
    {
        return TYPE;
    }

    public FloatTag copy()
    {
        return this;
    }

    public boolean equals(Object p_128578_)
    {
        if (this == p_128578_)
        {
            return true;
        }
        else
        {
            return p_128578_ instanceof FloatTag && this.data == ((FloatTag)p_128578_).data;
        }
    }

    public int hashCode()
    {
        return Float.floatToIntBits(this.data);
    }

    public void accept(TagVisitor p_177866_)
    {
        p_177866_.visitFloat(this);
    }

    public long getAsLong()
    {
        return (long)this.data;
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
        return (double)this.data;
    }

    public float getAsFloat()
    {
        return this.data;
    }

    public Number getAsNumber()
    {
        return this.data;
    }
}
