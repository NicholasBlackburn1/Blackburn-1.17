package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ShatteredSavanaSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public ShatteredSavanaSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75130_) {
      super(p_75130_);
   }

   public void apply(Random p_164186_, ChunkAccess p_164187_, Biome p_164188_, int p_164189_, int p_164190_, int p_164191_, double p_164192_, BlockState p_164193_, BlockState p_164194_, int p_164195_, int p_164196_, long p_164197_, SurfaceBuilderBaseConfiguration p_164198_) {
      if (p_164192_ > 1.75D) {
         SurfaceBuilder.DEFAULT.apply(p_164186_, p_164187_, p_164188_, p_164189_, p_164190_, p_164191_, p_164192_, p_164193_, p_164194_, p_164195_, p_164196_, p_164197_, SurfaceBuilder.CONFIG_STONE);
      } else if (p_164192_ > -0.5D) {
         SurfaceBuilder.DEFAULT.apply(p_164186_, p_164187_, p_164188_, p_164189_, p_164190_, p_164191_, p_164192_, p_164193_, p_164194_, p_164195_, p_164196_, p_164197_, SurfaceBuilder.CONFIG_COARSE_DIRT);
      } else {
         SurfaceBuilder.DEFAULT.apply(p_164186_, p_164187_, p_164188_, p_164189_, p_164190_, p_164191_, p_164192_, p_164193_, p_164194_, p_164195_, p_164196_, p_164197_, SurfaceBuilder.CONFIG_GRASS);
      }

   }
}