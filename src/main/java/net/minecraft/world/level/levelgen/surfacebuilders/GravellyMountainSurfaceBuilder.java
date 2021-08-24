package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class GravellyMountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public GravellyMountainSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74929_) {
      super(p_74929_);
   }

   public void apply(Random p_164018_, ChunkAccess p_164019_, Biome p_164020_, int p_164021_, int p_164022_, int p_164023_, double p_164024_, BlockState p_164025_, BlockState p_164026_, int p_164027_, int p_164028_, long p_164029_, SurfaceBuilderBaseConfiguration p_164030_) {
      if (!(p_164024_ < -1.0D) && !(p_164024_ > 2.0D)) {
         if (p_164024_ > 1.0D) {
            SurfaceBuilder.DEFAULT.apply(p_164018_, p_164019_, p_164020_, p_164021_, p_164022_, p_164023_, p_164024_, p_164025_, p_164026_, p_164027_, p_164028_, p_164029_, SurfaceBuilder.CONFIG_STONE);
         } else {
            SurfaceBuilder.DEFAULT.apply(p_164018_, p_164019_, p_164020_, p_164021_, p_164022_, p_164023_, p_164024_, p_164025_, p_164026_, p_164027_, p_164028_, p_164029_, SurfaceBuilder.CONFIG_GRASS);
         }
      } else {
         SurfaceBuilder.DEFAULT.apply(p_164018_, p_164019_, p_164020_, p_164021_, p_164022_, p_164023_, p_164024_, p_164025_, p_164026_, p_164027_, p_164028_, p_164029_, SurfaceBuilder.CONFIG_GRAVEL);
      }

   }
}