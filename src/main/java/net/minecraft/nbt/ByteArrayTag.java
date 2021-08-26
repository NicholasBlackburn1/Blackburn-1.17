package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag>
{
    private static final int SELF_SIZE_IN_BITS = 192;
    public static final TagType<ByteArrayTag> TYPE = new TagType<ByteArrayTag>()
    {
        public ByteArrayTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(192L);
            int i = pInput.readInt();
            pAccounter.accountBits(8L * (long)i);
            byte[] abyte = new byte[i];
            pInput.readFully(abyte);
            return new ByteArrayTag(abyte);
        }
        public String getName()
        {
            return "BYTE[]";
        }
        public String getPrettyName()
        {
            return "TAG_Byte_Array";
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] p_128191_)
    {
        this.data = p_128191_;
    }

    public ByteArrayTag(List<Byte> p_128189_)
    {
        this(toArray(p_128189_));
    }

    private static byte[] toArray(List<Byte> pBytes)
    {
        byte[] abyte = new byte[pBytes.size()];

        for (int i = 0; i < pBytes.size(); ++i)
        {
            Byte obyte = pBytes.get(i);
            abyte[i] = obyte == null ? 0 : obyte;
        }

        return abyte;
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeInt(this.data.length);
        pOutput.write(this.data);
    }

    public byte getId()
    {
        return 7;
    }

    public TagType<ByteArrayTag> getType()
    {
        return TYPE;
    }

    public String toString()
    {
        return this.getAsString();
    }

    public Tag copy()
    {
        byte[] abyte = new byte[this.data.length];
        System.arraycopy(this.data, 0, abyte, 0, this.data.length);
        return new ByteArrayTag(abyte);
    }

    public boolean equals(Object p_128233_)
    {
        if (this == p_128233_)
        {
            return true;
        }
        else
        {
            return p_128233_ instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)p_128233_).data);
        }
    }

    public int hashCode()
    {
        return Arrays.hashCode(this.data);
    }

    public void accept(TagVisitor p_177839_)
    {
        p_177839_.visitByteArray(this);
    }

    public byte[] getAsByteArray()
    {
        return this.data;
    }

    public int size()
    {
        return this.data.length;
    }

    public ByteTag get(int p_128194_)
    {
        return ByteTag.valueOf(this.data[p_128194_]);
    }

    public ByteTag set(int p_128196_, ByteTag p_128197_)
    {
        byte b0 = this.data[p_128196_];
        this.data[p_128196_] = p_128197_.getAsByte();
        return ByteTag.valueOf(b0);
    }

    public void add(int p_128215_, ByteTag p_128216_)
    {
        this.data = ArrayUtils.add(this.data, p_128215_, p_128216_.getAsByte());
    }

    public boolean setTag(int pIndex, Tag pNbt)
    {
        if (pNbt instanceof NumericTag)
        {
            this.data[pIndex] = ((NumericTag)pNbt).getAsByte();
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
            this.data = ArrayUtils.add(this.data, pIndex, ((NumericTag)pNbt).getAsByte());
            return true;
        }
        else
        {
            return false;
        }
    }

    public ByteTag remove(int p_128213_)
    {
        byte b0 = this.data[p_128213_];
        this.data = ArrayUtils.remove(this.data, p_128213_);
        return ByteTag.valueOf(b0);
    }

    public byte getElementType()
    {
        return 1;
    }

    public void clear()
    {
        this.data = new byte[0];
    }
}
