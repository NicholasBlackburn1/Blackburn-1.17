package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.function.LongFunction;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.LazyAreaContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public class Layers implements LayerBiomes
{
    protected static final int WARM_ID = 1;
    protected static final int MEDIUM_ID = 2;
    protected static final int COLD_ID = 3;
    protected static final int ICE_ID = 4;
    protected static final int SPECIAL_MASK = 3840;
    protected static final int SPECIAL_SHIFT = 8;
    private static final Int2IntMap CATEGORIES = Util.make(new Int2IntOpenHashMap(), (p_76741_) ->
    {
        register(p_76741_, Layers.Category.BEACH, 16);
        register(p_76741_, Layers.Category.BEACH, 26);
        register(p_76741_, Layers.Category.DESERT, 2);
        register(p_76741_, Layers.Category.DESERT, 17);
        register(p_76741_, Layers.Category.DESERT, 130);
        register(p_76741_, Layers.Category.EXTREME_HILLS, 131);
        register(p_76741_, Layers.Category.EXTREME_HILLS, 162);
        register(p_76741_, Layers.Category.EXTREME_HILLS, 20);
        register(p_76741_, Layers.Category.EXTREME_HILLS, 3);
        register(p_76741_, Layers.Category.EXTREME_HILLS, 34);
        register(p_76741_, Layers.Category.FOREST, 27);
        register(p_76741_, Layers.Category.FOREST, 28);
        register(p_76741_, Layers.Category.FOREST, 29);
        register(p_76741_, Layers.Category.FOREST, 157);
        register(p_76741_, Layers.Category.FOREST, 132);
        register(p_76741_, Layers.Category.FOREST, 4);
        register(p_76741_, Layers.Category.FOREST, 155);
        register(p_76741_, Layers.Category.FOREST, 156);
        register(p_76741_, Layers.Category.FOREST, 18);
        register(p_76741_, Layers.Category.ICY, 140);
        register(p_76741_, Layers.Category.ICY, 13);
        register(p_76741_, Layers.Category.ICY, 12);
        register(p_76741_, Layers.Category.JUNGLE, 168);
        register(p_76741_, Layers.Category.JUNGLE, 169);
        register(p_76741_, Layers.Category.JUNGLE, 21);
        register(p_76741_, Layers.Category.JUNGLE, 23);
        register(p_76741_, Layers.Category.JUNGLE, 22);
        register(p_76741_, Layers.Category.JUNGLE, 149);
        register(p_76741_, Layers.Category.JUNGLE, 151);
        register(p_76741_, Layers.Category.MESA, 37);
        register(p_76741_, Layers.Category.MESA, 165);
        register(p_76741_, Layers.Category.MESA, 167);
        register(p_76741_, Layers.Category.MESA, 166);
        register(p_76741_, Layers.Category.BADLANDS_PLATEAU, 39);
        register(p_76741_, Layers.Category.BADLANDS_PLATEAU, 38);
        register(p_76741_, Layers.Category.MUSHROOM, 14);
        register(p_76741_, Layers.Category.MUSHROOM, 15);
        register(p_76741_, Layers.Category.NONE, 25);
        register(p_76741_, Layers.Category.OCEAN, 46);
        register(p_76741_, Layers.Category.OCEAN, 49);
        register(p_76741_, Layers.Category.OCEAN, 50);
        register(p_76741_, Layers.Category.OCEAN, 48);
        register(p_76741_, Layers.Category.OCEAN, 24);
        register(p_76741_, Layers.Category.OCEAN, 47);
        register(p_76741_, Layers.Category.OCEAN, 10);
        register(p_76741_, Layers.Category.OCEAN, 45);
        register(p_76741_, Layers.Category.OCEAN, 0);
        register(p_76741_, Layers.Category.OCEAN, 44);
        register(p_76741_, Layers.Category.PLAINS, 1);
        register(p_76741_, Layers.Category.PLAINS, 129);
        register(p_76741_, Layers.Category.RIVER, 11);
        register(p_76741_, Layers.Category.RIVER, 7);
        register(p_76741_, Layers.Category.SAVANNA, 35);
        register(p_76741_, Layers.Category.SAVANNA, 36);
        register(p_76741_, Layers.Category.SAVANNA, 163);
        register(p_76741_, Layers.Category.SAVANNA, 164);
        register(p_76741_, Layers.Category.SWAMP, 6);
        register(p_76741_, Layers.Category.SWAMP, 134);
        register(p_76741_, Layers.Category.TAIGA, 160);
        register(p_76741_, Layers.Category.TAIGA, 161);
        register(p_76741_, Layers.Category.TAIGA, 32);
        register(p_76741_, Layers.Category.TAIGA, 33);
        register(p_76741_, Layers.Category.TAIGA, 30);
        register(p_76741_, Layers.Category.TAIGA, 31);
        register(p_76741_, Layers.Category.TAIGA, 158);
        register(p_76741_, Layers.Category.TAIGA, 5);
        register(p_76741_, Layers.Category.TAIGA, 19);
        register(p_76741_, Layers.Category.TAIGA, 133);
    });

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> zoom(long pSeed, AreaTransformer1 p_76731_, AreaFactory<T> pParent, int p_76733_, LongFunction<C> pCount)
    {
        AreaFactory<T> areafactory = pParent;

        for (int i = 0; i < p_76733_; ++i)
        {
            areafactory = p_76731_.run(pCount.apply(pSeed + (long)i), areafactory);
        }

        return areafactory;
    }

    private static <T extends Area, C extends BigContext<T>> AreaFactory<T> getDefaultLayer(boolean p_76747_, int p_76748_, int p_76749_, LongFunction<C> p_76750_)
    {
        AreaFactory<T> areafactory = IslandLayer.INSTANCE.run(p_76750_.apply(1L));
        areafactory = ZoomLayer.FUZZY.run(p_76750_.apply(2000L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(1L), areafactory);
        areafactory = ZoomLayer.NORMAL.run(p_76750_.apply(2001L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(2L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(50L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(70L), areafactory);
        areafactory = RemoveTooMuchOceanLayer.INSTANCE.run(p_76750_.apply(2L), areafactory);
        AreaFactory<T> areafactory1 = OceanLayer.INSTANCE.run(p_76750_.apply(2L));
        areafactory1 = zoom(2001L, ZoomLayer.NORMAL, areafactory1, 6, p_76750_);
        areafactory = AddSnowLayer.INSTANCE.run(p_76750_.apply(2L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(3L), areafactory);
        areafactory = AddEdgeLayer.CoolWarm.INSTANCE.run(p_76750_.apply(2L), areafactory);
        areafactory = AddEdgeLayer.HeatIce.INSTANCE.run(p_76750_.apply(2L), areafactory);
        areafactory = AddEdgeLayer.IntroduceSpecial.INSTANCE.run(p_76750_.apply(3L), areafactory);
        areafactory = ZoomLayer.NORMAL.run(p_76750_.apply(2002L), areafactory);
        areafactory = ZoomLayer.NORMAL.run(p_76750_.apply(2003L), areafactory);
        areafactory = AddIslandLayer.INSTANCE.run(p_76750_.apply(4L), areafactory);
        areafactory = AddMushroomIslandLayer.INSTANCE.run(p_76750_.apply(5L), areafactory);
        areafactory = AddDeepOceanLayer.INSTANCE.run(p_76750_.apply(4L), areafactory);
        areafactory = zoom(1000L, ZoomLayer.NORMAL, areafactory, 0, p_76750_);
        AreaFactory<T> areafactory2 = zoom(1000L, ZoomLayer.NORMAL, areafactory, 0, p_76750_);
        areafactory2 = RiverInitLayer.INSTANCE.run(p_76750_.apply(100L), areafactory2);
        AreaFactory<T> areafactory3 = (new BiomeInitLayer(p_76747_)).run(p_76750_.apply(200L), areafactory);
        areafactory3 = RareBiomeLargeLayer.INSTANCE.run(p_76750_.apply(1001L), areafactory3);
        areafactory3 = zoom(1000L, ZoomLayer.NORMAL, areafactory3, 2, p_76750_);
        areafactory3 = BiomeEdgeLayer.INSTANCE.run(p_76750_.apply(1000L), areafactory3);
        AreaFactory<T> areafactory4 = zoom(1000L, ZoomLayer.NORMAL, areafactory2, 2, p_76750_);
        areafactory3 = RegionHillsLayer.INSTANCE.run(p_76750_.apply(1000L), areafactory3, areafactory4);
        areafactory2 = zoom(1000L, ZoomLayer.NORMAL, areafactory2, 2, p_76750_);
        areafactory2 = zoom(1000L, ZoomLayer.NORMAL, areafactory2, p_76749_, p_76750_);
        areafactory2 = RiverLayer.INSTANCE.run(p_76750_.apply(1L), areafactory2);
        areafactory2 = SmoothLayer.INSTANCE.run(p_76750_.apply(1000L), areafactory2);
        areafactory3 = RareBiomeSpotLayer.INSTANCE.run(p_76750_.apply(1001L), areafactory3);

        for (int i = 0; i < p_76748_; ++i)
        {
            areafactory3 = ZoomLayer.NORMAL.run(p_76750_.apply((long)(1000 + i)), areafactory3);

            if (i == 0)
            {
                areafactory3 = AddIslandLayer.INSTANCE.run(p_76750_.apply(3L), areafactory3);
            }

            if (i == 1 || p_76748_ == 1)
            {
                areafactory3 = ShoreLayer.INSTANCE.run(p_76750_.apply(1000L), areafactory3);
            }
        }

        areafactory3 = SmoothLayer.INSTANCE.run(p_76750_.apply(1000L), areafactory3);
        areafactory3 = RiverMixerLayer.INSTANCE.run(p_76750_.apply(100L), areafactory3, areafactory2);
        return OceanMixerLayer.INSTANCE.run(p_76750_.apply(100L), areafactory3, areafactory1);
    }

    public static Layer getDefaultLayer(long p_76736_, boolean p_76737_, int p_76738_, int p_76739_)
    {
        int i = 25;
        AreaFactory<LazyArea> areafactory = getDefaultLayer(p_76737_, p_76738_, p_76739_, (p_76728_) ->
        {
            return new LazyAreaContext(25, p_76736_, p_76728_);
        });
        return new Layer(areafactory);
    }

    public static boolean isSame(int p_76724_, int p_76725_)
    {
        if (p_76724_ == p_76725_)
        {
            return true;
        }
        else
        {
            return CATEGORIES.get(p_76724_) == CATEGORIES.get(p_76725_);
        }
    }

    private static void register(Int2IntOpenHashMap p_76743_, Layers.Category p_76744_, int p_76745_)
    {
        p_76743_.put(p_76745_, p_76744_.ordinal());
    }

    protected static boolean isOcean(int pBiome)
    {
        return pBiome == 44 || pBiome == 45 || pBiome == 0 || pBiome == 46 || pBiome == 10 || pBiome == 47 || pBiome == 48 || pBiome == 24 || pBiome == 49 || pBiome == 50;
    }

    protected static boolean isShallowOcean(int pBiome)
    {
        return pBiome == 44 || pBiome == 45 || pBiome == 0 || pBiome == 46 || pBiome == 10;
    }

    static enum Category
    {
        NONE,
        TAIGA,
        EXTREME_HILLS,
        JUNGLE,
        MESA,
        BADLANDS_PLATEAU,
        PLAINS,
        SAVANNA,
        ICY,
        BEACH,
        FOREST,
        OCEAN,
        DESERT,
        RIVER,
        SWAMP,
        MUSHROOM;
    }
}
