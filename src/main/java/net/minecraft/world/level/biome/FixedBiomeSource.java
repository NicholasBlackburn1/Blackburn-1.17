package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class FixedBiomeSource extends BiomeSource
{
    public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, (p_48278_) ->
    {
        return p_48278_.biome;
    }).stable().codec();
    private final Supplier<Biome> biome;

    public FixedBiomeSource(Biome p_48255_)
    {
        this(() ->
        {
            return p_48255_;
        });
    }

    public FixedBiomeSource(Supplier<Biome> p_48257_)
    {
        super(ImmutableList.of(p_48257_.get()));
        this.biome = p_48257_;
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
        return this.biome.get();
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, int pIncrement, Predicate<Biome> pBiomes, Random pRand, boolean pFindClosest)
    {
        if (pBiomes.test(this.biome.get()))
        {
            return pFindClosest ? new BlockPos(pX, pY, pZ) : new BlockPos(pX - pRadius + pRand.nextInt(pRadius * 2 + 1), pY, pZ - pRadius + pRand.nextInt(pRadius * 2 + 1));
        }
        else
        {
            return null;
        }
    }

    public Set<Biome> getBiomesWithin(int pX, int pY, int pZ, int pRadius)
    {
        return Sets.newHashSet(this.biome.get());
    }
}
