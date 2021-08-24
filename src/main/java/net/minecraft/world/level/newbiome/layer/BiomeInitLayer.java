package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public class BiomeInitLayer implements C0Transformer {
   private static final int[] LEGACY_WARM_BIOMES = new int[]{2, 4, 3, 6, 1, 5};
   private static final int[] WARM_BIOMES = new int[]{2, 2, 2, 35, 35, 1};
   private static final int[] MEDIUM_BIOMES = new int[]{4, 29, 3, 1, 27, 6};
   private static final int[] COLD_BIOMES = new int[]{4, 3, 5, 1};
   private static final int[] ICE_BIOMES = new int[]{12, 12, 12, 30};
   private int[] warmBiomes = WARM_BIOMES;

   public BiomeInitLayer(boolean p_76693_) {
      if (p_76693_) {
         this.warmBiomes = LEGACY_WARM_BIOMES;
      }

   }

   public int apply(Context p_76695_, int p_76696_) {
      int i = (p_76696_ & 3840) >> 8;
      p_76696_ = p_76696_ & -3841;
      if (!Layers.isOcean(p_76696_) && p_76696_ != 14) {
         switch(p_76696_) {
         case 1:
            if (i > 0) {
               return p_76695_.nextRandom(3) == 0 ? 39 : 38;
            }

            return this.warmBiomes[p_76695_.nextRandom(this.warmBiomes.length)];
         case 2:
            if (i > 0) {
               return 21;
            }

            return MEDIUM_BIOMES[p_76695_.nextRandom(MEDIUM_BIOMES.length)];
         case 3:
            if (i > 0) {
               return 32;
            }

            return COLD_BIOMES[p_76695_.nextRandom(COLD_BIOMES.length)];
         case 4:
            return ICE_BIOMES[p_76695_.nextRandom(ICE_BIOMES.length)];
         default:
            return 14;
         }
      } else {
         return p_76696_;
      }
   }
}