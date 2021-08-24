package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCanyonWorldCarver extends CanyonWorldCarver {
   public UnderwaterCanyonWorldCarver(Codec<CanyonCarverConfiguration> p_64903_) {
      super(p_64903_);
      this.replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.SAND, Blocks.GRAVEL, Blocks.WATER, Blocks.LAVA, Blocks.OBSIDIAN, Blocks.AIR, Blocks.CAVE_AIR);
   }

   protected boolean hasDisallowedLiquid(ChunkAccess p_159298_, int p_159299_, int p_159300_, int p_159301_, int p_159302_, int p_159303_, int p_159304_) {
      return false;
   }

   protected boolean carveBlock(CarvingContext p_159306_, CanyonCarverConfiguration p_159307_, ChunkAccess p_159308_, Function<BlockPos, Biome> p_159309_, BitSet p_159310_, Random p_159311_, BlockPos.MutableBlockPos p_159312_, BlockPos.MutableBlockPos p_159313_, Aquifer p_159314_, MutableBoolean p_159315_) {
      return UnderwaterCaveWorldCarver.carveBlock(this, p_159308_, p_159311_, p_159312_, p_159313_, p_159314_);
   }
}