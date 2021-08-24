package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum OceanMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
   INSTANCE;

   public int applyPixel(Context p_76797_, Area p_76798_, Area p_76799_, int p_76800_, int p_76801_) {
      int i = p_76798_.get(this.getParentX(p_76800_), this.getParentY(p_76801_));
      int j = p_76799_.get(this.getParentX(p_76800_), this.getParentY(p_76801_));
      if (!Layers.isOcean(i)) {
         return i;
      } else {
         int k = 8;
         int l = 4;

         for(int i1 = -8; i1 <= 8; i1 += 4) {
            for(int j1 = -8; j1 <= 8; j1 += 4) {
               int k1 = p_76798_.get(this.getParentX(p_76800_ + i1), this.getParentY(p_76801_ + j1));
               if (!Layers.isOcean(k1)) {
                  if (j == 44) {
                     return 45;
                  }

                  if (j == 10) {
                     return 46;
                  }
               }
            }
         }

         if (i == 24) {
            if (j == 45) {
               return 48;
            }

            if (j == 0) {
               return 24;
            }

            if (j == 46) {
               return 49;
            }

            if (j == 10) {
               return 50;
            }
         }

         return j;
      }
   }
}