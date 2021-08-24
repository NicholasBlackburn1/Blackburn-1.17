package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class GiantTreeTaigaSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public GiantTreeTaigaSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74901_) {
      super(p_74901_);
   }

   public void apply(Random p_163990_, ChunkAccess p_163991_, Biome p_163992_, int p_163993_, int p_163994_, int p_163995_, double p_163996_, BlockState p_163997_, BlockState p_163998_, int p_163999_, int p_164000_, long p_164001_, SurfaceBuilderBaseConfiguration p_164002_) {
      if (p_163996_ > 1.75D) {
         SurfaceBuilder.DEFAULT.apply(p_163990_, p_163991_, p_163992_, p_163993_, p_163994_, p_163995_, p_163996_, p_163997_, p_163998_, p_163999_, p_164000_, p_164001_, SurfaceBuilder.CONFIG_COARSE_DIRT);
      } else if (p_163996_ > -0.95D) {
         SurfaceBuilder.DEFAULT.apply(p_163990_, p_163991_, p_163992_, p_163993_, p_163994_, p_163995_, p_163996_, p_163997_, p_163998_, p_163999_, p_164000_, p_164001_, SurfaceBuilder.CONFIG_PODZOL);
      } else {
         SurfaceBuilder.DEFAULT.apply(p_163990_, p_163991_, p_163992_, p_163993_, p_163994_, p_163995_, p_163996_, p_163997_, p_163998_, p_163999_, p_164000_, p_164001_, SurfaceBuilder.CONFIG_GRASS);
      }

   }
}