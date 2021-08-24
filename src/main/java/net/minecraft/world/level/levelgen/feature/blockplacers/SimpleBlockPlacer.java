package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer {
   public static final Codec<SimpleBlockPlacer> CODEC;
   public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

   protected BlockPlacerType<?> type() {
      return BlockPlacerType.SIMPLE_BLOCK_PLACER;
   }

   public void place(LevelAccessor p_67534_, BlockPos p_67535_, BlockState p_67536_, Random p_67537_) {
      p_67534_.setBlock(p_67535_, p_67536_, 2);
   }

   static {
      CODEC = Codec.unit(() -> {
         return INSTANCE;
      });
   }
}