package net.minecraft.world.level;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements BlockGetter, CollisionGetter
{
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;

    public PathNavigationRegion(Level p_47164_, BlockPos p_47165_, BlockPos p_47166_)
    {
        this.level = p_47164_;
        this.centerX = SectionPos.blockToSectionCoord(p_47165_.getX());
        this.centerZ = SectionPos.blockToSectionCoord(p_47165_.getZ());
        int i = SectionPos.blockToSectionCoord(p_47166_.getX());
        int j = SectionPos.blockToSectionCoord(p_47166_.getZ());
        this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
        ChunkSource chunksource = p_47164_.getChunkSource();
        this.allEmpty = true;

        for (int k = this.centerX; k <= i; ++k)
        {
            for (int l = this.centerZ; l <= j; ++l)
            {
                this.chunks[k - this.centerX][l - this.centerZ] = chunksource.getChunkNow(k, l);
            }
        }

        for (int i1 = SectionPos.blockToSectionCoord(p_47165_.getX()); i1 <= SectionPos.blockToSectionCoord(p_47166_.getX()); ++i1)
        {
            for (int j1 = SectionPos.blockToSectionCoord(p_47165_.getZ()); j1 <= SectionPos.blockToSectionCoord(p_47166_.getZ()); ++j1)
            {
                ChunkAccess chunkaccess = this.chunks[i1 - this.centerX][j1 - this.centerZ];

                if (chunkaccess != null && !chunkaccess.isYSpaceEmpty(p_47165_.getY(), p_47166_.getY()))
                {
                    this.allEmpty = false;
                    return;
                }
            }
        }
    }

    private ChunkAccess getChunk(BlockPos p_47186_)
    {
        return this.getChunk(SectionPos.blockToSectionCoord(p_47186_.getX()), SectionPos.blockToSectionCoord(p_47186_.getZ()));
    }

    private ChunkAccess getChunk(int p_47168_, int p_47169_)
    {
        int i = p_47168_ - this.centerX;
        int j = p_47169_ - this.centerZ;

        if (i >= 0 && i < this.chunks.length && j >= 0 && j < this.chunks[i].length)
        {
            ChunkAccess chunkaccess = this.chunks[i][j];
            return (ChunkAccess)(chunkaccess != null ? chunkaccess : new EmptyLevelChunk(this.level, new ChunkPos(p_47168_, p_47169_)));
        }
        else
        {
            return new EmptyLevelChunk(this.level, new ChunkPos(p_47168_, p_47169_));
        }
    }

    public WorldBorder getWorldBorder()
    {
        return this.level.getWorldBorder();
    }

    public BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        ChunkAccess chunkaccess = this.getChunk(pPos);
        return chunkaccess.getBlockEntity(pPos);
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            ChunkAccess chunkaccess = this.getChunk(pPos);
            return chunkaccess.getBlockState(pPos);
        }
    }

    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_47176_, AABB p_47177_, Predicate<Entity> p_47178_)
    {
        return Stream.empty();
    }

    public Stream<VoxelShape> getCollisions(@Nullable Entity p_47182_, AABB p_47183_, Predicate<Entity> p_47184_)
    {
        return this.getBlockCollisions(p_47182_, p_47183_);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return Fluids.EMPTY.defaultFluidState();
        }
        else
        {
            ChunkAccess chunkaccess = this.getChunk(pPos);
            return chunkaccess.getFluidState(pPos);
        }
    }

    public int getMinBuildHeight()
    {
        return this.level.getMinBuildHeight();
    }

    public int getHeight()
    {
        return this.level.getHeight();
    }

    public ProfilerFiller getProfiler()
    {
        return this.level.getProfiler();
    }
}
