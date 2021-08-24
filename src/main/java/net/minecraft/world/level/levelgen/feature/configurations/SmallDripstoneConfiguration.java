package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SmallDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<SmallDripstoneConfiguration> CODEC = RecordCodecBuilder.create((p_161181_) -> {
      return p_161181_.group(Codec.intRange(0, 100).fieldOf("max_placements").orElse(5).forGetter((p_161189_) -> {
         return p_161189_.maxPlacements;
      }), Codec.intRange(0, 20).fieldOf("empty_space_search_radius").orElse(10).forGetter((p_161187_) -> {
         return p_161187_.emptySpaceSearchRadius;
      }), Codec.intRange(0, 20).fieldOf("max_offset_from_origin").orElse(2).forGetter((p_161185_) -> {
         return p_161185_.maxOffsetFromOrigin;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter((p_161183_) -> {
         return p_161183_.chanceOfTallerDripstone;
      })).apply(p_161181_, SmallDripstoneConfiguration::new);
   });
   public final int maxPlacements;
   public final int emptySpaceSearchRadius;
   public final int maxOffsetFromOrigin;
   public final float chanceOfTallerDripstone;

   public SmallDripstoneConfiguration(int p_161176_, int p_161177_, int p_161178_, float p_161179_) {
      this.maxPlacements = p_161176_;
      this.emptySpaceSearchRadius = p_161177_;
      this.maxOffsetFromOrigin = p_161178_;
      this.chanceOfTallerDripstone = p_161179_;
   }
}