package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface BishopTransformer extends AreaTransformer1, DimensionOffset1Transformer {
   int apply(Context p_77034_, int p_77035_, int p_77036_, int p_77037_, int p_77038_, int p_77039_);

   default int applyPixel(BigContext<?> p_77030_, Area p_77031_, int p_77032_, int p_77033_) {
      return this.apply(p_77030_, p_77031_.get(this.getParentX(p_77032_ + 0), this.getParentY(p_77033_ + 2)), p_77031_.get(this.getParentX(p_77032_ + 2), this.getParentY(p_77033_ + 2)), p_77031_.get(this.getParentX(p_77032_ + 2), this.getParentY(p_77033_ + 0)), p_77031_.get(this.getParentX(p_77032_ + 0), this.getParentY(p_77033_ + 0)), p_77031_.get(this.getParentX(p_77032_ + 1), this.getParentY(p_77033_ + 1)));
   }
}