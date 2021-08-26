package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C0Transformer extends AreaTransformer1, DimensionOffset0Transformer
{
    int apply(Context pContext, int pValue);

default int applyPixel(BigContext<?> pContext, Area pArea, int pX, int pZ)
    {
        return this.apply(pContext, pArea.get(this.getParentX(pX), this.getParentY(pZ)));
    }
}
