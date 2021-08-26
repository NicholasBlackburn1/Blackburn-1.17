package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class DebugLevelSource extends ChunkGenerator
{
    public static final Codec<DebugLevelSource> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY).xmap(DebugLevelSource::new, DebugLevelSource::biomes).stable().codec();
    private static final int BLOCK_MARGIN = 2;
    private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((p_64147_) ->
    {
        return p_64147_.getStateDefinition().getPossibleStates().stream();
    }).collect(Collectors.toList());
    private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
    private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
    public static final int HEIGHT = 70;
    public static final int BARRIER_HEIGHT = 60;
    private final Registry<Biome> biomes;

    public DebugLevelSource(Registry<Biome> p_64120_)
    {
        super(new FixedBiomeSource(p_64120_.getOrThrow(Biomes.PLAINS)), new StructureSettings(false));
        this.biomes = p_64120_;
    }

    public Registry<Biome> biomes()
    {
        return this.biomes;
    }

    protected Codec <? extends ChunkGenerator > codec()
    {
        return CODEC;
    }

    public ChunkGenerator withSeed(long p_64130_)
    {
        return this;
    }

    public void buildSurfaceAndBedrock(WorldGenRegion p_64140_, ChunkAccess p_64141_)
    {
    }

    public void applyCarvers(long p_64132_, BiomeManager p_64133_, ChunkAccess p_64134_, GenerationStep.Carving p_64135_)
    {
    }

    public void applyBiomeDecoration(WorldGenRegion p_64137_, StructureFeatureManager p_64138_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        ChunkPos chunkpos = p_64137_.getCenter();

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                int k = SectionPos.sectionToBlockCoord(chunkpos.x, i);
                int l = SectionPos.sectionToBlockCoord(chunkpos.z, j);
                p_64137_.setBlock(blockpos$mutableblockpos.set(k, 60, l), BARRIER, 2);
                BlockState blockstate = getBlockStateFor(k, l);

                if (blockstate != null)
                {
                    p_64137_.setBlock(blockpos$mutableblockpos.set(k, 70, l), blockstate, 2);
                }
            }
        }
    }

    public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_158238_, StructureFeatureManager p_158239_, ChunkAccess p_158240_)
    {
        return CompletableFuture.completedFuture(p_158240_);
    }

    public int getBaseHeight(int p_158233_, int p_158234_, Heightmap.Types p_158235_, LevelHeightAccessor p_158236_)
    {
        return 0;
    }

    public NoiseColumn getBaseColumn(int p_158229_, int p_158230_, LevelHeightAccessor p_158231_)
    {
        return new NoiseColumn(0, new BlockState[0]);
    }

    public static BlockState getBlockStateFor(int p_64149_, int p_64150_)
    {
        BlockState blockstate = AIR;

        if (p_64149_ > 0 && p_64150_ > 0 && p_64149_ % 2 != 0 && p_64150_ % 2 != 0)
        {
            p_64149_ = p_64149_ / 2;
            p_64150_ = p_64150_ / 2;

            if (p_64149_ <= GRID_WIDTH && p_64150_ <= GRID_HEIGHT)
            {
                int i = Mth.abs(p_64149_ * GRID_WIDTH + p_64150_);

                if (i < ALL_BLOCKS.size())
                {
                    blockstate = ALL_BLOCKS.get(i);
                }
            }
        }

        return blockstate;
    }
}
