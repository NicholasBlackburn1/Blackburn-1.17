package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
   public RandomPatchFeature(Codec<RandomPatchConfiguration> p_66605_) {
      super(p_66605_);
   }

   public boolean place(FeaturePlaceContext<RandomPatchConfiguration> p_160210_) {
      RandomPatchConfiguration randompatchconfiguration = p_160210_.config();
      Random random = p_160210_.random();
      BlockPos blockpos = p_160210_.origin();
      WorldGenLevel worldgenlevel = p_160210_.level();
      BlockState blockstate = randompatchconfiguration.stateProvider.getState(random, blockpos);
      BlockPos blockpos1;
      if (randompatchconfiguration.project) {
         blockpos1 = worldgenlevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockpos);
      } else {
         blockpos1 = blockpos;
      }

      int i = 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < randompatchconfiguration.tries; ++j) {
         blockpos$mutableblockpos.setWithOffset(blockpos1, random.nextInt(randompatchconfiguration.xspread + 1) - random.nextInt(randompatchconfiguration.xspread + 1), random.nextInt(randompatchconfiguration.yspread + 1) - random.nextInt(randompatchconfiguration.yspread + 1), random.nextInt(randompatchconfiguration.zspread + 1) - random.nextInt(randompatchconfiguration.zspread + 1));
         BlockPos blockpos2 = blockpos$mutableblockpos.below();
         BlockState blockstate1 = worldgenlevel.getBlockState(blockpos2);
         if ((worldgenlevel.isEmptyBlock(blockpos$mutableblockpos) || randompatchconfiguration.canReplace && worldgenlevel.getBlockState(blockpos$mutableblockpos).getMaterial().isReplaceable()) && blockstate.canSurvive(worldgenlevel, blockpos$mutableblockpos) && (randompatchconfiguration.whitelist.isEmpty() || randompatchconfiguration.whitelist.contains(blockstate1.getBlock())) && !randompatchconfiguration.blacklist.contains(blockstate1) && (!randompatchconfiguration.needWater || worldgenlevel.getFluidState(blockpos2.west()).is(FluidTags.WATER) || worldgenlevel.getFluidState(blockpos2.east()).is(FluidTags.WATER) || worldgenlevel.getFluidState(blockpos2.north()).is(FluidTags.WATER) || worldgenlevel.getFluidState(blockpos2.south()).is(FluidTags.WATER))) {
            randompatchconfiguration.blockPlacer.place(worldgenlevel, blockpos$mutableblockpos, blockstate, random);
            ++i;
         }
      }

      return i > 0;
   }
}