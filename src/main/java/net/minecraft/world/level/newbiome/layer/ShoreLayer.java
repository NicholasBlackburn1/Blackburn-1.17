package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer {
   INSTANCE;

   private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
   private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

   public int apply(Context p_76918_, int p_76919_, int p_76920_, int p_76921_, int p_76922_, int p_76923_) {
      if (p_76923_ == 14) {
         if (Layers.isShallowOcean(p_76919_) || Layers.isShallowOcean(p_76920_) || Layers.isShallowOcean(p_76921_) || Layers.isShallowOcean(p_76922_)) {
            return 15;
         }
      } else if (JUNGLES.contains(p_76923_)) {
         if (!isJungleCompatible(p_76919_) || !isJungleCompatible(p_76920_) || !isJungleCompatible(p_76921_) || !isJungleCompatible(p_76922_)) {
            return 23;
         }

         if (Layers.isOcean(p_76919_) || Layers.isOcean(p_76920_) || Layers.isOcean(p_76921_) || Layers.isOcean(p_76922_)) {
            return 16;
         }
      } else if (p_76923_ != 3 && p_76923_ != 34 && p_76923_ != 20) {
         if (SNOWY.contains(p_76923_)) {
            if (!Layers.isOcean(p_76923_) && (Layers.isOcean(p_76919_) || Layers.isOcean(p_76920_) || Layers.isOcean(p_76921_) || Layers.isOcean(p_76922_))) {
               return 26;
            }
         } else if (p_76923_ != 37 && p_76923_ != 38) {
            if (!Layers.isOcean(p_76923_) && p_76923_ != 7 && p_76923_ != 6 && (Layers.isOcean(p_76919_) || Layers.isOcean(p_76920_) || Layers.isOcean(p_76921_) || Layers.isOcean(p_76922_))) {
               return 16;
            }
         } else if (!Layers.isOcean(p_76919_) && !Layers.isOcean(p_76920_) && !Layers.isOcean(p_76921_) && !Layers.isOcean(p_76922_) && (!this.isMesa(p_76919_) || !this.isMesa(p_76920_) || !this.isMesa(p_76921_) || !this.isMesa(p_76922_))) {
            return 2;
         }
      } else if (!Layers.isOcean(p_76923_) && (Layers.isOcean(p_76919_) || Layers.isOcean(p_76920_) || Layers.isOcean(p_76921_) || Layers.isOcean(p_76922_))) {
         return 25;
      }

      return p_76923_;
   }

   private static boolean isJungleCompatible(int p_76925_) {
      return JUNGLES.contains(p_76925_) || p_76925_ == 4 || p_76925_ == 5 || Layers.isOcean(p_76925_);
   }

   private boolean isMesa(int p_76927_) {
      return p_76927_ == 37 || p_76927_ == 38 || p_76927_ == 39 || p_76927_ == 165 || p_76927_ == 166 || p_76927_ == 167;
   }
}