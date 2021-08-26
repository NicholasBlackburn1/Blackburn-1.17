package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public enum RiverInitLayer implements C0Transformer
{
    INSTANCE;

    public int apply(Context pContext, int pValue)
    {
        return Layers.isShallowOcean(pValue) ? pValue : pContext.nextRandom(299999) + 2;
    }
}
