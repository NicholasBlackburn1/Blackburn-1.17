package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringTag implements Tag
{
    private static final int SELF_SIZE_IN_BITS = 288;
    public static final TagType<StringTag> TYPE = new TagType<StringTag>()
    {
        public StringTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(288L);
            String s = pInput.readUTF();
            pAccounter.accountBits((long)(16 * s.length()));
            return StringTag.valueOf(s);
        }
        public String getName()
        {
            return "STRING";
        }
        public String getPrettyName()
        {
            return "TAG_String";
        }
        public boolean isValue()
        {
            return true;
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';
    private final String data;

    private StringTag(String p_129293_)
    {
        Objects.requireNonNull(p_129293_, "Null string not allowed");
        this.data = p_129293_;
    }

    public static StringTag valueOf(String pValue)
    {
        return pValue.isEmpty() ? EMPTY : new StringTag(pValue);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        pOutput.writeUTF(this.data);
    }

    public byte getId()
    {
        return 8;
    }

    public TagType<StringTag> getType()
    {
        return TYPE;
    }

    public String toString()
    {
        return Tag.super.getAsString();
    }

    public StringTag copy()
    {
        return this;
    }

    public boolean equals(Object p_129308_)
    {
        if (this == p_129308_)
        {
            return true;
        }
        else
        {
            return p_129308_ instanceof StringTag && Objects.equals(this.data, ((StringTag)p_129308_).data);
        }
    }

    public int hashCode()
    {
        return this.data.hashCode();
    }

    public String getAsString()
    {
        return this.data;
    }

    public void accept(TagVisitor p_178154_)
    {
        p_178154_.visitString(this);
    }

    public static String quoteAndEscape(String pName)
    {
        StringBuilder stringbuilder = new StringBuilder(" ");
        char c0 = 0;

        for (int i = 0; i < pName.length(); ++i)
        {
            char c1 = pName.charAt(i);

            if (c1 == '\\')
            {
                stringbuilder.append('\\');
            }
            else if (c1 == '"' || c1 == '\'')
            {
                if (c0 == 0)
                {
                    c0 = (char)(c1 == '"' ? 39 : 34);
                }

                if (c0 == c1)
                {
                    stringbuilder.append('\\');
                }
            }

            stringbuilder.append(c1);
        }

        if (c0 == 0)
        {
            c0 = '"';
        }

        stringbuilder.setCharAt(0, c0);
        stringbuilder.append(c0);
        return stringbuilder.toString();
    }
}
