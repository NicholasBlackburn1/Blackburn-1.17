package net.optifine.util;

import net.minecraft.world.level.chunk.LevelChunk;
import net.optifine.ChunkOF;

public class ChunkUtils
{
    public static boolean hasEntities(LevelChunk chunk)
    {
        if (chunk instanceof ChunkOF)
        {
            ChunkOF chunkof = (ChunkOF)chunk;
            return chunkof.hasEntities();
        }
        else
        {
            return true;
        }
    }

    public static boolean isLoaded(LevelChunk chunk)
    {
        if (chunk instanceof ChunkOF)
        {
            ChunkOF chunkof = (ChunkOF)chunk;
            return chunkof.isLoaded();
        }
        else
        {
            return false;
        }
    }
}
