package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C1Transformer extends AreaTransformer1, DimensionOffset1Transformer {
   int apply(Context p_77052_, int p_77053_);

   default int applyPixel(BigContext<?> p_77048_, Area p_77049_, int p_77050_, int p_77051_) {
      int i = p_77049_.get(this.getParentX(p_77050_ + 1), this.getParentY(p_77051_ + 1));
      return this.apply(p_77048_, i);
   }
}