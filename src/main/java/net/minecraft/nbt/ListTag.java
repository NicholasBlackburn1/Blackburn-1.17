package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag>
{
    private static final int SELF_SIZE_IN_BITS = 296;
    public static final TagType<ListTag> TYPE = new TagType<ListTag>()
    {
        public ListTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(296L);

            if (pDepth > 512)
            {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            else
            {
                byte b0 = pInput.readByte();
                int i = pInput.readInt();

                if (b0 == 0 && i > 0)
                {
                    throw new RuntimeException("Missing type on ListTag");
                }
                else
                {
                    pAccounter.accountBits(32L * (long)i);
                    TagType<?> tagtype = TagTypes.getType(b0);
                    List<Tag> list = Lists.newArrayListWithCapacity(i);

                    for (int j = 0; j < i; ++j)
                    {
                        list.add(tagtype.load(pInput, pDepth + 1, pAccounter));
                    }

                    return new ListTag(list, b0);
                }
            }
        }
        public String getName()
        {
            return "LIST";
        }
        public String getPrettyName()
        {
            return "TAG_List";
        }
    };
    private final List<Tag> list;
    private byte type;

    ListTag(List<Tag> p_128721_, byte p_128722_)
    {
        this.list = p_128721_;
        this.type = p_128722_;
    }

    public ListTag()
    {
        this(Lists.newArrayList(), (byte)0);
    }

    public void write(DataOutput pOutput) throws IOException
    {
        if (this.list.isEmpty())
        {
            this.type = 0;
        }
        else
        {
            this.type = this.list.get(0).getId();
        }

        pOutput.writeByte(this.type);
        pOutput.writeInt(this.list.size());

        for (Tag tag : this.list)
        {
            tag.write(pOutput);
        }
    }

    public byte getId()
    {
        return 9;
    }

    public TagType<ListTag> getType()
    {
        return TYPE;
    }

    public String toString()
    {
        return this.getAsString();
    }

    private void updateTypeAfterRemove()
    {
        if (this.list.isEmpty())
        {
            this.type = 0;
        }
    }

    public Tag remove(int p_128751_)
    {
        Tag tag = this.list.remove(p_128751_);
        this.updateTypeAfterRemove();
        return tag;
    }

    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 10)
            {
                return (CompoundTag)tag;
            }
        }

        return new CompoundTag();
    }

    public ListTag getList(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 9)
            {
                return (ListTag)tag;
            }
        }

        return new ListTag();
    }

    public short getShort(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 2)
            {
                return ((ShortTag)tag).getAsShort();
            }
        }

        return 0;
    }

    public int getInt(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 3)
            {
                return ((IntTag)tag).getAsInt();
            }
        }

        return 0;
    }

    public int[] getIntArray(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 11)
            {
                return ((IntArrayTag)tag).getAsIntArray();
            }
        }

        return new int[0];
    }

    public long[] getLongArray(int p_177992_)
    {
        if (p_177992_ >= 0 && p_177992_ < this.list.size())
        {
            Tag tag = this.list.get(p_177992_);

            if (tag.getId() == 11)
            {
                return ((LongArrayTag)tag).getAsLongArray();
            }
        }

        return new long[0];
    }

    public double getDouble(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 6)
            {
                return ((DoubleTag)tag).getAsDouble();
            }
        }

        return 0.0D;
    }

    public float getFloat(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);

            if (tag.getId() == 5)
            {
                return ((FloatTag)tag).getAsFloat();
            }
        }

        return 0.0F;
    }

    public String getString(int pI)
    {
        if (pI >= 0 && pI < this.list.size())
        {
            Tag tag = this.list.get(pI);
            return tag.getId() == 8 ? tag.getAsString() : tag.toString();
        }
        else
        {
            return "";
        }
    }

    public int size()
    {
        return this.list.size();
    }

    public Tag get(int p_128781_)
    {
        return this.list.get(p_128781_);
    }

    public Tag set(int p_128760_, Tag p_128761_)
    {
        Tag tag = this.get(p_128760_);

        if (!this.setTag(p_128760_, p_128761_))
        {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", p_128761_.getId(), this.type));
        }
        else
        {
            return tag;
        }
    }

    public void add(int p_128753_, Tag p_128754_)
    {
        if (!this.addTag(p_128753_, p_128754_))
        {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", p_128754_.getId(), this.type));
        }
    }

    public boolean setTag(int pIndex, Tag pNbt)
    {
        if (this.updateType(pNbt))
        {
            this.list.set(pIndex, pNbt);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean addTag(int pIndex, Tag pNbt)
    {
        if (this.updateType(pNbt))
        {
            this.list.add(pIndex, pNbt);
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean updateType(Tag pNbt)
    {
        if (pNbt.getId() == 0)
        {
            return false;
        }
        else if (this.type == 0)
        {
            this.type = pNbt.getId();
            return true;
        }
        else
        {
            return this.type == pNbt.getId();
        }
    }

    public ListTag copy()
    {
        Iterable<Tag> iterable = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
        List<Tag> list = Lists.newArrayList(iterable);
        return new ListTag(list, this.type);
    }

    public boolean equals(Object p_128766_)
    {
        if (this == p_128766_)
        {
            return true;
        }
        else
        {
            return p_128766_ instanceof ListTag && Objects.equals(this.list, ((ListTag)p_128766_).list);
        }
    }

    public int hashCode()
    {
        return this.list.hashCode();
    }

    public void accept(TagVisitor p_177990_)
    {
        p_177990_.visitList(this);
    }

    public byte getElementType()
    {
        return this.type;
    }

    public void clear()
    {
        this.list.clear();
        this.type = 0;
    }
}
