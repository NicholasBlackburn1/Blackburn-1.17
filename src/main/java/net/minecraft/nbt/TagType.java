package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag>
{
    T load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException;

default boolean isValue()
    {
        return false;
    }

    String getName();

    String getPrettyName();

    static TagType<EndTag> createInvalid(final int pId)
    {
        return new TagType<EndTag>()
        {
            public EndTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter)
            {
                throw new IllegalArgumentException("Invalid tag id: " + pId);
            }
            public String getName()
            {
                return "INVALID[" + pId + "]";
            }
            public String getPrettyName()
            {
                return "UNKNOWN_" + pId;
            }
        };
    }
}
