package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class RenderChunkRegion implements BlockAndTintGetter
{
    protected final int centerX;
    protected final int centerZ;
    protected final BlockPos start;
    protected final int xLength;
    protected final int yLength;
    protected final int zLength;
    protected final LevelChunk[][] chunks;
    protected final BlockState[] blockStates;
    protected final Level level;

    @Nullable
    public static RenderChunkRegion createIfNotEmpty(Level pLevel, BlockPos pFrom, BlockPos pTo, int pPadding)
    {
        return generateCache(pLevel, pFrom, pTo, pPadding, true);
    }

    public static RenderChunkRegion generateCache(Level worldIn, BlockPos from, BlockPos to, int padding, boolean checkEmpty)
    {
        int i = SectionPos.blockToSectionCoord(from.getX() - padding);
        int j = SectionPos.blockToSectionCoord(from.getZ() - padding);
        int k = SectionPos.blockToSectionCoord(to.getX() + padding);
        int l = SectionPos.blockToSectionCoord(to.getZ() + padding);
        LevelChunk[][] alevelchunk = new LevelChunk[k - i + 1][l - j + 1];

        for (int i1 = i; i1 <= k; ++i1)
        {
            for (int j1 = j; j1 <= l; ++j1)
            {
                alevelchunk[i1 - i][j1 - j] = worldIn.getChunk(i1, j1);
            }
        }

        if (checkEmpty && m_112930_(from, to, i, j, alevelchunk))
        {
            return null;
        }
        else
        {
            int k1 = 1;
            BlockPos blockpos1 = from.offset(-1, -1, -1);
            BlockPos blockpos = to.offset(1, 1, 1);
            return new RenderChunkRegion(worldIn, i, j, alevelchunk, blockpos1, blockpos);
        }
    }

    public static boolean m_112930_(BlockPos p_112931_, BlockPos p_112932_, int p_112933_, int p_112934_, LevelChunk[][] p_112935_)
    {
        for (int i = SectionPos.blockToSectionCoord(p_112931_.getX()); i <= SectionPos.blockToSectionCoord(p_112932_.getX()); ++i)
        {
            for (int j = SectionPos.blockToSectionCoord(p_112931_.getZ()); j <= SectionPos.blockToSectionCoord(p_112932_.getZ()); ++j)
            {
                LevelChunk levelchunk = p_112935_[i - p_112933_][j - p_112934_];

                if (!levelchunk.isYSpaceEmpty(p_112931_.getY(), p_112932_.getY()))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public RenderChunkRegion(Level p_112910_, int p_112911_, int p_112912_, LevelChunk[][] p_112913_, BlockPos p_112914_, BlockPos p_112915_)
    {
        this.level = p_112910_;
        this.centerX = p_112911_;
        this.centerZ = p_112912_;
        this.chunks = p_112913_;
        this.start = p_112914_;
        this.xLength = p_112915_.getX() - p_112914_.getX() + 1;
        this.yLength = p_112915_.getY() - p_112914_.getY() + 1;
        this.zLength = p_112915_.getZ() - p_112914_.getZ() + 1;
        this.blockStates = null;
    }

    protected final int index(BlockPos pX)
    {
        return this.index(pX.getX(), pX.getY(), pX.getZ());
    }

    protected int index(int pX, int pY, int pZ)
    {
        int i = pX - this.start.getX();
        int j = pY - this.start.getY();
        int k = pZ - this.start.getZ();
        return k * this.xLength * this.yLength + j * this.xLength + i;
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        int i = (pPos.getX() >> 4) - this.centerX;
        int j = (pPos.getZ() >> 4) - this.centerZ;
        return this.chunks[i][j].getBlockState(pPos);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return this.getBlockState(pPos).getFluidState();
    }

    public float getShade(Direction p_112940_, boolean p_112941_)
    {
        return this.level.getShade(p_112940_, p_112941_);
    }

    public LevelLightEngine getLightEngine()
    {
        return this.level.getLightEngine();
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        return this.getBlockEntity(pPos, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos, LevelChunk.EntityCreationType p_112929_)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pPos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockEntity(pPos, p_112929_);
    }

    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        return this.level.getBlockTint(pBlockPos, pColorResolver);
    }

    public int getMinBuildHeight()
    {
        return this.level.getMinBuildHeight();
    }

    public int getHeight()
    {
        return this.level.getHeight();
    }

    public Biome getBiome(BlockPos pos)
    {
        return this.level.getBiome(pos);
    }

    public LevelChunk getChunk(int ix, int iz)
    {
        return this.chunks[ix][iz];
    }
}
