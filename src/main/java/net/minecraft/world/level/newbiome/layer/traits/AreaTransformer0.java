package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer0
{
default <R extends Area> AreaFactory<R> run(BigContext<R> pContext)
    {
        return () ->
        {
            return pContext.createResult((p_164642_, p_164643_) -> {
                pContext.initRandom((long)p_164642_, (long)p_164643_);
                return this.applyPixel(pContext, p_164642_, p_164643_);
            });
        };
    }

    int applyPixel(Context p_76990_, int p_76991_, int p_76992_);
}
