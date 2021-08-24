package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.GrowingPlantConfiguration;

public class GrowingPlantFeature extends Feature<GrowingPlantConfiguration> {
   public GrowingPlantFeature(Codec<GrowingPlantConfiguration> p_159863_) {
      super(p_159863_);
   }

   public boolean place(FeaturePlaceContext<GrowingPlantConfiguration> p_159865_) {
      LevelAccessor levelaccessor = p_159865_.level();
      GrowingPlantConfiguration growingplantconfiguration = p_159865_.config();
      Random random = p_159865_.random();
      int i = growingplantconfiguration.heightDistribution.getRandomValue(random).orElseThrow(IllegalStateException::new).sample(random);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_159865_.origin().mutable();
      BlockPos.MutableBlockPos blockpos$mutableblockpos1 = blockpos$mutableblockpos.mutable().move(growingplantconfiguration.direction);
      BlockState blockstate = levelaccessor.getBlockState(blockpos$mutableblockpos);

      for(int j = 1; j <= i; ++j) {
         BlockState blockstate1 = blockstate;
         blockstate = levelaccessor.getBlockState(blockpos$mutableblockpos1);
         if (blockstate1.isAir() || growingplantconfiguration.allowWater && blockstate1.getFluidState().is(FluidTags.WATER)) {
            if (j == i || !blockstate.isAir()) {
               levelaccessor.setBlock(blockpos$mutableblockpos, growingplantconfiguration.headProvider.getState(random, blockpos$mutableblockpos), 2);
               break;
            }

            levelaccessor.setBlock(blockpos$mutableblockpos, growingplantconfiguration.bodyProvider.getState(random, blockpos$mutableblockpos), 2);
         }

         blockpos$mutableblockpos1.move(growingplantconfiguration.direction);
         blockpos$mutableblockpos.move(growingplantconfiguration.direction);
      }

      return true;
   }
}