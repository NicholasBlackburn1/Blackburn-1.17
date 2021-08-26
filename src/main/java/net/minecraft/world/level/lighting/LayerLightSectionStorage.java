package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> extends SectionTracker
{
    protected static final int LIGHT_AND_DATA = 0;
    protected static final int LIGHT_ONLY = 1;
    protected static final int EMPTY = 2;
    protected static final DataLayer EMPTY_DATA = new DataLayer();
    private static final Direction[] DIRECTIONS = Direction.values();
    private final LightLayer layer;
    private final LightChunkGetter chunkSource;
    protected final LongSet dataSectionSet = new LongOpenHashSet();
    protected final LongSet toMarkNoData = new LongOpenHashSet();
    protected final LongSet toMarkData = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final LongSet untrustedSections = new LongOpenHashSet();
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasToRemove;

    protected LayerLightSectionStorage(LightLayer p_75745_, LightChunkGetter p_75746_, M p_75747_)
    {
        super(3, 16, 256);
        this.layer = p_75745_;
        this.chunkSource = p_75746_;
        this.updatingSectionData = p_75747_;
        this.visibleSectionData = p_75747_.copy();
        this.visibleSectionData.disableCache();
    }

    protected boolean storingLightForSection(long pSectionPos)
    {
        return this.getDataLayer(pSectionPos, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(long pSectionPos, boolean p_75760_)
    {
        return this.getDataLayer((M)(p_75760_ ? this.updatingSectionData : this.visibleSectionData), pSectionPos);
    }

    @Nullable
    protected DataLayer getDataLayer(M pSectionPos, long p_75763_)
    {
        return pSectionPos.getLayer(p_75763_);
    }

    @Nullable
    public DataLayer getDataLayerData(long pSectionPos)
    {
        DataLayer datalayer = this.queuedSections.get(pSectionPos);
        return datalayer != null ? datalayer : this.getDataLayer(pSectionPos, false);
    }

    protected abstract int getLightValue(long pLevelPos);

    protected int getStoredLevel(long pLevelPos)
    {
        long i = SectionPos.blockToSection(pLevelPos);
        DataLayer datalayer = this.getDataLayer(i, true);
        return datalayer.get(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)));
    }

    protected void setStoredLevel(long pLevelPos, int p_75774_)
    {
        long i = SectionPos.blockToSection(pLevelPos);

        if (this.changedSections.add(i))
        {
            this.updatingSectionData.copyDataLayer(i);
        }

        DataLayer datalayer = this.getDataLayer(i, true);
        datalayer.set(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)), p_75774_);

        for (int j = -1; j <= 1; ++j)
        {
            for (int k = -1; k <= 1; ++k)
            {
                for (int l = -1; l <= 1; ++l)
                {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(pLevelPos, k, l, j)));
                }
            }
        }
    }

    protected int getLevel(long pSectionPos)
    {
        if (pSectionPos == Long.MAX_VALUE)
        {
            return 2;
        }
        else if (this.dataSectionSet.contains(pSectionPos))
        {
            return 0;
        }
        else
        {
            return !this.toRemove.contains(pSectionPos) && this.updatingSectionData.hasLayer(pSectionPos) ? 1 : 2;
        }
    }

    protected int getLevelFromSource(long pPos)
    {
        if (this.toMarkNoData.contains(pPos))
        {
            return 2;
        }
        else
        {
            return !this.dataSectionSet.contains(pPos) && !this.toMarkData.contains(pPos) ? 2 : 0;
        }
    }

    protected void setLevel(long pSectionPos, int p_75750_)
    {
        int i = this.getLevel(pSectionPos);

        if (i != 0 && p_75750_ == 0)
        {
            this.dataSectionSet.add(pSectionPos);
            this.toMarkData.remove(pSectionPos);
        }

        if (i == 0 && p_75750_ != 0)
        {
            this.dataSectionSet.remove(pSectionPos);
            this.toMarkNoData.remove(pSectionPos);
        }

        if (i >= 2 && p_75750_ != 2)
        {
            if (this.toRemove.contains(pSectionPos))
            {
                this.toRemove.remove(pSectionPos);
            }
            else
            {
                this.updatingSectionData.setLayer(pSectionPos, this.createDataLayer(pSectionPos));
                this.changedSections.add(pSectionPos);
                this.onNodeAdded(pSectionPos);

                for (int j = -1; j <= 1; ++j)
                {
                    for (int k = -1; k <= 1; ++k)
                    {
                        for (int l = -1; l <= 1; ++l)
                        {
                            this.sectionsAffectedByLightUpdates.add(SectionPos.blockToSection(BlockPos.offset(pSectionPos, k, l, j)));
                        }
                    }
                }
            }
        }

        if (i != 2 && p_75750_ >= 2)
        {
            this.toRemove.add(pSectionPos);
        }

        this.hasToRemove = !this.toRemove.isEmpty();
    }

    protected DataLayer createDataLayer(long pSectionPos)
    {
        DataLayer datalayer = this.queuedSections.get(pSectionPos);
        return datalayer != null ? datalayer : new DataLayer();
    }

    protected void clearQueuedSectionBlocks(LayerLightEngine <? , ? > pEngine, long pSectionPos)
    {
        if (pEngine.getQueueSize() < 8192)
        {
            pEngine.removeIf((p_75753_) ->
            {
                return SectionPos.blockToSection(p_75753_) == pSectionPos;
            });
        }
        else
        {
            int i = SectionPos.sectionToBlockCoord(SectionPos.x(pSectionPos));
            int j = SectionPos.sectionToBlockCoord(SectionPos.y(pSectionPos));
            int k = SectionPos.sectionToBlockCoord(SectionPos.z(pSectionPos));

            for (int l = 0; l < 16; ++l)
            {
                for (int i1 = 0; i1 < 16; ++i1)
                {
                    for (int j1 = 0; j1 < 16; ++j1)
                    {
                        long k1 = BlockPos.asLong(i + l, j + i1, k + j1);
                        pEngine.removeFromQueue(k1);
                    }
                }
            }
        }
    }

    protected boolean hasInconsistencies()
    {
        return this.hasToRemove;
    }

    protected void markNewInconsistencies(LayerLightEngine < M, ? > pEngine, boolean pUpdateSkyLight, boolean pUpdateBlockLight)
    {
        if (this.hasInconsistencies() || !this.queuedSections.isEmpty())
        {
            for (long i : this.toRemove)
            {
                this.clearQueuedSectionBlocks(pEngine, i);
                DataLayer datalayer = this.queuedSections.remove(i);
                DataLayer datalayer1 = this.updatingSectionData.removeLayer(i);

                if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(i)))
                {
                    if (datalayer != null)
                    {
                        this.queuedSections.put(i, datalayer);
                    }
                    else if (datalayer1 != null)
                    {
                        this.queuedSections.put(i, datalayer1);
                    }
                }
            }

            this.updatingSectionData.clearCache();

            for (long k : this.toRemove)
            {
                this.onNodeRemoved(k);
            }

            this.toRemove.clear();
            this.hasToRemove = false;

            for (Entry<DataLayer> entry : this.queuedSections.long2ObjectEntrySet())
            {
                long j = entry.getLongKey();

                if (this.storingLightForSection(j))
                {
                    DataLayer datalayer2 = entry.getValue();

                    if (this.updatingSectionData.getLayer(j) != datalayer2)
                    {
                        this.clearQueuedSectionBlocks(pEngine, j);
                        this.updatingSectionData.setLayer(j, datalayer2);
                        this.changedSections.add(j);
                    }
                }
            }

            this.updatingSectionData.clearCache();

            if (!pUpdateBlockLight)
            {
                for (long l : this.queuedSections.keySet())
                {
                    this.checkEdgesForSection(pEngine, l);
                }
            }
            else
            {
                for (long i1 : this.untrustedSections)
                {
                    this.checkEdgesForSection(pEngine, i1);
                }
            }

            this.untrustedSections.clear();
            ObjectIterator<Entry<DataLayer>> objectiterator = this.queuedSections.long2ObjectEntrySet().iterator();

            while (objectiterator.hasNext())
            {
                Entry<DataLayer> entry1 = objectiterator.next();
                long j1 = entry1.getLongKey();

                if (this.storingLightForSection(j1))
                {
                    objectiterator.remove();
                }
            }
        }
    }

    private void checkEdgesForSection(LayerLightEngine < M, ? > p_75778_, long p_75779_)
    {
        if (this.storingLightForSection(p_75779_))
        {
            int i = SectionPos.sectionToBlockCoord(SectionPos.x(p_75779_));
            int j = SectionPos.sectionToBlockCoord(SectionPos.y(p_75779_));
            int k = SectionPos.sectionToBlockCoord(SectionPos.z(p_75779_));

            for (Direction direction : DIRECTIONS)
            {
                long l = SectionPos.offset(p_75779_, direction);

                if (!this.queuedSections.containsKey(l) && this.storingLightForSection(l))
                {
                    for (int i1 = 0; i1 < 16; ++i1)
                    {
                        for (int j1 = 0; j1 < 16; ++j1)
                        {
                            long k1;
                            long l1;

                            switch (direction)
                            {
                                case DOWN:
                                    k1 = BlockPos.asLong(i + j1, j, k + i1);
                                    l1 = BlockPos.asLong(i + j1, j - 1, k + i1);
                                    break;

                                case UP:
                                    k1 = BlockPos.asLong(i + j1, j + 16 - 1, k + i1);
                                    l1 = BlockPos.asLong(i + j1, j + 16, k + i1);
                                    break;

                                case NORTH:
                                    k1 = BlockPos.asLong(i + i1, j + j1, k);
                                    l1 = BlockPos.asLong(i + i1, j + j1, k - 1);
                                    break;

                                case SOUTH:
                                    k1 = BlockPos.asLong(i + i1, j + j1, k + 16 - 1);
                                    l1 = BlockPos.asLong(i + i1, j + j1, k + 16);
                                    break;

                                case WEST:
                                    k1 = BlockPos.asLong(i, j + i1, k + j1);
                                    l1 = BlockPos.asLong(i - 1, j + i1, k + j1);
                                    break;

                                default:
                                    k1 = BlockPos.asLong(i + 16 - 1, j + i1, k + j1);
                                    l1 = BlockPos.asLong(i + 16, j + i1, k + j1);
                            }

                            p_75778_.checkEdge(k1, l1, p_75778_.computeLevelFromNeighbor(k1, l1, p_75778_.getLevel(k1)), false);
                            p_75778_.checkEdge(l1, k1, p_75778_.computeLevelFromNeighbor(l1, k1, p_75778_.getLevel(l1)), false);
                        }
                    }
                }
            }
        }
    }

    protected void onNodeAdded(long pSectionPos)
    {
    }

    protected void onNodeRemoved(long p_75799_)
    {
    }

    protected void enableLightSources(long p_75775_, boolean p_75776_)
    {
    }

    public void retainData(long pSectionColumnPos, boolean p_75784_)
    {
        if (p_75784_)
        {
            this.columnsToRetainQueuedDataFor.add(pSectionColumnPos);
        }
        else
        {
            this.columnsToRetainQueuedDataFor.remove(pSectionColumnPos);
        }
    }

    protected void queueSectionData(long pSectionPos, @Nullable DataLayer p_75756_, boolean pArray)
    {
        if (p_75756_ != null)
        {
            this.queuedSections.put(pSectionPos, p_75756_);

            if (!pArray)
            {
                this.untrustedSections.add(pSectionPos);
            }
        }
        else
        {
            this.queuedSections.remove(pSectionPos);
        }
    }

    protected void updateSectionStatus(long pSectionPos, boolean p_75789_)
    {
        boolean flag = this.dataSectionSet.contains(pSectionPos);

        if (!flag && !p_75789_)
        {
            this.toMarkData.add(pSectionPos);
            this.checkEdge(Long.MAX_VALUE, pSectionPos, 0, true);
        }

        if (flag && p_75789_)
        {
            this.toMarkNoData.add(pSectionPos);
            this.checkEdge(Long.MAX_VALUE, pSectionPos, 2, false);
        }
    }

    protected void runAllUpdates()
    {
        if (this.hasWork())
        {
            this.runUpdates(Integer.MAX_VALUE);
        }
    }

    protected void swapSectionMap()
    {
        if (!this.changedSections.isEmpty())
        {
            M m = this.updatingSectionData.copy();
            m.disableCache();
            this.visibleSectionData = m;
            this.changedSections.clear();
        }

        if (!this.sectionsAffectedByLightUpdates.isEmpty())
        {
            LongIterator longiterator = this.sectionsAffectedByLightUpdates.iterator();

            while (longiterator.hasNext())
            {
                long i = longiterator.nextLong();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(i));
            }

            this.sectionsAffectedByLightUpdates.clear();
        }
    }
}
