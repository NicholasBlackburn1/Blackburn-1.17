package net.minecraft.world.level.levelgen.placement;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class DecorationContext extends WorldGenerationContext {
   private final WorldGenLevel level;

   public DecorationContext(WorldGenLevel p_70584_, ChunkGenerator p_70585_) {
      super(p_70585_, p_70584_);
      this.level = p_70584_;
   }

   public int getHeight(Heightmap.Types p_70591_, int p_70592_, int p_70593_) {
      return this.level.getHeight(p_70591_, p_70592_, p_70593_);
   }

   public BitSet getCarvingMask(ChunkPos p_70588_, GenerationStep.Carving p_70589_) {
      return ((ProtoChunk)this.level.getChunk(p_70588_.x, p_70588_.z)).getOrCreateCarvingMask(p_70589_);
   }

   public BlockState getBlockState(BlockPos p_70595_) {
      return this.level.getBlockState(p_70595_);
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public WorldGenLevel getLevel() {
      return this.level;
   }
}