package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C1Transformer extends AreaTransformer1, DimensionOffset1Transformer
{
    int apply(Context pContext, int pValue);

default int applyPixel(BigContext<?> pContext, Area pArea, int pX, int pZ)
    {
        int i = pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 1));
        return this.apply(pContext, i);
    }
}
