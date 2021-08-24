package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
   public UnderwaterCaveWorldCarver(Codec<CaveCarverConfiguration> p_64932_) {
      super(p_64932_);
      this.replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.SAND, Blocks.GRAVEL, Blocks.WATER, Blocks.LAVA, Blocks.OBSIDIAN, Blocks.PACKED_ICE);
   }

   protected boolean hasDisallowedLiquid(ChunkAccess p_159328_, int p_159329_, int p_159330_, int p_159331_, int p_159332_, int p_159333_, int p_159334_) {
      return false;
   }

   protected boolean carveBlock(CarvingContext p_159347_, CaveCarverConfiguration p_159348_, ChunkAccess p_159349_, Function<BlockPos, Biome> p_159350_, BitSet p_159351_, Random p_159352_, BlockPos.MutableBlockPos p_159353_, BlockPos.MutableBlockPos p_159354_, Aquifer p_159355_, MutableBoolean p_159356_) {
      return carveBlock(this, p_159349_, p_159352_, p_159353_, p_159354_, p_159355_);
   }

   protected static boolean carveBlock(WorldCarver<?> p_159358_, ChunkAccess p_159359_, Random p_159360_, BlockPos.MutableBlockPos p_159361_, BlockPos.MutableBlockPos p_159362_, Aquifer p_159363_) {
      if (p_159363_.computeState(WorldCarver.STONE_SOURCE, p_159361_.getX(), p_159361_.getY(), p_159361_.getZ(), Double.NEGATIVE_INFINITY).isAir()) {
         return false;
      } else {
         BlockState blockstate = p_159359_.getBlockState(p_159361_);
         if (!p_159358_.canReplaceBlock(blockstate)) {
            return false;
         } else if (p_159361_.getY() == 10) {
            float f = p_159360_.nextFloat();
            if ((double)f < 0.25D) {
               p_159359_.setBlockState(p_159361_, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
               p_159359_.getBlockTicks().scheduleTick(p_159361_, Blocks.MAGMA_BLOCK, 0);
            } else {
               p_159359_.setBlockState(p_159361_, Blocks.OBSIDIAN.defaultBlockState(), false);
            }

            return true;
         } else if (p_159361_.getY() < 10) {
            p_159359_.setBlockState(p_159361_, Blocks.LAVA.defaultBlockState(), false);
            return false;
         } else {
            p_159359_.setBlockState(p_159361_, WATER.createLegacyBlock(), false);
            int i = p_159359_.getPos().x;
            int j = p_159359_.getPos().z;

            for(Direction direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
               p_159362_.setWithOffset(p_159361_, direction);
               if (SectionPos.blockToSectionCoord(p_159362_.getX()) != i || SectionPos.blockToSectionCoord(p_159362_.getZ()) != j || p_159359_.getBlockState(p_159362_).isAir()) {
                  p_159359_.getLiquidTicks().scheduleTick(p_159361_, WATER.getType(), 0);
                  break;
               }
            }

            return true;
         }
      }
   }
}