package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public class ChunkTickList<T> implements TickList<T>
{
    private final List<ChunkTickList.ScheduledTick<T>> ticks;
    private final Function<T, ResourceLocation> toId;

    public ChunkTickList(Function<T, ResourceLocation> p_45637_, List<TickNextTickData<T>> p_45638_, long p_45639_)
    {
        this(p_45637_, p_45638_.stream().map((p_45642_) ->
        {
            return new ChunkTickList.ScheduledTick<>(p_45642_.getType(), p_45642_.pos, (int)(p_45642_.triggerTick - p_45639_), p_45642_.priority);
        }).collect(Collectors.toList()));
    }

    private ChunkTickList(Function<T, ResourceLocation> p_45634_, List<ChunkTickList.ScheduledTick<T>> p_45635_)
    {
        this.ticks = p_45635_;
        this.toId = p_45634_;
    }

    public boolean hasScheduledTick(BlockPos pPos, T pItem)
    {
        return false;
    }

    public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority)
    {
        this.ticks.add(new ChunkTickList.ScheduledTick<>(pItem, pPos, pScheduledTime, pPriority));
    }

    public boolean willTickThisTick(BlockPos pPos, T pObj)
    {
        return false;
    }

    public ListTag save()
    {
        ListTag listtag = new ListTag();

        for (ChunkTickList.ScheduledTick<T> scheduledtick : this.ticks)
        {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("i", this.toId.apply(scheduledtick.type).toString());
            compoundtag.putInt("x", scheduledtick.pos.getX());
            compoundtag.putInt("y", scheduledtick.pos.getY());
            compoundtag.putInt("z", scheduledtick.pos.getZ());
            compoundtag.putInt("t", scheduledtick.delay);
            compoundtag.putInt("p", scheduledtick.priority.getValue());
            listtag.add(compoundtag);
        }

        return listtag;
    }

    public static <T> ChunkTickList<T> create(ListTag p_45657_, Function<T, ResourceLocation> p_45658_, Function<ResourceLocation, T> p_45659_)
    {
        List<ChunkTickList.ScheduledTick<T>> list = Lists.newArrayList();

        for (int i = 0; i < p_45657_.size(); ++i)
        {
            CompoundTag compoundtag = p_45657_.getCompound(i);
            T t = p_45659_.apply(new ResourceLocation(compoundtag.getString("i")));

            if (t != null)
            {
                BlockPos blockpos = new BlockPos(compoundtag.getInt("x"), compoundtag.getInt("y"), compoundtag.getInt("z"));
                list.add(new ChunkTickList.ScheduledTick<>(t, blockpos, compoundtag.getInt("t"), TickPriority.byValue(compoundtag.getInt("p"))));
            }
        }

        return new ChunkTickList<>(p_45658_, list);
    }

    public void copyOut(TickList<T> p_45644_)
    {
        this.ticks.forEach((p_45647_) ->
        {
            p_45644_.scheduleTick(p_45647_.pos, p_45647_.type, p_45647_.delay, p_45647_.priority);
        });
    }

    public int size()
    {
        return this.ticks.size();
    }

    static class ScheduledTick<T>
    {
        final T type;
        public final BlockPos pos;
        public final int delay;
        public final TickPriority priority;

        ScheduledTick(T p_45669_, BlockPos p_45670_, int p_45671_, TickPriority p_45672_)
        {
            this.type = p_45669_;
            this.pos = p_45670_;
            this.delay = p_45671_;
            this.priority = p_45672_;
        }

        public String toString()
        {
            return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority;
        }
    }
}
