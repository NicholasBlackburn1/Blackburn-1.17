package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;

public class CheckerboardColumnBiomeSource extends BiomeSource
{
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create((p_48244_) ->
    {
        return p_48244_.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter((p_151790_) -> {
            return p_151790_.allowedBiomes;
        }), Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter((p_151788_) -> {
            return p_151788_.size;
        })).apply(p_48244_, CheckerboardColumnBiomeSource::new);
    });
    private final List<Supplier<Biome>> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(List<Supplier<Biome>> p_48236_, int p_48237_)
    {
        super(p_48236_.stream());
        this.allowedBiomes = p_48236_;
        this.bitShift = p_48237_ + 2;
        this.size = p_48237_;
    }

    protected Codec <? extends BiomeSource > codec()
    {
        return CODEC;
    }

    public BiomeSource withSeed(long pSeed)
    {
        return this;
    }

    public Biome getNoiseBiome(int pX, int pY, int pZ)
    {
        return this.allowedBiomes.get(Math.floorMod((pX >> this.bitShift) + (pZ >> this.bitShift), this.allowedBiomes.size())).get();
    }
}
