package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;

public class NoiseSettings {
   public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.<NoiseSettings>create((p_64536_) -> {
      return p_64536_.group(Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY), Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height), NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings), NoiseSlideSettings.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings), NoiseSlideSettings.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings), Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal), Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical), Codec.DOUBLE.fieldOf("density_factor").forGetter(NoiseSettings::densityFactor), Codec.DOUBLE.fieldOf("density_offset").forGetter(NoiseSettings::densityOffset), Codec.BOOL.fieldOf("simplex_surface_noise").forGetter(NoiseSettings::useSimplexSurfaceNoise), Codec.BOOL.optionalFieldOf("random_density_offset", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::randomDensityOffset), Codec.BOOL.optionalFieldOf("island_noise_override", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::islandNoiseOverride), Codec.BOOL.optionalFieldOf("amplified", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified)).apply(p_64536_, NoiseSettings::new);
   }).comapFlatMap(NoiseSettings::guardY, Function.identity());
   private final int minY;
   private final int height;
   private final NoiseSamplingSettings noiseSamplingSettings;
   private final NoiseSlideSettings topSlideSettings;
   private final NoiseSlideSettings bottomSlideSettings;
   private final int noiseSizeHorizontal;
   private final int noiseSizeVertical;
   private final double densityFactor;
   private final double densityOffset;
   private final boolean useSimplexSurfaceNoise;
   private final boolean randomDensityOffset;
   private final boolean islandNoiseOverride;
   private final boolean isAmplified;

   private static DataResult<NoiseSettings> guardY(NoiseSettings p_158721_) {
      if (p_158721_.minY() + p_158721_.height() > DimensionType.MAX_Y + 1) {
         return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
      } else if (p_158721_.height() % 16 != 0) {
         return DataResult.error("height has to be a multiple of 16");
      } else {
         return p_158721_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_158721_);
      }
   }

   private NoiseSettings(int p_158690_, int p_158691_, NoiseSamplingSettings p_158692_, NoiseSlideSettings p_158693_, NoiseSlideSettings p_158694_, int p_158695_, int p_158696_, double p_158697_, double p_158698_, boolean p_158699_, boolean p_158700_, boolean p_158701_, boolean p_158702_) {
      this.minY = p_158690_;
      this.height = p_158691_;
      this.noiseSamplingSettings = p_158692_;
      this.topSlideSettings = p_158693_;
      this.bottomSlideSettings = p_158694_;
      this.noiseSizeHorizontal = p_158695_;
      this.noiseSizeVertical = p_158696_;
      this.densityFactor = p_158697_;
      this.densityOffset = p_158698_;
      this.useSimplexSurfaceNoise = p_158699_;
      this.randomDensityOffset = p_158700_;
      this.islandNoiseOverride = p_158701_;
      this.isAmplified = p_158702_;
   }

   public static NoiseSettings create(int p_158705_, int p_158706_, NoiseSamplingSettings p_158707_, NoiseSlideSettings p_158708_, NoiseSlideSettings p_158709_, int p_158710_, int p_158711_, double p_158712_, double p_158713_, boolean p_158714_, boolean p_158715_, boolean p_158716_, boolean p_158717_) {
      NoiseSettings noisesettings = new NoiseSettings(p_158705_, p_158706_, p_158707_, p_158708_, p_158709_, p_158710_, p_158711_, p_158712_, p_158713_, p_158714_, p_158715_, p_158716_, p_158717_);
      guardY(noisesettings).error().ifPresent((p_158719_) -> {
         throw new IllegalStateException(p_158719_.message());
      });
      return noisesettings;
   }

   public int minY() {
      return this.minY;
   }

   public int height() {
      return this.height;
   }

   public NoiseSamplingSettings noiseSamplingSettings() {
      return this.noiseSamplingSettings;
   }

   public NoiseSlideSettings topSlideSettings() {
      return this.topSlideSettings;
   }

   public NoiseSlideSettings bottomSlideSettings() {
      return this.bottomSlideSettings;
   }

   public int noiseSizeHorizontal() {
      return this.noiseSizeHorizontal;
   }

   public int noiseSizeVertical() {
      return this.noiseSizeVertical;
   }

   public double densityFactor() {
      return this.densityFactor;
   }

   public double densityOffset() {
      return this.densityOffset;
   }

   @Deprecated
   public boolean useSimplexSurfaceNoise() {
      return this.useSimplexSurfaceNoise;
   }

   @Deprecated
   public boolean randomDensityOffset() {
      return this.randomDensityOffset;
   }

   @Deprecated
   public boolean islandNoiseOverride() {
      return this.islandNoiseOverride;
   }

   @Deprecated
   public boolean isAmplified() {
      return this.isAmplified;
   }
}
