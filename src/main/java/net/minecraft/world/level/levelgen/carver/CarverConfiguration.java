package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
   public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec((p_159101_) -> {
      return p_159101_.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((p_159113_) -> {
         return p_159113_.probability;
      }), HeightProvider.CODEC.fieldOf("y").forGetter((p_159111_) -> {
         return p_159111_.y;
      }), FloatProvider.CODEC.fieldOf("yScale").forGetter((p_159109_) -> {
         return p_159109_.yScale;
      }), VerticalAnchor.CODEC.fieldOf("lava_level").forGetter((p_159107_) -> {
         return p_159107_.lavaLevel;
      }), Codec.BOOL.fieldOf("aquifers_enabled").forGetter((p_159105_) -> {
         return p_159105_.aquifersEnabled;
      }), CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter((p_159103_) -> {
         return p_159103_.debugSettings;
      })).apply(p_159101_, CarverConfiguration::new);
   });
   public final HeightProvider y;
   public final FloatProvider yScale;
   public final VerticalAnchor lavaLevel;
   public final boolean aquifersEnabled;
   public final CarverDebugSettings debugSettings;

   public CarverConfiguration(float p_159094_, HeightProvider p_159095_, FloatProvider p_159096_, VerticalAnchor p_159097_, boolean p_159098_, CarverDebugSettings p_159099_) {
      super(p_159094_);
      this.y = p_159095_;
      this.yScale = p_159096_;
      this.lavaLevel = p_159097_;
      this.aquifersEnabled = p_159098_;
      this.debugSettings = p_159099_;
   }
}