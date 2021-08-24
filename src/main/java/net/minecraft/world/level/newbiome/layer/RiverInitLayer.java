package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public enum RiverInitLayer implements C0Transformer {
   INSTANCE;

   public int apply(Context p_76871_, int p_76872_) {
      return Layers.isShallowOcean(p_76872_) ? p_76872_ : p_76871_.nextRandom(299999) + 2;
   }
}