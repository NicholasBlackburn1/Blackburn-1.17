package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
   public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create((p_70161_) -> {
      return trunkPlacerParts(p_70161_).apply(p_70161_, ForkingTrunkPlacer::new);
   });

   public ForkingTrunkPlacer(int p_70148_, int p_70149_, int p_70150_) {
      super(p_70148_, p_70149_, p_70150_);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.FORKING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader p_161828_, BiConsumer<BlockPos, BlockState> p_161829_, Random p_161830_, int p_161831_, BlockPos p_161832_, TreeConfiguration p_161833_) {
      setDirtAt(p_161828_, p_161829_, p_161830_, p_161832_.below(), p_161833_);
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(p_161830_);
      int i = p_161831_ - p_161830_.nextInt(4) - 1;
      int j = 3 - p_161830_.nextInt(3);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int k = p_161832_.getX();
      int l = p_161832_.getZ();
      int i1 = 0;

      for(int j1 = 0; j1 < p_161831_; ++j1) {
         int k1 = p_161832_.getY() + j1;
         if (j1 >= i && j > 0) {
            k += direction.getStepX();
            l += direction.getStepZ();
            --j;
         }

         if (placeLog(p_161828_, p_161829_, p_161830_, blockpos$mutableblockpos.set(k, k1, l), p_161833_)) {
            i1 = k1 + 1;
         }
      }

      list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, i1, l), 1, false));
      k = p_161832_.getX();
      l = p_161832_.getZ();
      Direction direction1 = Direction.Plane.HORIZONTAL.getRandomDirection(p_161830_);
      if (direction1 != direction) {
         int k2 = i - p_161830_.nextInt(2) - 1;
         int l1 = 1 + p_161830_.nextInt(3);
         i1 = 0;

         for(int i2 = k2; i2 < p_161831_ && l1 > 0; --l1) {
            if (i2 >= 1) {
               int j2 = p_161832_.getY() + i2;
               k += direction1.getStepX();
               l += direction1.getStepZ();
               if (placeLog(p_161828_, p_161829_, p_161830_, blockpos$mutableblockpos.set(k, j2, l), p_161833_)) {
                  i1 = j2 + 1;
               }
            }

            ++i2;
         }

         if (i1 > 1) {
            list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, i1, l), 0, false));
         }
      }

      return list;
   }
}