package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration extends CarverConfiguration {
   public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create((p_159184_) -> {
      return p_159184_.group(CarverConfiguration.CODEC.forGetter((p_159192_) -> {
         return p_159192_;
      }), FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter((p_159190_) -> {
         return p_159190_.horizontalRadiusMultiplier;
      }), FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter((p_159188_) -> {
         return p_159188_.verticalRadiusMultiplier;
      }), FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter((p_159186_) -> {
         return p_159186_.floorLevel;
      })).apply(p_159184_, CaveCarverConfiguration::new);
   });
   public final FloatProvider horizontalRadiusMultiplier;
   public final FloatProvider verticalRadiusMultiplier;
   final FloatProvider floorLevel;

   public CaveCarverConfiguration(float p_159169_, HeightProvider p_159170_, FloatProvider p_159171_, VerticalAnchor p_159172_, boolean p_159173_, CarverDebugSettings p_159174_, FloatProvider p_159175_, FloatProvider p_159176_, FloatProvider p_159177_) {
      super(p_159169_, p_159170_, p_159171_, p_159172_, p_159173_, p_159174_);
      this.horizontalRadiusMultiplier = p_159175_;
      this.verticalRadiusMultiplier = p_159176_;
      this.floorLevel = p_159177_;
   }

   public CaveCarverConfiguration(float p_159160_, HeightProvider p_159161_, FloatProvider p_159162_, VerticalAnchor p_159163_, boolean p_159164_, FloatProvider p_159165_, FloatProvider p_159166_, FloatProvider p_159167_) {
      this(p_159160_, p_159161_, p_159162_, p_159163_, p_159164_, CarverDebugSettings.DEFAULT, p_159165_, p_159166_, p_159167_);
   }

   public CaveCarverConfiguration(CarverConfiguration p_159179_, FloatProvider p_159180_, FloatProvider p_159181_, FloatProvider p_159182_) {
      this(p_159179_.probability, p_159179_.y, p_159179_.yScale, p_159179_.lavaLevel, p_159179_.aquifersEnabled, p_159179_.debugSettings, p_159180_, p_159181_, p_159182_);
   }
}