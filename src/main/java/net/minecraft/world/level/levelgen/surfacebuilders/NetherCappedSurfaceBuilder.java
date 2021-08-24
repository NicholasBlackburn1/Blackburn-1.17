package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public abstract class NetherCappedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   private long seed;
   private ImmutableMap<BlockState, PerlinNoise> floorNoises = ImmutableMap.of();
   private ImmutableMap<BlockState, PerlinNoise> ceilingNoises = ImmutableMap.of();
   private PerlinNoise patchNoise;

   public NetherCappedSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74989_) {
      super(p_74989_);
   }

   public void apply(Random p_164074_, ChunkAccess p_164075_, Biome p_164076_, int p_164077_, int p_164078_, int p_164079_, double p_164080_, BlockState p_164081_, BlockState p_164082_, int p_164083_, int p_164084_, long p_164085_, SurfaceBuilderBaseConfiguration p_164086_) {
      int i = p_164083_ + 1;
      int j = p_164077_ & 15;
      int k = p_164078_ & 15;
      int l = (int)(p_164080_ / 3.0D + 3.0D + p_164074_.nextDouble() * 0.25D);
      int i1 = (int)(p_164080_ / 3.0D + 3.0D + p_164074_.nextDouble() * 0.25D);
      double d0 = 0.03125D;
      boolean flag = this.patchNoise.getValue((double)p_164077_ * 0.03125D, 109.0D, (double)p_164078_ * 0.03125D) * 75.0D + p_164074_.nextDouble() > 0.0D;
      BlockState blockstate = this.ceilingNoises.entrySet().stream().max(Comparator.comparing((p_75030_) -> {
         return p_75030_.getValue().getValue((double)p_164077_, (double)p_164083_, (double)p_164078_);
      })).get().getKey();
      BlockState blockstate1 = this.floorNoises.entrySet().stream().max(Comparator.comparing((p_74994_) -> {
         return p_74994_.getValue().getValue((double)p_164077_, (double)p_164083_, (double)p_164078_);
      })).get().getKey();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      BlockState blockstate2 = p_164075_.getBlockState(blockpos$mutableblockpos.set(j, 128, k));

      for(int j1 = 127; j1 >= p_164084_; --j1) {
         blockpos$mutableblockpos.set(j, j1, k);
         BlockState blockstate3 = p_164075_.getBlockState(blockpos$mutableblockpos);
         if (blockstate2.is(p_164081_.getBlock()) && (blockstate3.isAir() || blockstate3 == p_164082_)) {
            for(int k1 = 0; k1 < l; ++k1) {
               blockpos$mutableblockpos.move(Direction.UP);
               if (!p_164075_.getBlockState(blockpos$mutableblockpos).is(p_164081_.getBlock())) {
                  break;
               }

               p_164075_.setBlockState(blockpos$mutableblockpos, blockstate, false);
            }

            blockpos$mutableblockpos.set(j, j1, k);
         }

         if ((blockstate2.isAir() || blockstate2 == p_164082_) && blockstate3.is(p_164081_.getBlock())) {
            for(int l1 = 0; l1 < i1 && p_164075_.getBlockState(blockpos$mutableblockpos).is(p_164081_.getBlock()); ++l1) {
               if (flag && j1 >= i - 4 && j1 <= i + 1) {
                  p_164075_.setBlockState(blockpos$mutableblockpos, this.getPatchBlockState(), false);
               } else {
                  p_164075_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
               }

               blockpos$mutableblockpos.move(Direction.DOWN);
            }
         }

         blockstate2 = blockstate3;
      }

   }

   public void initNoise(long p_74996_) {
      if (this.seed != p_74996_ || this.patchNoise == null || this.floorNoises.isEmpty() || this.ceilingNoises.isEmpty()) {
         this.floorNoises = initPerlinNoises(this.getFloorBlockStates(), p_74996_);
         this.ceilingNoises = initPerlinNoises(this.getCeilingBlockStates(), p_74996_ + (long)this.floorNoises.size());
         this.patchNoise = new PerlinNoise(new WorldgenRandom(p_74996_ + (long)this.floorNoises.size() + (long)this.ceilingNoises.size()), ImmutableList.of(0));
      }

      this.seed = p_74996_;
   }

   private static ImmutableMap<BlockState, PerlinNoise> initPerlinNoises(ImmutableList<BlockState> p_74998_, long p_74999_) {
      Builder<BlockState, PerlinNoise> builder = new Builder<>();

      for(BlockState blockstate : p_74998_) {
         builder.put(blockstate, new PerlinNoise(new WorldgenRandom(p_74999_), ImmutableList.of(-4)));
         ++p_74999_;
      }

      return builder.build();
   }

   protected abstract ImmutableList<BlockState> getFloorBlockStates();

   protected abstract ImmutableList<BlockState> getCeilingBlockStates();

   protected abstract BlockState getPatchBlockState();
}