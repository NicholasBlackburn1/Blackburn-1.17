package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.SingleBaseStoneSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
   public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
   public static final WorldCarver<CanyonCarverConfiguration> UNDERWATER_CANYON = register("underwater_canyon", new UnderwaterCanyonWorldCarver(CanyonCarverConfiguration.CODEC));
   public static final WorldCarver<CaveCarverConfiguration> UNDERWATER_CAVE = register("underwater_cave", new UnderwaterCaveWorldCarver(CaveCarverConfiguration.CODEC));
   protected static final BaseStoneSource STONE_SOURCE = new SingleBaseStoneSource(Blocks.STONE.defaultBlockState());
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
   protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
   protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.GRANITE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK);
   protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
   private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

   private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String p_65066_, F p_65067_) {
      return Registry.register(Registry.CARVER, p_65066_, p_65067_);
   }

   public WorldCarver(Codec<C> p_159366_) {
      this.configuredCodec = p_159366_.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config).codec();
   }

   public ConfiguredWorldCarver<C> configured(C p_65064_) {
      return new ConfiguredWorldCarver<>(this, p_65064_);
   }

   public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
      return this.configuredCodec;
   }

   public int getRange() {
      return 4;
   }

   protected boolean carveEllipsoid(CarvingContext p_159387_, C p_159388_, ChunkAccess p_159389_, Function<BlockPos, Biome> p_159390_, long p_159391_, Aquifer p_159392_, double p_159393_, double p_159394_, double p_159395_, double p_159396_, double p_159397_, BitSet p_159398_, WorldCarver.CarveSkipChecker p_159399_) {
      ChunkPos chunkpos = p_159389_.getPos();
      int i = chunkpos.x;
      int j = chunkpos.z;
      Random random = new Random(p_159391_ + (long)i + (long)j);
      double d0 = (double)chunkpos.getMiddleBlockX();
      double d1 = (double)chunkpos.getMiddleBlockZ();
      double d2 = 16.0D + p_159396_ * 2.0D;
      if (!(Math.abs(p_159393_ - d0) > d2) && !(Math.abs(p_159395_ - d1) > d2)) {
         int k = chunkpos.getMinBlockX();
         int l = chunkpos.getMinBlockZ();
         int i1 = Math.max(Mth.floor(p_159393_ - p_159396_) - k - 1, 0);
         int j1 = Math.min(Mth.floor(p_159393_ + p_159396_) - k, 15);
         int k1 = Math.max(Mth.floor(p_159394_ - p_159397_) - 1, p_159387_.getMinGenY() + 1);
         int l1 = Math.min(Mth.floor(p_159394_ + p_159397_) + 1, p_159387_.getMinGenY() + p_159387_.getGenDepth() - 8);
         int i2 = Math.max(Mth.floor(p_159395_ - p_159396_) - l - 1, 0);
         int j2 = Math.min(Mth.floor(p_159395_ + p_159396_) - l, 15);
         if (!p_159388_.aquifersEnabled && this.hasDisallowedLiquid(p_159389_, i1, j1, k1, l1, i2, j2)) {
            return false;
         } else {
            boolean flag = false;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

            for(int k2 = i1; k2 <= j1; ++k2) {
               int l2 = chunkpos.getBlockX(k2);
               double d3 = ((double)l2 + 0.5D - p_159393_) / p_159396_;

               for(int i3 = i2; i3 <= j2; ++i3) {
                  int j3 = chunkpos.getBlockZ(i3);
                  double d4 = ((double)j3 + 0.5D - p_159395_) / p_159396_;
                  if (!(d3 * d3 + d4 * d4 >= 1.0D)) {
                     MutableBoolean mutableboolean = new MutableBoolean(false);

                     for(int k3 = l1; k3 > k1; --k3) {
                        double d5 = ((double)k3 - 0.5D - p_159394_) / p_159397_;
                        if (!p_159399_.shouldSkip(p_159387_, d3, d5, d4, k3)) {
                           int l3 = k3 - p_159387_.getMinGenY();
                           int i4 = k2 | i3 << 4 | l3 << 8;
                           if (!p_159398_.get(i4) || isDebugEnabled(p_159388_)) {
                              p_159398_.set(i4);
                              blockpos$mutableblockpos.set(l2, k3, j3);
                              flag |= this.carveBlock(p_159387_, p_159388_, p_159389_, p_159390_, p_159398_, random, blockpos$mutableblockpos, blockpos$mutableblockpos1, p_159392_, mutableboolean);
                           }
                        }
                     }
                  }
               }
            }

            return flag;
         }
      } else {
         return false;
      }
   }

   protected boolean carveBlock(CarvingContext p_159400_, C p_159401_, ChunkAccess p_159402_, Function<BlockPos, Biome> p_159403_, BitSet p_159404_, Random p_159405_, BlockPos.MutableBlockPos p_159406_, BlockPos.MutableBlockPos p_159407_, Aquifer p_159408_, MutableBoolean p_159409_) {
      BlockState blockstate = p_159402_.getBlockState(p_159406_);
      BlockState blockstate1 = p_159402_.getBlockState(p_159407_.setWithOffset(p_159406_, Direction.UP));
      if (blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(Blocks.MYCELIUM)) {
         p_159409_.setTrue();
      }

      if (!this.canReplaceBlock(blockstate, blockstate1) && !isDebugEnabled(p_159401_)) {
         return false;
      } else {
         BlockState blockstate2 = this.getCarveState(p_159400_, p_159401_, p_159406_, p_159408_);
         if (blockstate2 == null) {
            return false;
         } else {
            p_159402_.setBlockState(p_159406_, blockstate2, false);
            if (p_159409_.isTrue()) {
               p_159407_.setWithOffset(p_159406_, Direction.DOWN);
               if (p_159402_.getBlockState(p_159407_).is(Blocks.DIRT)) {
                  p_159402_.setBlockState(p_159407_, p_159403_.apply(p_159406_).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false);
               }
            }

            return true;
         }
      }
   }

   @Nullable
   private BlockState getCarveState(CarvingContext p_159419_, C p_159420_, BlockPos p_159421_, Aquifer p_159422_) {
      if (p_159421_.getY() <= p_159420_.lavaLevel.resolveY(p_159419_)) {
         return LAVA.createLegacyBlock();
      } else if (!p_159420_.aquifersEnabled) {
         return isDebugEnabled(p_159420_) ? getDebugState(p_159420_, AIR) : AIR;
      } else {
         BlockState blockstate = p_159422_.computeState(STONE_SOURCE, p_159421_.getX(), p_159421_.getY(), p_159421_.getZ(), 0.0D);
         if (blockstate == Blocks.STONE.defaultBlockState()) {
            return isDebugEnabled(p_159420_) ? p_159420_.debugSettings.getBarrierState() : null;
         } else {
            return isDebugEnabled(p_159420_) ? getDebugState(p_159420_, blockstate) : blockstate;
         }
      }
   }

   private static BlockState getDebugState(CarverConfiguration p_159382_, BlockState p_159383_) {
      if (p_159383_.is(Blocks.AIR)) {
         return p_159382_.debugSettings.getAirState();
      } else if (p_159383_.is(Blocks.WATER)) {
         BlockState blockstate = p_159382_.debugSettings.getWaterState();
         return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : blockstate;
      } else {
         return p_159383_.is(Blocks.LAVA) ? p_159382_.debugSettings.getLavaState() : p_159383_;
      }
   }

   public abstract boolean carve(CarvingContext p_159410_, C p_159411_, ChunkAccess p_159412_, Function<BlockPos, Biome> p_159413_, Random p_159414_, Aquifer p_159415_, ChunkPos p_159416_, BitSet p_159417_);

   public abstract boolean isStartChunk(C p_159384_, Random p_159385_);

   protected boolean canReplaceBlock(BlockState p_65011_) {
      return this.replaceableBlocks.contains(p_65011_.getBlock());
   }

   protected boolean canReplaceBlock(BlockState p_65013_, BlockState p_65014_) {
      return this.canReplaceBlock(p_65013_) || (p_65013_.is(Blocks.SAND) || p_65013_.is(Blocks.GRAVEL)) && !p_65014_.getFluidState().is(FluidTags.WATER);
   }

   protected boolean hasDisallowedLiquid(ChunkAccess p_159374_, int p_159375_, int p_159376_, int p_159377_, int p_159378_, int p_159379_, int p_159380_) {
      ChunkPos chunkpos = p_159374_.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k = p_159375_; k <= p_159376_; ++k) {
         for(int l = p_159379_; l <= p_159380_; ++l) {
            for(int i1 = p_159377_ - 1; i1 <= p_159378_ + 1; ++i1) {
               blockpos$mutableblockpos.set(i + k, i1, j + l);
               if (this.liquids.contains(p_159374_.getFluidState(blockpos$mutableblockpos).getType())) {
                  return true;
               }

               if (i1 != p_159378_ + 1 && !isEdge(k, l, p_159375_, p_159376_, p_159379_, p_159380_)) {
                  i1 = p_159378_;
               }
            }
         }
      }

      return false;
   }

   private static boolean isEdge(int p_65004_, int p_65005_, int p_65006_, int p_65007_, int p_65008_, int p_65009_) {
      return p_65004_ == p_65006_ || p_65004_ == p_65007_ || p_65005_ == p_65008_ || p_65005_ == p_65009_;
   }

   protected static boolean canReach(ChunkPos p_159368_, double p_159369_, double p_159370_, int p_159371_, int p_159372_, float p_159373_) {
      double d0 = (double)p_159368_.getMiddleBlockX();
      double d1 = (double)p_159368_.getMiddleBlockZ();
      double d2 = p_159369_ - d0;
      double d3 = p_159370_ - d1;
      double d4 = (double)(p_159372_ - p_159371_);
      double d5 = (double)(p_159373_ + 2.0F + 16.0F);
      return d2 * d2 + d3 * d3 - d4 * d4 <= d5 * d5;
   }

   private static boolean isDebugEnabled(CarverConfiguration p_159424_) {
      return p_159424_.debugSettings.isDebugMode();
   }

   public interface CarveSkipChecker {
      boolean shouldSkip(CarvingContext p_159426_, double p_159427_, double p_159428_, double p_159429_, int p_159430_);
   }
}