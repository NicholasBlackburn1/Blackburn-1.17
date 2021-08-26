package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public abstract class BiomeSource implements BiomeManager.NoiseBiomeSource
{
    public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
    protected final Map < StructureFeature<?>, Boolean > supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();
    protected final List<Biome> possibleBiomes;

    protected BiomeSource(Stream<Supplier<Biome>> p_47896_)
    {
        this(p_47896_.map(Supplier::get).collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> p_47894_)
    {
        this.possibleBiomes = p_47894_;
    }

    protected abstract Codec <? extends BiomeSource > codec();

    public abstract BiomeSource withSeed(long pSeed);

    public List<Biome> possibleBiomes()
    {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int pX, int pY, int pZ, int pRadius)
    {
        int i = QuartPos.fromBlock(pX - pRadius);
        int j = QuartPos.fromBlock(pY - pRadius);
        int k = QuartPos.fromBlock(pZ - pRadius);
        int l = QuartPos.fromBlock(pX + pRadius);
        int i1 = QuartPos.fromBlock(pY + pRadius);
        int j1 = QuartPos.fromBlock(pZ + pRadius);
        int k1 = l - i + 1;
        int l1 = i1 - j + 1;
        int i2 = j1 - k + 1;
        Set<Biome> set = Sets.newHashSet();

        for (int j2 = 0; j2 < i2; ++j2)
        {
            for (int k2 = 0; k2 < k1; ++k2)
            {
                for (int l2 = 0; l2 < l1; ++l2)
                {
                    int i3 = i + k2;
                    int j3 = j + l2;
                    int k3 = k + j2;
                    set.add(this.getNoiseBiome(i3, j3, k3));
                }
            }
        }

        return set;
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, Predicate<Biome> pIncrement, Random pBiomes)
    {
        return this.findBiomeHorizontal(pX, pY, pZ, pRadius, 1, pIncrement, pBiomes, false);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, int pIncrement, Predicate<Biome> pBiomes, Random pRand, boolean pFindClosest)
    {
        int i = QuartPos.fromBlock(pX);
        int j = QuartPos.fromBlock(pZ);
        int k = QuartPos.fromBlock(pRadius);
        int l = QuartPos.fromBlock(pY);
        BlockPos blockpos = null;
        int i1 = 0;
        int j1 = pFindClosest ? 0 : k;

        for (int k1 = j1; k1 <= k; k1 += pIncrement)
        {
            for (int l1 = -k1; l1 <= k1; l1 += pIncrement)
            {
                boolean flag = Math.abs(l1) == k1;

                for (int i2 = -k1; i2 <= k1; i2 += pIncrement)
                {
                    if (pFindClosest)
                    {
                        boolean flag1 = Math.abs(i2) == k1;

                        if (!flag1 && !flag)
                        {
                            continue;
                        }
                    }

                    int k2 = i + i2;
                    int j2 = j + l1;

                    if (pBiomes.test(this.getNoiseBiome(k2, l, j2)))
                    {
                        if (blockpos == null || pRand.nextInt(i1 + 1) == 0)
                        {
                            blockpos = new BlockPos(QuartPos.toBlock(k2), pY, QuartPos.toBlock(j2));

                            if (pFindClosest)
                            {
                                return blockpos;
                            }
                        }

                        ++i1;
                    }
                }
            }
        }

        return blockpos;
    }

    public boolean canGenerateStructure(StructureFeature<?> pStructure)
    {
        return this.supportedStructures.computeIfAbsent(pStructure, (p_47924_) ->
        {
            return this.possibleBiomes.stream().anyMatch((p_151758_) -> {
                return p_151758_.getGenerationSettings().isValidStart(p_47924_);
            });
        });
    }

    public Set<BlockState> getSurfaceBlocks()
    {
        if (this.surfaceBlocks.isEmpty())
        {
            for (Biome biome : this.possibleBiomes)
            {
                this.surfaceBlocks.add(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial());
            }
        }

        return this.surfaceBlocks;
    }

    static
    {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "vanilla_layered", OverworldBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
    }
}
