package net.minecraft.server.level;

import net.minecraft.core.BlockPos;

public class BlockDestructionProgress implements Comparable<BlockDestructionProgress>
{
    private final int id;
    private final BlockPos pos;
    private int progress;
    private int updatedRenderTick;

    public BlockDestructionProgress(int p_139979_, BlockPos p_139980_)
    {
        this.id = p_139979_;
        this.pos = p_139980_;
    }

    public int getId()
    {
        return this.id;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public void setProgress(int pDamage)
    {
        if (pDamage > 10)
        {
            pDamage = 10;
        }

        this.progress = pDamage;
    }

    public int getProgress()
    {
        return this.progress;
    }

    public void updateTick(int pCreatedAtCloudUpdateTick)
    {
        this.updatedRenderTick = pCreatedAtCloudUpdateTick;
    }

    public int getUpdatedRenderTick()
    {
        return this.updatedRenderTick;
    }

    public boolean equals(Object p_139993_)
    {
        if (this == p_139993_)
        {
            return true;
        }
        else if (p_139993_ != null && this.getClass() == p_139993_.getClass())
        {
            BlockDestructionProgress blockdestructionprogress = (BlockDestructionProgress)p_139993_;
            return this.id == blockdestructionprogress.id;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return Integer.hashCode(this.id);
    }

    public int compareTo(BlockDestructionProgress p_139984_)
    {
        return this.progress != p_139984_.progress ? Integer.compare(this.progress, p_139984_.progress) : Integer.compare(this.id, p_139984_.id);
    }
}
