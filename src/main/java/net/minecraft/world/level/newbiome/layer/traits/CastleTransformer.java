package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface CastleTransformer extends AreaTransformer1, DimensionOffset1Transformer
{
    int apply(Context pContext, int pNorth, int pEast, int pSouth, int pWest, int pCenter);

default int applyPixel(BigContext<?> pContext, Area pArea, int pX, int pZ)
    {
        return this.apply(pContext, pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 0)), pArea.get(this.getParentX(pX + 2), this.getParentY(pZ + 1)), pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 2)), pArea.get(this.getParentX(pX + 0), this.getParentY(pZ + 1)), pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 1)));
    }
}
