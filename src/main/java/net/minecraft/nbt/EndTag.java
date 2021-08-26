package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag
{
    private static final int SELF_SIZE_IN_BITS = 64;
    public static final TagType<EndTag> TYPE = new TagType<EndTag>()
    {
        public EndTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter)
        {
            pAccounter.accountBits(64L);
            return EndTag.INSTANCE;
        }
        public String getName()
        {
            return "END";
        }
        public String getPrettyName()
        {
            return "TAG_End";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    public static final EndTag INSTANCE = new EndTag();

    private EndTag()
    {
    }

    public void write(DataOutput pOutput) throws IOException
    {
    }

    public byte getId()
    {
        return 0;
    }

    public TagType<EndTag> getType()
    {
        return TYPE;
    }

    public String toString()
    {
        return this.getAsString();
    }

    public EndTag copy()
    {
        return this;
    }

    public void accept(TagVisitor p_177863_)
    {
        p_177863_.visitEnd(this);
    }
}
