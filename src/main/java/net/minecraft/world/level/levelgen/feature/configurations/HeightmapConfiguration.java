package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapConfiguration implements DecoratorConfiguration {
   public static final Codec<HeightmapConfiguration> CODEC = RecordCodecBuilder.create((p_160935_) -> {
      return p_160935_.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((p_160937_) -> {
         return p_160937_.heightmap;
      })).apply(p_160935_, HeightmapConfiguration::new);
   });
   public final Heightmap.Types heightmap;

   public HeightmapConfiguration(Heightmap.Types p_160933_) {
      this.heightmap = p_160933_;
   }
}