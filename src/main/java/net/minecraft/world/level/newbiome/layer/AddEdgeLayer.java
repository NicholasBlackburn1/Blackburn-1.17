package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public class AddEdgeLayer {
   public static enum CoolWarm implements CastleTransformer {
      INSTANCE;

      public int apply(Context p_76576_, int p_76577_, int p_76578_, int p_76579_, int p_76580_, int p_76581_) {
         return p_76581_ != 1 || p_76577_ != 3 && p_76578_ != 3 && p_76580_ != 3 && p_76579_ != 3 && p_76577_ != 4 && p_76578_ != 4 && p_76580_ != 4 && p_76579_ != 4 ? p_76581_ : 2;
      }
   }

   public static enum HeatIce implements CastleTransformer {
      INSTANCE;

      public int apply(Context p_76592_, int p_76593_, int p_76594_, int p_76595_, int p_76596_, int p_76597_) {
         return p_76597_ != 4 || p_76593_ != 1 && p_76594_ != 1 && p_76596_ != 1 && p_76595_ != 1 && p_76593_ != 2 && p_76594_ != 2 && p_76596_ != 2 && p_76595_ != 2 ? p_76597_ : 3;
      }
   }

   public static enum IntroduceSpecial implements C0Transformer {
      INSTANCE;

      public int apply(Context p_76608_, int p_76609_) {
         if (!Layers.isShallowOcean(p_76609_) && p_76608_.nextRandom(13) == 0) {
            p_76609_ |= 1 + p_76608_.nextRandom(15) << 8 & 3840;
         }

         return p_76609_;
      }
   }
}