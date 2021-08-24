package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlainFlowerProvider extends BlockStateProvider {
   public static final Codec<PlainFlowerProvider> CODEC;
   public static final PlainFlowerProvider INSTANCE = new PlainFlowerProvider();
   private static final BlockState[] LOW_NOISE_FLOWERS = new BlockState[]{Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()};
   private static final BlockState[] HIGH_NOISE_FLOWERS = new BlockState[]{Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()};

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.PLAIN_FLOWER_PROVIDER;
   }

   public BlockState getState(Random p_68783_, BlockPos p_68784_) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)p_68784_.getX() / 200.0D, (double)p_68784_.getZ() / 200.0D, false);
      if (d0 < -0.8D) {
         return Util.getRandom(LOW_NOISE_FLOWERS, p_68783_);
      } else {
         return p_68783_.nextInt(3) > 0 ? Util.getRandom(HIGH_NOISE_FLOWERS, p_68783_) : Blocks.DANDELION.defaultBlockState();
      }
   }

   static {
      CODEC = Codec.unit(() -> {
         return INSTANCE;
      });
   }
}