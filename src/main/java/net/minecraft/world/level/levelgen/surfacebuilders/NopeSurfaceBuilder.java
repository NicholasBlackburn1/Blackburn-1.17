package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class NopeSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public NopeSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75102_) {
      super(p_75102_);
   }

   public void apply(Random p_164158_, ChunkAccess p_164159_, Biome p_164160_, int p_164161_, int p_164162_, int p_164163_, double p_164164_, BlockState p_164165_, BlockState p_164166_, int p_164167_, int p_164168_, long p_164169_, SurfaceBuilderBaseConfiguration p_164170_) {
   }
}