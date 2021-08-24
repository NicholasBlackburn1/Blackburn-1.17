package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum SmoothLayer implements CastleTransformer {
   INSTANCE;

   public int apply(Context p_76938_, int p_76939_, int p_76940_, int p_76941_, int p_76942_, int p_76943_) {
      boolean flag = p_76940_ == p_76942_;
      boolean flag1 = p_76939_ == p_76941_;
      if (flag == flag1) {
         if (flag) {
            return p_76938_.nextRandom(2) == 0 ? p_76942_ : p_76939_;
         } else {
            return p_76943_;
         }
      } else {
         return flag ? p_76942_ : p_76939_;
      }
   }
}