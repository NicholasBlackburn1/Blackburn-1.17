package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddIslandLayer implements BishopTransformer {
   INSTANCE;

   public int apply(Context p_76620_, int p_76621_, int p_76622_, int p_76623_, int p_76624_, int p_76625_) {
      if (!Layers.isShallowOcean(p_76625_) || Layers.isShallowOcean(p_76624_) && Layers.isShallowOcean(p_76623_) && Layers.isShallowOcean(p_76621_) && Layers.isShallowOcean(p_76622_)) {
         if (!Layers.isShallowOcean(p_76625_) && (Layers.isShallowOcean(p_76624_) || Layers.isShallowOcean(p_76621_) || Layers.isShallowOcean(p_76623_) || Layers.isShallowOcean(p_76622_)) && p_76620_.nextRandom(5) == 0) {
            if (Layers.isShallowOcean(p_76624_)) {
               return p_76625_ == 4 ? 4 : p_76624_;
            }

            if (Layers.isShallowOcean(p_76621_)) {
               return p_76625_ == 4 ? 4 : p_76621_;
            }

            if (Layers.isShallowOcean(p_76623_)) {
               return p_76625_ == 4 ? 4 : p_76623_;
            }

            if (Layers.isShallowOcean(p_76622_)) {
               return p_76625_ == 4 ? 4 : p_76622_;
            }
         }

         return p_76625_;
      } else {
         int i = 1;
         int j = 1;
         if (!Layers.isShallowOcean(p_76624_) && p_76620_.nextRandom(i++) == 0) {
            j = p_76624_;
         }

         if (!Layers.isShallowOcean(p_76623_) && p_76620_.nextRandom(i++) == 0) {
            j = p_76623_;
         }

         if (!Layers.isShallowOcean(p_76621_) && p_76620_.nextRandom(i++) == 0) {
            j = p_76621_;
         }

         if (!Layers.isShallowOcean(p_76622_) && p_76620_.nextRandom(i++) == 0) {
            j = p_76622_;
         }

         if (p_76620_.nextRandom(3) == 0) {
            return j;
         } else {
            return j == 4 ? 4 : p_76625_;
         }
      }
   }
}