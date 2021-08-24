package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WeightedConfiguredFeature {
   public static final Codec<WeightedConfiguredFeature> CODEC = RecordCodecBuilder.create((p_67423_) -> {
      return p_67423_.group(ConfiguredFeature.CODEC.fieldOf("feature").flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter((p_160665_) -> {
         return p_160665_.feature;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter((p_160663_) -> {
         return p_160663_.chance;
      })).apply(p_67423_, WeightedConfiguredFeature::new);
   });
   public final Supplier<ConfiguredFeature<?, ?>> feature;
   public final float chance;

   public WeightedConfiguredFeature(ConfiguredFeature<?, ?> p_67408_, float p_67409_) {
      this(() -> {
         return p_67408_;
      }, p_67409_);
   }

   private WeightedConfiguredFeature(Supplier<ConfiguredFeature<?, ?>> p_67411_, float p_67412_) {
      this.feature = p_67411_;
      this.chance = p_67412_;
   }

   public boolean place(WorldGenLevel p_67414_, ChunkGenerator p_67415_, Random p_67416_, BlockPos p_67417_) {
      return this.feature.get().place(p_67414_, p_67415_, p_67416_, p_67417_);
   }
}