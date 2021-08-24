package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer {
   INSTANCE;

   public int apply(Context p_76664_, int p_76665_, int p_76666_, int p_76667_, int p_76668_, int p_76669_) {
      int[] aint = new int[1];
      if (!this.checkEdge(aint, p_76669_) && !this.checkEdgeStrict(aint, p_76665_, p_76666_, p_76667_, p_76668_, p_76669_, 38, 37) && !this.checkEdgeStrict(aint, p_76665_, p_76666_, p_76667_, p_76668_, p_76669_, 39, 37) && !this.checkEdgeStrict(aint, p_76665_, p_76666_, p_76667_, p_76668_, p_76669_, 32, 5)) {
         if (p_76669_ != 2 || p_76665_ != 12 && p_76666_ != 12 && p_76668_ != 12 && p_76667_ != 12) {
            if (p_76669_ == 6) {
               if (p_76665_ == 2 || p_76666_ == 2 || p_76668_ == 2 || p_76667_ == 2 || p_76665_ == 30 || p_76666_ == 30 || p_76668_ == 30 || p_76667_ == 30 || p_76665_ == 12 || p_76666_ == 12 || p_76668_ == 12 || p_76667_ == 12) {
                  return 1;
               }

               if (p_76665_ == 21 || p_76667_ == 21 || p_76666_ == 21 || p_76668_ == 21 || p_76665_ == 168 || p_76667_ == 168 || p_76666_ == 168 || p_76668_ == 168) {
                  return 23;
               }
            }

            return p_76669_;
         } else {
            return 34;
         }
      } else {
         return aint[0];
      }
   }

   private boolean checkEdge(int[] p_76671_, int p_76672_) {
      if (!Layers.isSame(p_76672_, 3)) {
         return false;
      } else {
         p_76671_[0] = p_76672_;
         return true;
      }
   }

   private boolean checkEdgeStrict(int[] p_76674_, int p_76675_, int p_76676_, int p_76677_, int p_76678_, int p_76679_, int p_76680_, int p_76681_) {
      if (p_76679_ != p_76680_) {
         return false;
      } else {
         if (Layers.isSame(p_76675_, p_76680_) && Layers.isSame(p_76676_, p_76680_) && Layers.isSame(p_76678_, p_76680_) && Layers.isSame(p_76677_, p_76680_)) {
            p_76674_[0] = p_76679_;
         } else {
            p_76674_[0] = p_76681_;
         }

         return true;
      }
   }
}