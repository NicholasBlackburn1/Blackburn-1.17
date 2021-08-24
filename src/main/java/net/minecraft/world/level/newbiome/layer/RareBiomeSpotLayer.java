package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeSpotLayer implements C1Transformer {
   INSTANCE;

   public int apply(Context p_76824_, int p_76825_) {
      return p_76824_.nextRandom(57) == 0 && p_76825_ == 1 ? 129 : p_76825_;
   }
}