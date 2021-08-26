package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader extends BlockAndTintGetter, CollisionGetter, BiomeManager.NoiseBiomeSource
{
    @Nullable
    ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus p_46825_, boolean p_46826_);

    @Deprecated
    boolean hasChunk(int pChunkX, int pChunkZ);

    int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ);

    int getSkyDarken();

    BiomeManager getBiomeManager();

default Biome getBiome(BlockPos pPos)
    {
        return this.getBiomeManager().getBiome(pPos);
    }

default Stream<BlockState> getBlockStatesIfLoaded(AABB pAabb)
    {
        int i = Mth.floor(pAabb.minX);
        int j = Mth.floor(pAabb.maxX);
        int k = Mth.floor(pAabb.minY);
        int l = Mth.floor(pAabb.maxY);
        int i1 = Mth.floor(pAabb.minZ);
        int j1 = Mth.floor(pAabb.maxZ);
        return this.hasChunksAt(i, k, i1, j, l, j1) ? this.getBlockStates(pAabb) : Stream.empty();
    }

default int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        return pColorResolver.getColor(this.getBiome(pBlockPos), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
    }

default Biome getNoiseBiome(int pX, int pY, int pZ)
    {
        ChunkAccess chunkaccess = this.getChunk(QuartPos.toSection(pX), QuartPos.toSection(pZ), ChunkStatus.BIOMES, false);
        return chunkaccess != null && chunkaccess.getBiomes() != null ? chunkaccess.getBiomes().getNoiseBiome(pX, pY, pZ) : this.getUncachedNoiseBiome(pX, pY, pZ);
    }

    Biome getUncachedNoiseBiome(int pX, int pY, int pZ);

    boolean isClientSide();

    @Deprecated
    int getSeaLevel();

    DimensionType dimensionType();

default int getMinBuildHeight()
    {
        return this.dimensionType().minY();
    }

default int getHeight()
    {
        return this.dimensionType().height();
    }

default BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos)
    {
        return new BlockPos(pPos.getX(), this.getHeight(pHeightmapType, pPos.getX(), pPos.getZ()), pPos.getZ());
    }

default boolean isEmptyBlock(BlockPos pPos)
    {
        return this.getBlockState(pPos).isAir();
    }

default boolean canSeeSkyFromBelowWater(BlockPos pPos)
    {
        if (pPos.getY() >= this.getSeaLevel())
        {
            return this.canSeeSky(pPos);
        }
        else
        {
            BlockPos blockpos = new BlockPos(pPos.getX(), this.getSeaLevel(), pPos.getZ());

            if (!this.canSeeSky(blockpos))
            {
                return false;
            }
            else
            {
                for (BlockPos blockpos1 = blockpos.below(); blockpos1.getY() > pPos.getY(); blockpos1 = blockpos1.below())
                {
                    BlockState blockstate = this.getBlockState(blockpos1);

                    if (blockstate.getLightBlock(this, blockpos1) > 0 && !blockstate.getMaterial().isLiquid())
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    @Deprecated

default float getBrightness(BlockPos pPos)
    {
        return this.dimensionType().brightness(this.getMaxLocalRawBrightness(pPos));
    }

default int getDirectSignal(BlockPos pPos, Direction pDirection)
    {
        return this.getBlockState(pPos).getDirectSignal(this, pPos, pDirection);
    }

default ChunkAccess getChunk(BlockPos pChunkX)
    {
        return this.getChunk(SectionPos.blockToSectionCoord(pChunkX.getX()), SectionPos.blockToSectionCoord(pChunkX.getZ()));
    }

default ChunkAccess getChunk(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, true);
    }

default ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus p_46822_)
    {
        return this.getChunk(pChunkX, pChunkZ, p_46822_, true);
    }

    @Nullable

default BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
    }

default boolean isWaterAt(BlockPos pPos)
    {
        return this.getFluidState(pPos).is(FluidTags.WATER);
    }

default boolean containsAnyLiquid(AABB pBb)
    {
        int i = Mth.floor(pBb.minX);
        int j = Mth.ceil(pBb.maxX);
        int k = Mth.floor(pBb.minY);
        int l = Mth.ceil(pBb.maxY);
        int i1 = Mth.floor(pBb.minZ);
        int j1 = Mth.ceil(pBb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    BlockState blockstate = this.getBlockState(blockpos$mutableblockpos.set(k1, l1, i2));

                    if (!blockstate.getFluidState().isEmpty())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

default int getMaxLocalRawBrightness(BlockPos pPos)
    {
        return this.getMaxLocalRawBrightness(pPos, this.getSkyDarken());
    }

default int getMaxLocalRawBrightness(BlockPos pPos, int p_46851_)
    {
        return pPos.getX() >= -30000000 && pPos.getZ() >= -30000000 && pPos.getX() < 30000000 && pPos.getZ() < 30000000 ? this.getRawBrightness(pPos, p_46851_) : 15;
    }

    @Deprecated

default boolean hasChunkAt(int pPos, int p_151579_)
    {
        return this.hasChunk(SectionPos.blockToSectionCoord(pPos), SectionPos.blockToSectionCoord(p_151579_));
    }

    @Deprecated

default boolean hasChunkAt(BlockPos pPos)
    {
        return this.hasChunkAt(pPos.getX(), pPos.getZ());
    }

    @Deprecated

default boolean hasChunksAt(BlockPos pFromX, BlockPos pFromY)
    {
        return this.hasChunksAt(pFromX.getX(), pFromX.getY(), pFromX.getZ(), pFromY.getX(), pFromY.getY(), pFromY.getZ());
    }

    @Deprecated

default boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX, int pToY, int pToZ)
    {
        return pToY >= this.getMinBuildHeight() && pFromY < this.getMaxBuildHeight() ? this.hasChunksAt(pFromX, pFromZ, pToX, pToZ) : false;
    }

    @Deprecated

default boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX)
    {
        int i = SectionPos.blockToSectionCoord(pFromX);
        int j = SectionPos.blockToSectionCoord(pFromZ);
        int k = SectionPos.blockToSectionCoord(pFromY);
        int l = SectionPos.blockToSectionCoord(pToX);

        for (int i1 = i; i1 <= j; ++i1)
        {
            for (int j1 = k; j1 <= l; ++j1)
            {
                if (!this.hasChunk(i1, j1))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
