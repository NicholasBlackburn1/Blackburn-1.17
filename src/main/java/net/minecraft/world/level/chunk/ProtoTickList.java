package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

public class ProtoTickList<T> implements TickList<T>
{
    protected final Predicate<T> ignore;
    private final ChunkPos chunkPos;
    private final ShortList[] toBeTicked;
    private LevelHeightAccessor levelHeightAccessor;

    public ProtoTickList(Predicate<T> p_156495_, ChunkPos p_156496_, LevelHeightAccessor p_156497_)
    {
        this(p_156495_, p_156496_, new ListTag(), p_156497_);
    }

    public ProtoTickList(Predicate<T> p_156499_, ChunkPos p_156500_, ListTag p_156501_, LevelHeightAccessor p_156502_)
    {
        this.ignore = p_156499_;
        this.chunkPos = p_156500_;
        this.levelHeightAccessor = p_156502_;
        this.toBeTicked = new ShortList[p_156502_.getSectionsCount()];

        for (int i = 0; i < p_156501_.size(); ++i)
        {
            ListTag listtag = p_156501_.getList(i);

            for (int j = 0; j < listtag.size(); ++j)
            {
                ChunkAccess.m_62095_(this.toBeTicked, i).add(listtag.getShort(j));
            }
        }
    }

    public ListTag save()
    {
        return ChunkSerializer.m_63490_(this.toBeTicked);
    }

    public void copyOut(TickList<T> pTickList, Function<BlockPos, T> pFunc)
    {
        for (int i = 0; i < this.toBeTicked.length; ++i)
        {
            if (this.toBeTicked[i] != null)
            {
                for (Short oshort : this.toBeTicked[i])
                {
                    BlockPos blockpos = ProtoChunk.unpackOffsetCoordinates(oshort, this.levelHeightAccessor.getSectionYFromSectionIndex(i), this.chunkPos);
                    pTickList.scheduleTick(blockpos, pFunc.apply(blockpos), 0);
                }

                this.toBeTicked[i].clear();
            }
        }
    }

    public boolean hasScheduledTick(BlockPos pPos, T pItem)
    {
        return false;
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority)
    {
        int i = this.levelHeightAccessor.getSectionIndex(pPos.getY());

        if (i >= 0 && i < this.levelHeightAccessor.getSectionsCount())
        {
            ChunkAccess.m_62095_(this.toBeTicked, i).add(ProtoChunk.packOffsetCoordinates(pPos));
        }
    }

    public boolean willTickThisTick(BlockPos pPos, T pObj)
    {
        return false;
    }

    public int size()
    {
        return Stream.of(this.toBeTicked).filter(Objects::nonNull).mapToInt(List::size).sum();
    }
}
