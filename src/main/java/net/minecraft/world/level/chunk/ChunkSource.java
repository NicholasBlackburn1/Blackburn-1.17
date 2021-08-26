package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements LightChunkGetter, AutoCloseable
{
    @Nullable
    public LevelChunk getChunk(int pChunkX, int pChunkZ, boolean pRequiredStatus)
    {
        return (LevelChunk)this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, pRequiredStatus);
    }

    @Nullable
    public LevelChunk getChunkNow(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, false);
    }

    @Nullable
    public BlockGetter getChunkForLighting(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int pX, int pZ)
    {
        return this.getChunk(pX, pZ, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad);

    public abstract void tick(BooleanSupplier pHasTimeLeft);

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    public void close() throws IOException
    {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean pHostile, boolean pPeaceful)
    {
    }

    public void updateChunkForced(ChunkPos pPos, boolean pAdd)
    {
    }
}
