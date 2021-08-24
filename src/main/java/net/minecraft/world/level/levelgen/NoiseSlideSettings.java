package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public class NoiseSlideSettings {
   public static final Codec<NoiseSlideSettings> CODEC = RecordCodecBuilder.create((p_64559_) -> {
      return p_64559_.group(Codec.INT.fieldOf("target").forGetter(NoiseSlideSettings::target), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(NoiseSlideSettings::size), Codec.INT.fieldOf("offset").forGetter(NoiseSlideSettings::offset)).apply(p_64559_, NoiseSlideSettings::new);
   });
   private final int target;
   private final int size;
   private final int offset;

   public NoiseSlideSettings(int p_64554_, int p_64555_, int p_64556_) {
      this.target = p_64554_;
      this.size = p_64555_;
      this.offset = p_64556_;
   }

   public int target() {
      return this.target;
   }

   public int size() {
      return this.size;
   }

   public int offset() {
      return this.offset;
   }
}