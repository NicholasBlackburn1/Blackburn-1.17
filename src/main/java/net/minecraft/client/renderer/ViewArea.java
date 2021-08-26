package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.optifine.Config;
import net.optifine.render.VboRegion;

public class ViewArea
{
    protected final LevelRenderer levelRenderer;
    protected final Level level;
    protected int chunkGridSizeY;
    protected int chunkGridSizeX;
    protected int chunkGridSizeZ;
    public ChunkRenderDispatcher.RenderChunk[] chunks;
    private Map<ChunkPos, VboRegion[]> mapVboRegions = new HashMap<>();

    public ViewArea(ChunkRenderDispatcher p_110845_, Level p_110846_, int p_110847_, LevelRenderer p_110848_)
    {
        this.levelRenderer = p_110848_;
        this.level = p_110846_;
        this.setViewDistance(p_110847_);
        this.createChunks(p_110845_);
    }

    protected void createChunks(ChunkRenderDispatcher pRenderChunkFactory)
    {
        int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
        this.chunks = new ChunkRenderDispatcher.RenderChunk[i];
        int j = this.level.getMinBuildHeight();

        for (int k = 0; k < this.chunkGridSizeX; ++k)
        {
            for (int l = 0; l < this.chunkGridSizeY; ++l)
            {
                for (int i1 = 0; i1 < this.chunkGridSizeZ; ++i1)
                {
                    int j1 = this.getChunkIndex(k, l, i1);
                    this.chunks[j1] = pRenderChunkFactory.new RenderChunk(j1);
                    this.chunks[j1].setOrigin(k * 16, l * 16 + j, i1 * 16);

                    if (Config.isVbo() && Config.isRenderRegions())
                    {
                        this.updateVboRegion(this.chunks[j1]);
                    }
                }
            }
        }

        for (int k1 = 0; k1 < this.chunks.length; ++k1)
        {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = this.chunks[k1];

            for (int l1 = 0; l1 < Direction.VALUES.length; ++l1)
            {
                Direction direction = Direction.VALUES[l1];
                BlockPos blockpos = chunkrenderdispatcher$renderchunk1.getRelativeOrigin(direction);
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.getRenderChunkAt(blockpos);
                chunkrenderdispatcher$renderchunk1.setRenderChunkNeighbour(direction, chunkrenderdispatcher$renderchunk);
            }
        }
    }

    public void releaseAllBuffers()
    {
        for (ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk : this.chunks)
        {
            chunkrenderdispatcher$renderchunk.releaseBuffers();
        }

        this.deleteVboRegions();
    }

    private int getChunkIndex(int pX, int pY, int pZ)
    {
        return (pZ * this.chunkGridSizeY + pY) * this.chunkGridSizeX + pX;
    }

    protected void setViewDistance(int pRenderDistanceChunks)
    {
        int i = pRenderDistanceChunks * 2 + 1;
        this.chunkGridSizeX = i;
        this.chunkGridSizeY = this.level.getSectionsCount();
        this.chunkGridSizeZ = i;
    }

    public void repositionCamera(double pViewEntityX, double p_110852_)
    {
        int i = Mth.floor(pViewEntityX);
        int j = Mth.floor(p_110852_);

        for (int k = 0; k < this.chunkGridSizeX; ++k)
        {
            int l = this.chunkGridSizeX * 16;
            int i1 = i - 8 - l / 2;
            int j1 = i1 + Math.floorMod(k * 16 - i1, l);

            for (int k1 = 0; k1 < this.chunkGridSizeZ; ++k1)
            {
                int l1 = this.chunkGridSizeZ * 16;
                int i2 = j - 8 - l1 / 2;
                int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

                for (int k2 = 0; k2 < this.chunkGridSizeY; ++k2)
                {
                    int l2 = this.level.getMinBuildHeight() + k2 * 16;
                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(k, k2, k1)];
                    chunkrenderdispatcher$renderchunk.setOrigin(j1, l2, j2);
                }
            }
        }
    }

    public void setDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pRerenderOnMainThread)
    {
        int i = Math.floorMod(pSectionX, this.chunkGridSizeX);
        int j = Math.floorMod(pSectionY - this.level.getMinSection(), this.chunkGridSizeY);
        int k = Math.floorMod(pSectionZ, this.chunkGridSizeZ);
        ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(i, j, k)];
        chunkrenderdispatcher$renderchunk.setDirty(pRerenderOnMainThread);
    }

    @Nullable
    public ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pPos)
    {
        int i = pPos.getX() >> 4;
        int j = pPos.getY() - this.level.getMinBuildHeight() >> 4;
        int k = pPos.getZ() >> 4;

        if (j >= 0 && j < this.chunkGridSizeY)
        {
            i = Mth.positiveModulo(i, this.chunkGridSizeX);
            k = Mth.positiveModulo(k, this.chunkGridSizeZ);
            return this.chunks[this.getChunkIndex(i, j, k)];
        }
        else
        {
            return null;
        }
    }

    private void updateVboRegion(ChunkRenderDispatcher.RenderChunk renderChunk)
    {
        BlockPos blockpos = renderChunk.getOrigin();
        int i = blockpos.getX() >> 8 << 8;
        int j = blockpos.getZ() >> 8 << 8;
        ChunkPos chunkpos = new ChunkPos(i, j);
        RenderType[] arendertype = RenderType.CHUNK_RENDER_TYPES;
        VboRegion[] avboregion = this.mapVboRegions.get(chunkpos);

        if (avboregion == null)
        {
            avboregion = new VboRegion[arendertype.length];

            for (int k = 0; k < arendertype.length; ++k)
            {
                if (!arendertype[k].isNeedsSorting())
                {
                    avboregion[k] = new VboRegion(arendertype[k]);
                }
            }

            this.mapVboRegions.put(chunkpos, avboregion);
        }

        for (int l = 0; l < arendertype.length; ++l)
        {
            RenderType rendertype = arendertype[l];
            VboRegion vboregion = avboregion[l];
            renderChunk.getBuffer(rendertype).setVboRegion(vboregion);
        }
    }

    public void deleteVboRegions()
    {
        for (ChunkPos chunkpos : this.mapVboRegions.keySet())
        {
            VboRegion[] avboregion = this.mapVboRegions.get(chunkpos);

            for (int i = 0; i < avboregion.length; ++i)
            {
                VboRegion vboregion = avboregion[i];

                if (vboregion != null)
                {
                    vboregion.deleteGlBuffers();
                }

                avboregion[i] = null;
            }
        }

        this.mapVboRegions.clear();
    }
}
