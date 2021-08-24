package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<RangeDecoratorConfiguration> {
   public NetherFossilFeature(Codec<RangeDecoratorConfiguration> p_72031_) {
      super(p_72031_);
   }

   public StructureFeature.StructureStartFactory<RangeDecoratorConfiguration> getStartFactory() {
      return NetherFossilFeature.FeatureStart::new;
   }

   public static class FeatureStart extends NoiseAffectingStructureStart<RangeDecoratorConfiguration> {
      public FeatureStart(StructureFeature<RangeDecoratorConfiguration> p_162936_, ChunkPos p_162937_, int p_162938_, long p_162939_) {
         super(p_162936_, p_162937_, p_162938_, p_162939_);
      }

      public void generatePieces(RegistryAccess p_162949_, ChunkGenerator p_162950_, StructureManager p_162951_, ChunkPos p_162952_, Biome p_162953_, RangeDecoratorConfiguration p_162954_, LevelHeightAccessor p_162955_) {
         int i = p_162952_.getMinBlockX() + this.random.nextInt(16);
         int j = p_162952_.getMinBlockZ() + this.random.nextInt(16);
         int k = p_162950_.getSeaLevel();
         WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(p_162950_, p_162955_);
         int l = p_162954_.height.sample(this.random, worldgenerationcontext);
         NoiseColumn noisecolumn = p_162950_.getBaseColumn(i, j, p_162955_);

         for(BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, l, j); l > k; --l) {
            BlockState blockstate = noisecolumn.getBlockState(blockpos$mutableblockpos);
            blockpos$mutableblockpos.move(Direction.DOWN);
            BlockState blockstate1 = noisecolumn.getBlockState(blockpos$mutableblockpos);
            if (blockstate.isAir() && (blockstate1.is(Blocks.SOUL_SAND) || blockstate1.isFaceSturdy(EmptyBlockGetter.INSTANCE, blockpos$mutableblockpos, Direction.UP))) {
               break;
            }
         }

         if (l > k) {
            NetherFossilPieces.addPieces(p_162951_, this, this.random, new BlockPos(i, l, j));
         }
      }
   }
}