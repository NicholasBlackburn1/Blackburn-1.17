package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CaveDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<CaveDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_162087_) -> {
      return p_162087_.group(CaveSurface.CODEC.fieldOf("surface").forGetter((p_162091_) -> {
         return p_162091_.surface;
      }), Codec.INT.fieldOf("floor_to_ceiling_search_range").forGetter((p_162089_) -> {
         return p_162089_.floorToCeilingSearchRange;
      })).apply(p_162087_, CaveDecoratorConfiguration::new);
   });
   public final CaveSurface surface;
   public final int floorToCeilingSearchRange;

   public CaveDecoratorConfiguration(CaveSurface p_162084_, int p_162085_) {
      this.surface = p_162084_;
      this.floorToCeilingSearchRange = p_162085_;
   }
}