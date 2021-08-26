package net.optifine.override;

import java.util.Arrays;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.optifine.BlockPosM;
import net.optifine.render.RenderEnv;
import net.optifine.util.ArrayCache;

public class ChunkCacheOF implements BlockAndTintGetter
{
    private final RenderChunkRegion chunkCache;
    private final int posX;
    private final int posY;
    private final int posZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int sizeXZ;
    private int[] combinedLights;
    private BlockState[] blockStates;
    private Biome[] biomes;
    private final int arraySize;
    private RenderEnv renderEnv;
    private static final ArrayCache cacheCombinedLights = new ArrayCache(Integer.TYPE, 16);
    private static final ArrayCache cacheBlockStates = new ArrayCache(BlockState.class, 16);
    private static final ArrayCache cacheBiomes = new ArrayCache(Biome.class, 16);

    public ChunkCacheOF(RenderChunkRegion chunkCache, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        this.chunkCache = chunkCache;
        int i = posFromIn.getX() - subIn >> 4;
        int j = posFromIn.getY() - subIn >> 4;
        int k = posFromIn.getZ() - subIn >> 4;
        int l = posToIn.getX() + subIn >> 4;
        int i1 = posToIn.getY() + subIn >> 4;
        int j1 = posToIn.getZ() + subIn >> 4;
        this.sizeX = l - i + 1 << 4;
        this.sizeY = i1 - j + 1 << 4;
        this.sizeZ = j1 - k + 1 << 4;
        this.sizeXZ = this.sizeX * this.sizeZ;
        this.arraySize = this.sizeX * this.sizeY * this.sizeZ;
        this.posX = i << 4;
        this.posY = j << 4;
        this.posZ = k << 4;
    }

    public int getPositionIndex(BlockPos pos)
    {
        int i = pos.getX() - this.posX;

        if (i >= 0 && i < this.sizeX)
        {
            int j = pos.getY() - this.posY;

            if (j >= 0 && j < this.sizeY)
            {
                int k = pos.getZ() - this.posZ;
                return k >= 0 && k < this.sizeZ ? j * this.sizeXZ + k * this.sizeX + i : -1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return -1;
        }
    }

    public int getBrightness(LightLayer pLightType, BlockPos pBlockPos)
    {
        return this.chunkCache.getBrightness(pLightType, pBlockPos);
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        int i = this.getPositionIndex(pPos);

        if (i >= 0 && i < this.arraySize && this.blockStates != null)
        {
            BlockState blockstate = this.blockStates[i];

            if (blockstate == null)
            {
                blockstate = this.chunkCache.getBlockState(pPos);
                this.blockStates[i] = blockstate;
            }

            return blockstate;
        }
        else
        {
            return this.chunkCache.getBlockState(pPos);
        }
    }

    public void renderStart()
    {
        if (this.combinedLights == null)
        {
            this.combinedLights = (int[])cacheCombinedLights.allocate(this.arraySize);
        }

        if (this.blockStates == null)
        {
            this.blockStates = (BlockState[])cacheBlockStates.allocate(this.arraySize);
        }

        if (this.biomes == null)
        {
            this.biomes = (Biome[])cacheBiomes.allocate(this.arraySize);
        }

        Arrays.fill(this.combinedLights, -1);
        Arrays.fill(this.blockStates, (Object)null);
        Arrays.fill(this.biomes, (Object)null);
        this.loadBlockStates();
    }

    private void loadBlockStates()
    {
        if (this.sizeX == 48 && this.sizeY == 48 && this.sizeZ == 48)
        {
            LevelChunk levelchunk = this.chunkCache.getChunk(1, 1);
            BlockPosM blockposm = new BlockPosM();

            for (int i = 16; i < 32; ++i)
            {
                int j = i * this.sizeXZ;

                for (int k = 16; k < 32; ++k)
                {
                    int l = k * this.sizeX;

                    for (int i1 = 16; i1 < 32; ++i1)
                    {
                        blockposm.setXyz(this.posX + i1, this.posY + i, this.posZ + k);
                        int j1 = j + l + i1;
                        BlockState blockstate = levelchunk.getBlockState(blockposm);
                        this.blockStates[j1] = blockstate;
                    }
                }
            }
        }
    }

    public void renderFinish()
    {
        cacheCombinedLights.free(this.combinedLights);
        this.combinedLights = null;
        cacheBlockStates.free(this.blockStates);
        this.blockStates = null;
        cacheBiomes.free(this.biomes);
        this.biomes = null;
    }

    public int[] getCombinedLights()
    {
        return this.combinedLights;
    }

    public Biome getBiome(BlockPos pos)
    {
        int i = this.getPositionIndex(pos);

        if (i >= 0 && i < this.arraySize && this.biomes != null)
        {
            Biome biome = this.biomes[i];

            if (biome == null)
            {
                biome = this.chunkCache.getBiome(pos);
                this.biomes[i] = biome;
            }

            return biome;
        }
        else
        {
            return this.chunkCache.getBiome(pos);
        }
    }

    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        return this.chunkCache.getBlockEntity(pPos, LevelChunk.EntityCreationType.CHECK);
    }

    public BlockEntity getTileEntity(BlockPos pos, LevelChunk.EntityCreationType type)
    {
        return this.chunkCache.getBlockEntity(pos, type);
    }

    public boolean canSeeSky(BlockPos pBlockPos)
    {
        return this.chunkCache.canSeeSky(pBlockPos);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return this.getBlockState(pPos).getFluidState();
    }

    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        return this.chunkCache.getBlockTint(pBlockPos, pColorResolver);
    }

    public LevelLightEngine getLightEngine()
    {
        return this.chunkCache.getLightEngine();
    }

    public RenderEnv getRenderEnv()
    {
        return this.renderEnv;
    }

    public void setRenderEnv(RenderEnv renderEnv)
    {
        this.renderEnv = renderEnv;
    }

    public float getShade(Direction p_45522_, boolean p_45523_)
    {
        return this.chunkCache.getShade(p_45522_, p_45523_);
    }

    public int getHeight()
    {
        return this.chunkCache.getHeight();
    }

    public int getMinBuildHeight()
    {
        return this.chunkCache.getMinBuildHeight();
    }
}
