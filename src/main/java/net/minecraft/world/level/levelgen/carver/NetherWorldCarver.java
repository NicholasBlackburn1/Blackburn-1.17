package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
   public NetherWorldCarver(Codec<CaveCarverConfiguration> p_64873_) {
      super(p_64873_);
      this.replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM, Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.BASALT, Blocks.BLACKSTONE);
      this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
   }

   protected int getCaveBound() {
      return 10;
   }

   protected float getThickness(Random p_64893_) {
      return (p_64893_.nextFloat() * 2.0F + p_64893_.nextFloat()) * 2.0F;
   }

   protected double getYScale() {
      return 5.0D;
   }

   protected boolean carveBlock(CarvingContext p_159287_, CaveCarverConfiguration p_159288_, ChunkAccess p_159289_, Function<BlockPos, Biome> p_159290_, BitSet p_159291_, Random p_159292_, BlockPos.MutableBlockPos p_159293_, BlockPos.MutableBlockPos p_159294_, Aquifer p_159295_, MutableBoolean p_159296_) {
      if (this.canReplaceBlock(p_159289_.getBlockState(p_159293_))) {
         BlockState blockstate;
         if (p_159293_.getY() <= p_159287_.getMinGenY() + 31) {
            blockstate = LAVA.createLegacyBlock();
         } else {
            blockstate = CAVE_AIR;
         }

         p_159289_.setBlockState(p_159293_, blockstate, false);
         return true;
      } else {
         return false;
      }
   }
}