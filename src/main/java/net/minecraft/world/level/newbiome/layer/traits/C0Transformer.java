package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C0Transformer extends AreaTransformer1, DimensionOffset0Transformer {
   int apply(Context p_77045_, int p_77046_);

   default int applyPixel(BigContext<?> p_77041_, Area p_77042_, int p_77043_, int p_77044_) {
      return this.apply(p_77041_, p_77042_.get(this.getParentX(p_77043_), this.getParentY(p_77044_)));
   }
}