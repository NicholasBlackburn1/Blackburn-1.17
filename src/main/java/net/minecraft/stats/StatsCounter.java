package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;

public class StatsCounter
{
    protected final Object2IntMap < Stat<? >> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

    public StatsCounter()
    {
        this.stats.defaultReturnValue(0);
    }

    public void increment(Player pPlayer, Stat<?> pStat, int pAmount)
    {
        int i = (int)Math.min((long)this.getValue(pStat) + (long)pAmount, 2147483647L);
        this.setValue(pPlayer, pStat, i);
    }

    public void setValue(Player pPlayer, Stat<?> pStat, int p_13022_)
    {
        this.stats.put(pStat, p_13022_);
    }

    public <T> int getValue(StatType<T> pStat, T p_13019_)
    {
        return pStat.contains(p_13019_) ? this.getValue(pStat.get(p_13019_)) : 0;
    }

    public int getValue(Stat<?> pStat)
    {
        return this.stats.getInt(pStat);
    }
}
