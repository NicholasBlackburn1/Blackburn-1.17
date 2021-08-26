package net.optifine;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkOF extends LevelChunk
{
    private boolean hasEntitiesOF;
    private boolean loadedOF;

    public ChunkOF(Level p_62796_, ChunkPos p_62797_, ChunkBiomeContainer p_62798_)
    {
        super(p_62796_, p_62797_, p_62798_);
    }

    public void addEntity(Entity pEntity)
    {
        this.hasEntitiesOF = true;
        super.addEntity(pEntity);
    }

    public boolean hasEntities()
    {
        return this.hasEntitiesOF;
    }

    public void setLoaded(boolean pLoaded)
    {
        this.loadedOF = pLoaded;
        super.setLoaded(pLoaded);
    }

    public boolean isLoaded()
    {
        return this.loadedOF;
    }
}
