package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

public enum OceanLayer implements AreaTransformer0 {
   INSTANCE;

   public int applyPixel(Context p_76784_, int p_76785_, int p_76786_) {
      ImprovedNoise improvednoise = p_76784_.getBiomeNoise();
      double d0 = improvednoise.noise((double)p_76785_ / 8.0D, (double)p_76786_ / 8.0D, 0.0D);
      if (d0 > 0.4D) {
         return 44;
      } else if (d0 > 0.2D) {
         return 45;
      } else if (d0 < -0.4D) {
         return 10;
      } else {
         return d0 < -0.2D ? 46 : 0;
      }
   }
}