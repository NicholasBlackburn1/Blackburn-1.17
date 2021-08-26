package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag
{
    public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap((p_128336_) ->
    {
        Tag tag = p_128336_.convert(NbtOps.INSTANCE).getValue();
        return tag instanceof CompoundTag ? DataResult.success((CompoundTag)tag) : DataResult.error("Not a compound tag: " + tag);
    }, (p_128412_) ->
    {
        return new Dynamic<>(NbtOps.INSTANCE, p_128412_);
    });
    private static final int SELF_SIZE_IN_BITS = 384;
    private static final int MAP_ENTRY_SIZE_IN_BITS = 256;
    public static final TagType<CompoundTag> TYPE = new TagType<CompoundTag>()
    {
        public CompoundTag load(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
        {
            pAccounter.accountBits(384L);

            if (pDepth > 512)
            {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            else
            {
                Map<String, Tag> map = Maps.newHashMap();
                byte b0;

                while ((b0 = CompoundTag.readNamedTagType(pInput, pAccounter)) != 0)
                {
                    String s = CompoundTag.readNamedTagName(pInput, pAccounter);
                    pAccounter.accountBits((long)(224 + 16 * s.length()));
                    Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b0), s, pInput, pDepth + 1, pAccounter);

                    if (map.put(s, tag) != null)
                    {
                        pAccounter.accountBits(288L);
                    }
                }

                return new CompoundTag(map);
            }
        }
        public String getName()
        {
            return "COMPOUND";
        }
        public String getPrettyName()
        {
            return "TAG_Compound";
        }
    };
    private final Map<String, Tag> tags;

    protected CompoundTag(Map<String, Tag> p_128333_)
    {
        this.tags = p_128333_;
    }

    public CompoundTag()
    {
        this(Maps.newHashMap());
    }

    public void write(DataOutput pOutput) throws IOException
    {
        for (String s : this.tags.keySet())
        {
            Tag tag = this.tags.get(s);
            writeNamedTag(s, tag, pOutput);
        }

        pOutput.writeByte(0);
    }

    public Set<String> getAllKeys()
    {
        return this.tags.keySet();
    }

    public byte getId()
    {
        return 10;
    }

    public TagType<CompoundTag> getType()
    {
        return TYPE;
    }

    public int size()
    {
        return this.tags.size();
    }

    @Nullable
    public Tag put(String pKey, Tag pValue)
    {
        return this.tags.put(pKey, pValue);
    }

    public void putByte(String pKey, byte pValue)
    {
        this.tags.put(pKey, ByteTag.valueOf(pValue));
    }

    public void putShort(String pKey, short pValue)
    {
        this.tags.put(pKey, ShortTag.valueOf(pValue));
    }

    public void putInt(String pKey, int pValue)
    {
        this.tags.put(pKey, IntTag.valueOf(pValue));
    }

    public void putLong(String pKey, long pValue)
    {
        this.tags.put(pKey, LongTag.valueOf(pValue));
    }

    public void putUUID(String pKey, UUID pValue)
    {
        this.tags.put(pKey, NbtUtils.createUUID(pValue));
    }

    public UUID getUUID(String pKey)
    {
        return NbtUtils.loadUUID(this.get(pKey));
    }

    public boolean hasUUID(String pKey)
    {
        Tag tag = this.get(pKey);
        return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
    }

    public void putFloat(String pKey, float pValue)
    {
        this.tags.put(pKey, FloatTag.valueOf(pValue));
    }

    public void putDouble(String pKey, double pValue)
    {
        this.tags.put(pKey, DoubleTag.valueOf(pValue));
    }

    public void putString(String pKey, String pValue)
    {
        this.tags.put(pKey, StringTag.valueOf(pValue));
    }

    public void m_128382_(String p_128383_, byte[] p_128384_)
    {
        this.tags.put(p_128383_, new ByteArrayTag(p_128384_));
    }

    public void putByteArray(String pKey, List<Byte> pValue)
    {
        this.tags.put(pKey, new ByteArrayTag(pValue));
    }

    public void m_128385_(String p_128386_, int[] p_128387_)
    {
        this.tags.put(p_128386_, new IntArrayTag(p_128387_));
    }

    public void putIntArray(String pKey, List<Integer> pValue)
    {
        this.tags.put(pKey, new IntArrayTag(pValue));
    }

    public void m_128388_(String p_128389_, long[] p_128390_)
    {
        this.tags.put(p_128389_, new LongArrayTag(p_128390_));
    }

    public void putLongArray(String pKey, List<Long> pValue)
    {
        this.tags.put(pKey, new LongArrayTag(pValue));
    }

    public void putBoolean(String pKey, boolean pValue)
    {
        this.tags.put(pKey, ByteTag.valueOf(pValue));
    }

    @Nullable
    public Tag get(String pKey)
    {
        return this.tags.get(pKey);
    }

    public byte getTagType(String pKey)
    {
        Tag tag = this.tags.get(pKey);
        return tag == null ? 0 : tag.getId();
    }

    public boolean contains(String pKey)
    {
        return this.tags.containsKey(pKey);
    }

    public boolean contains(String pKey, int p_128427_)
    {
        int i = this.getTagType(pKey);

        if (i == p_128427_)
        {
            return true;
        }
        else if (p_128427_ != 99)
        {
            return false;
        }
        else
        {
            return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
        }
    }

    public byte getByte(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsByte();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0;
    }

    public short getShort(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsShort();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0;
    }

    public int getInt(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsInt();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0;
    }

    public long getLong(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsLong();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0L;
    }

    public float getFloat(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsFloat();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0.0F;
    }

    public double getDouble(String pKey)
    {
        try
        {
            if (this.contains(pKey, 99))
            {
                return ((NumericTag)this.tags.get(pKey)).getAsDouble();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return 0.0D;
    }

    public String getString(String pKey)
    {
        try
        {
            if (this.contains(pKey, 8))
            {
                return this.tags.get(pKey).getAsString();
            }
        }
        catch (ClassCastException classcastexception)
        {
        }

        return "";
    }

    public byte[] getByteArray(String pKey)
    {
        try
        {
            if (this.contains(pKey, 7))
            {
                return ((ByteArrayTag)this.tags.get(pKey)).getAsByteArray();
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createReport(pKey, ByteArrayTag.TYPE, classcastexception));
        }

        return new byte[0];
    }

    public int[] getIntArray(String pKey)
    {
        try
        {
            if (this.contains(pKey, 11))
            {
                return ((IntArrayTag)this.tags.get(pKey)).getAsIntArray();
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createReport(pKey, IntArrayTag.TYPE, classcastexception));
        }

        return new int[0];
    }

    public long[] getLongArray(String pKey)
    {
        try
        {
            if (this.contains(pKey, 12))
            {
                return ((LongArrayTag)this.tags.get(pKey)).getAsLongArray();
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createReport(pKey, LongArrayTag.TYPE, classcastexception));
        }

        return new long[0];
    }

    public CompoundTag getCompound(String pKey)
    {
        try
        {
            if (this.contains(pKey, 10))
            {
                return (CompoundTag)this.tags.get(pKey);
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createReport(pKey, TYPE, classcastexception));
        }

        return new CompoundTag();
    }

    public ListTag getList(String pKey, int pType)
    {
        try
        {
            if (this.getTagType(pKey) == 9)
            {
                ListTag listtag = (ListTag)this.tags.get(pKey);

                if (!listtag.isEmpty() && listtag.getElementType() != pType)
                {
                    return new ListTag();
                }

                return listtag;
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createReport(pKey, ListTag.TYPE, classcastexception));
        }

        return new ListTag();
    }

    public boolean getBoolean(String pKey)
    {
        return this.getByte(pKey) != 0;
    }

    public void remove(String pKey)
    {
        this.tags.remove(pKey);
    }

    public String toString()
    {
        return this.getAsString();
    }

    public boolean isEmpty()
    {
        return this.tags.isEmpty();
    }

    private CrashReport createReport(String pTagName, TagType<?> pType, ClassCastException pException)
    {
        CrashReport crashreport = CrashReport.forThrowable(pException, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
        crashreportcategory.setDetail("Tag type found", () ->
        {
            return this.tags.get(pTagName).getType().getName();
        });
        crashreportcategory.setDetail("Tag type expected", pType::getName);
        crashreportcategory.setDetail("Tag name", pTagName);
        return crashreport;
    }

    public CompoundTag copy()
    {
        Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(map);
    }

    public boolean equals(Object p_128444_)
    {
        if (this == p_128444_)
        {
            return true;
        }
        else
        {
            return p_128444_ instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)p_128444_).tags);
        }
    }

    public int hashCode()
    {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String pName, Tag pTag, DataOutput pOutput) throws IOException
    {
        pOutput.writeByte(pTag.getId());

        if (pTag.getId() != 0)
        {
            pOutput.writeUTF(pName);
            pTag.write(pOutput);
        }
    }

    static byte readNamedTagType(DataInput pInput, NbtAccounter pSizeTracker) throws IOException
    {
        return pInput.readByte();
    }

    static String readNamedTagName(DataInput pInput, NbtAccounter pSizeTracker) throws IOException
    {
        return pInput.readUTF();
    }

    static Tag readNamedTagData(TagType<?> pType, String pName, DataInput pInput, int pDepth, NbtAccounter pAccounter)
    {
        try
        {
            return pType.load(pInput, pDepth, pAccounter);
        }
        catch (IOException ioexception)
        {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
            crashreportcategory.setDetail("Tag name", pName);
            crashreportcategory.setDetail("Tag type", pType.getName());
            throw new ReportedException(crashreport);
        }
    }

    public CompoundTag merge(CompoundTag pOther)
    {
        for (String s : pOther.tags.keySet())
        {
            Tag tag = pOther.tags.get(s);

            if (tag.getId() == 10)
            {
                if (this.contains(s, 10))
                {
                    CompoundTag compoundtag = this.getCompound(s);
                    compoundtag.merge((CompoundTag)tag);
                }
                else
                {
                    this.put(s, tag.copy());
                }
            }
            else
            {
                this.put(s, tag.copy());
            }
        }

        return this;
    }

    public void accept(TagVisitor p_177857_)
    {
        p_177857_.visitCompound(this);
    }

    protected Map<String, Tag> entries()
    {
        return Collections.unmodifiableMap(this.tags);
    }
}
