package net.minecraftforgeop.client.extensions;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IForgeRenderChunk
{
default RenderChunkRegion createRegionRenderCache(Level world, BlockPos from, BlockPos to, int subtract)
    {
        return RenderChunkRegion.createIfNotEmpty(world, from, to, subtract);
    }
}
