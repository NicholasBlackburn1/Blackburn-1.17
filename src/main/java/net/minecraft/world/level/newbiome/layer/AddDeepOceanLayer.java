package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum AddDeepOceanLayer implements CastleTransformer {
   INSTANCE;

   public int apply(Context p_76560_, int p_76561_, int p_76562_, int p_76563_, int p_76564_, int p_76565_) {
      if (Layers.isShallowOcean(p_76565_)) {
         int i = 0;
         if (Layers.isShallowOcean(p_76561_)) {
            ++i;
         }

         if (Layers.isShallowOcean(p_76562_)) {
            ++i;
         }

         if (Layers.isShallowOcean(p_76564_)) {
            ++i;
         }

         if (Layers.isShallowOcean(p_76563_)) {
            ++i;
         }

         if (i > 3) {
            if (p_76565_ == 44) {
               return 47;
            }

            if (p_76565_ == 45) {
               return 48;
            }

            if (p_76565_ == 0) {
               return 24;
            }

            if (p_76565_ == 46) {
               return 49;
            }

            if (p_76565_ == 10) {
               return 50;
            }

            return 24;
         }
      }

      return p_76565_;
   }
}