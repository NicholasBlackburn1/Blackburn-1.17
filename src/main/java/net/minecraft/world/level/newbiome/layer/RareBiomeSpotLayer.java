package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeSpotLayer implements C1Transformer
{
    INSTANCE;

    public int apply(Context pContext, int pValue)
    {
        return pContext.nextRandom(57) == 0 && pValue == 1 ? 129 : pValue;
    }
}
