package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData
{
    private static final String TAG_REMAINING_INDEXES = "Remaining";
    private static final String TAG_All_INDEXES = "All";
    private final LongSet all;
    private final LongSet remaining;

    private StructureFeatureIndexSavedData(LongSet p_163532_, LongSet p_163533_)
    {
        this.all = p_163532_;
        this.remaining = p_163533_;
    }

    public StructureFeatureIndexSavedData()
    {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public static StructureFeatureIndexSavedData load(CompoundTag p_163535_)
    {
        return new StructureFeatureIndexSavedData(new LongOpenHashSet(p_163535_.getLongArray("All")), new LongOpenHashSet(p_163535_.getLongArray("Remaining")));
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        pCompound.m_128388_("All", this.all.toLongArray());
        pCompound.m_128388_("Remaining", this.remaining.toLongArray());
        return pCompound;
    }

    public void addIndex(long pChunkPos)
    {
        this.all.add(pChunkPos);
        this.remaining.add(pChunkPos);
    }

    public boolean hasStartIndex(long pChunkPos)
    {
        return this.all.contains(pChunkPos);
    }

    public boolean hasUnhandledIndex(long pChunkPos)
    {
        return this.remaining.contains(pChunkPos);
    }

    public void removeIndex(long pChunkPos)
    {
        this.remaining.remove(pChunkPos);
    }

    public LongSet getAll()
    {
        return this.all;
    }
}
