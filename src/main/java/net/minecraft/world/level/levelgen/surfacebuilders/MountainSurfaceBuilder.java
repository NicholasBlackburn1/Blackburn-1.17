package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class MountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public MountainSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74957_) {
      super(p_74957_);
   }

   public void apply(Random p_164046_, ChunkAccess p_164047_, Biome p_164048_, int p_164049_, int p_164050_, int p_164051_, double p_164052_, BlockState p_164053_, BlockState p_164054_, int p_164055_, int p_164056_, long p_164057_, SurfaceBuilderBaseConfiguration p_164058_) {
      if (p_164052_ > 1.0D) {
         SurfaceBuilder.DEFAULT.apply(p_164046_, p_164047_, p_164048_, p_164049_, p_164050_, p_164051_, p_164052_, p_164053_, p_164054_, p_164055_, p_164056_, p_164057_, SurfaceBuilder.CONFIG_STONE);
      } else {
         SurfaceBuilder.DEFAULT.apply(p_164046_, p_164047_, p_164048_, p_164049_, p_164050_, p_164051_, p_164052_, p_164053_, p_164054_, p_164055_, p_164056_, p_164057_, SurfaceBuilder.CONFIG_GRASS);
      }

   }
}