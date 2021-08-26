package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ServerTickList<T> implements TickList<T>
{
    public static final int MAX_TICK_BLOCKS_PER_TICK = 65536;
    protected final Predicate<T> ignore;
    private final Function<T, ResourceLocation> toId;
    private final Set<TickNextTickData<T>> tickNextTickSet = Sets.newHashSet();
    private final Set<TickNextTickData<T>> tickNextTickList = Sets.newTreeSet(TickNextTickData.createTimeComparator());
    private final ServerLevel level;
    private final Queue<TickNextTickData<T>> currentlyTicking = Queues.newArrayDeque();
    private final List<TickNextTickData<T>> alreadyTicked = Lists.newArrayList();
    private final Consumer<TickNextTickData<T>> ticker;

    public ServerTickList(ServerLevel p_47216_, Predicate<T> p_47217_, Function<T, ResourceLocation> p_47218_, Consumer<TickNextTickData<T>> p_47219_)
    {
        this.ignore = p_47217_;
        this.toId = p_47218_;
        this.level = p_47216_;
        this.ticker = p_47219_;
    }

    public void tick()
    {
        int i = this.tickNextTickList.size();

        if (i != this.tickNextTickSet.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        else
        {
            if (i > 65536)
            {
                i = 65536;
            }

            Iterator<TickNextTickData<T>> iterator = this.tickNextTickList.iterator();
            this.level.getProfiler().push("cleaning");

            while (i > 0 && iterator.hasNext())
            {
                TickNextTickData<T> ticknexttickdata = iterator.next();

                if (ticknexttickdata.triggerTick > this.level.getGameTime())
                {
                    break;
                }

                if (this.level.isPositionTickingWithEntitiesLoaded(ticknexttickdata.pos))
                {
                    iterator.remove();
                    this.tickNextTickSet.remove(ticknexttickdata);
                    this.currentlyTicking.add(ticknexttickdata);
                    --i;
                }
            }

            this.level.getProfiler().popPush("ticking");
            TickNextTickData<T> ticknexttickdata1;

            while ((ticknexttickdata1 = this.currentlyTicking.poll()) != null)
            {
                if (this.level.isPositionTickingWithEntitiesLoaded(ticknexttickdata1.pos))
                {
                    try
                    {
                        this.alreadyTicked.add(ticknexttickdata1);
                        this.ticker.accept(ticknexttickdata1);
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while ticking");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Block being ticked");
                        CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, ticknexttickdata1.pos, (BlockState)null);
                        throw new ReportedException(crashreport);
                    }
                }
                else
                {
                    this.scheduleTick(ticknexttickdata1.pos, ticknexttickdata1.getType(), 0);
                }
            }

            this.level.getProfiler().pop();
            this.alreadyTicked.clear();
            this.currentlyTicking.clear();
        }
    }

    public boolean willTickThisTick(BlockPos pPos, T pObj)
    {
        return this.currentlyTicking.contains(new TickNextTickData(pPos, pObj));
    }

    public List<TickNextTickData<T>> fetchTicksInChunk(ChunkPos pPos, boolean pRemove, boolean pSkipCompleted)
    {
        int i = pPos.getMinBlockX() - 2;
        int j = i + 16 + 2;
        int k = pPos.getMinBlockZ() - 2;
        int l = k + 16 + 2;
        return this.fetchTicksInArea(new BoundingBox(i, this.level.getMinBuildHeight(), k, j, this.level.getMaxBuildHeight(), l), pRemove, pSkipCompleted);
    }

    public List<TickNextTickData<T>> fetchTicksInArea(BoundingBox pResult, boolean pEntries, boolean pBb)
    {
        List<TickNextTickData<T>> list = this.fetchTicksInArea((List<TickNextTickData<T>>)null, this.tickNextTickList, pResult, pEntries);

        if (pEntries && list != null)
        {
            this.tickNextTickSet.removeAll(list);
        }

        list = this.fetchTicksInArea(list, this.currentlyTicking, pResult, pEntries);

        if (!pBb)
        {
            list = this.fetchTicksInArea(list, this.alreadyTicked, pResult, pEntries);
        }

        return list == null ? Collections.emptyList() : list;
    }

    @Nullable
    private List<TickNextTickData<T>> fetchTicksInArea(@Nullable List<TickNextTickData<T>> pResult, Collection<TickNextTickData<T>> pEntries, BoundingBox pBb, boolean pRemove)
    {
        Iterator<TickNextTickData<T>> iterator = pEntries.iterator();

        while (iterator.hasNext())
        {
            TickNextTickData<T> ticknexttickdata = iterator.next();
            BlockPos blockpos = ticknexttickdata.pos;

            if (blockpos.getX() >= pBb.minX() && blockpos.getX() < pBb.maxX() && blockpos.getZ() >= pBb.minZ() && blockpos.getZ() < pBb.maxZ())
            {
                if (pRemove)
                {
                    iterator.remove();
                }

                if (pResult == null)
                {
                    pResult = Lists.newArrayList();
                }

                pResult.add(ticknexttickdata);
            }
        }

        return pResult;
    }

    public void copy(BoundingBox pArea, BlockPos pOffset)
    {
        for (TickNextTickData<T> ticknexttickdata : this.fetchTicksInArea(pArea, false, false))
        {
            if (pArea.isInside(ticknexttickdata.pos))
            {
                BlockPos blockpos = ticknexttickdata.pos.offset(pOffset);
                T t = ticknexttickdata.getType();
                this.addTickData(new TickNextTickData<>(blockpos, t, ticknexttickdata.triggerTick, ticknexttickdata.priority));
            }
        }
    }

    public ListTag save(ChunkPos pPos)
    {
        List<TickNextTickData<T>> list = this.fetchTicksInChunk(pPos, false, true);
        return saveTickList(this.toId, list, this.level.getGameTime());
    }

    private static <T> ListTag saveTickList(Function<T, ResourceLocation> pTargetNameFunction, Iterable<TickNextTickData<T>> pTickEntries, long pTime)
    {
        ListTag listtag = new ListTag();

        for (TickNextTickData<T> ticknexttickdata : pTickEntries)
        {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("i", pTargetNameFunction.apply(ticknexttickdata.getType()).toString());
            compoundtag.putInt("x", ticknexttickdata.pos.getX());
            compoundtag.putInt("y", ticknexttickdata.pos.getY());
            compoundtag.putInt("z", ticknexttickdata.pos.getZ());
            compoundtag.putInt("t", (int)(ticknexttickdata.triggerTick - pTime));
            compoundtag.putInt("p", ticknexttickdata.priority.getValue());
            listtag.add(compoundtag);
        }

        return listtag;
    }

    public boolean hasScheduledTick(BlockPos pPos, T pItem)
    {
        return this.tickNextTickSet.contains(new TickNextTickData(pPos, pItem));
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority)
    {
        if (!this.ignore.test(pItem))
        {
            this.addTickData(new TickNextTickData<>(pPos, pItem, (long)pScheduledTime + this.level.getGameTime(), pPriority));
        }
    }

    private void addTickData(TickNextTickData<T> pEntry)
    {
        if (!this.tickNextTickSet.contains(pEntry))
        {
            this.tickNextTickSet.add(pEntry);
            this.tickNextTickList.add(pEntry);
        }
    }

    public int size()
    {
        return this.tickNextTickSet.size();
    }
}
