package net.optifine.render;

import net.minecraft.world.phys.AABB;

public class AabbFrame extends AABB
{
    private int frameCount = -1;
    private boolean inFrustumFully = false;

    public AabbFrame(double p_82295_, double p_82296_, double p_82297_, double p_82298_, double p_82299_, double p_82300_)
    {
        super(p_82295_, p_82296_, p_82297_, p_82298_, p_82299_, p_82300_);
    }

    public boolean isBoundingBoxInFrustumFully(ICamera camera, int frameCount)
    {
        if (this.frameCount != frameCount)
        {
            this.inFrustumFully = camera.isBoxInFrustumFully(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            this.frameCount = frameCount;
        }

        return this.inFrustumFully;
    }
}
