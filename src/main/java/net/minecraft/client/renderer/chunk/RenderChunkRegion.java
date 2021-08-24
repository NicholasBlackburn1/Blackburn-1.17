package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
   protected final int centerX;
   protected final int centerZ;
   protected final BlockPos start;
   protected final int xLength;
   protected final int yLength;
   protected final int zLength;
   protected final LevelChunk[][] chunks;
   protected final BlockState[] blockStates;
   protected final Level level;

   @Nullable
   public static RenderChunkRegion createIfNotEmpty(Level p_112921_, BlockPos p_112922_, BlockPos p_112923_, int p_112924_) {
      int i = SectionPos.blockToSectionCoord(p_112922_.getX() - p_112924_);
      int j = SectionPos.blockToSectionCoord(p_112922_.getZ() - p_112924_);
      int k = SectionPos.blockToSectionCoord(p_112923_.getX() + p_112924_);
      int l = SectionPos.blockToSectionCoord(p_112923_.getZ() + p_112924_);
      LevelChunk[][] alevelchunk = new LevelChunk[k - i + 1][l - j + 1];

      for(int i1 = i; i1 <= k; ++i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            alevelchunk[i1 - i][j1 - j] = p_112921_.getChunk(i1, j1);
         }
      }

      if (isAllEmpty(p_112922_, p_112923_, i, j, alevelchunk)) {
         return null;
      } else {
         int k1 = 1;
         BlockPos blockpos1 = p_112922_.offset(-1, -1, -1);
         BlockPos blockpos = p_112923_.offset(1, 1, 1);
         return new RenderChunkRegion(p_112921_, i, j, alevelchunk, blockpos1, blockpos);
      }
   }

   public static boolean isAllEmpty(BlockPos p_112931_, BlockPos p_112932_, int p_112933_, int p_112934_, LevelChunk[][] p_112935_) {
      for(int i = SectionPos.blockToSectionCoord(p_112931_.getX()); i <= SectionPos.blockToSectionCoord(p_112932_.getX()); ++i) {
         for(int j = SectionPos.blockToSectionCoord(p_112931_.getZ()); j <= SectionPos.blockToSectionCoord(p_112932_.getZ()); ++j) {
            LevelChunk levelchunk = p_112935_[i - p_112933_][j - p_112934_];
            if (!levelchunk.isYSpaceEmpty(p_112931_.getY(), p_112932_.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   public RenderChunkRegion(Level p_112910_, int p_112911_, int p_112912_, LevelChunk[][] p_112913_, BlockPos p_112914_, BlockPos p_112915_) {
      this.level = p_112910_;
      this.centerX = p_112911_;
      this.centerZ = p_112912_;
      this.chunks = p_112913_;
      this.start = p_112914_;
      this.xLength = p_112915_.getX() - p_112914_.getX() + 1;
      this.yLength = p_112915_.getY() - p_112914_.getY() + 1;
      this.zLength = p_112915_.getZ() - p_112914_.getZ() + 1;
      this.blockStates = new BlockState[this.xLength * this.yLength * this.zLength];

      for(BlockPos blockpos : BlockPos.betweenClosed(p_112914_, p_112915_)) {
         int i = SectionPos.blockToSectionCoord(blockpos.getX()) - p_112911_;
         int j = SectionPos.blockToSectionCoord(blockpos.getZ()) - p_112912_;
         LevelChunk levelchunk = p_112913_[i][j];
         int k = this.index(blockpos);
         this.blockStates[k] = levelchunk.getBlockState(blockpos);
      }

   }

   protected final int index(BlockPos p_112926_) {
      return this.index(p_112926_.getX(), p_112926_.getY(), p_112926_.getZ());
   }

   protected int index(int p_112917_, int p_112918_, int p_112919_) {
      int i = p_112917_ - this.start.getX();
      int j = p_112918_ - this.start.getY();
      int k = p_112919_ - this.start.getZ();
      return k * this.xLength * this.yLength + j * this.xLength + i;
   }

   public BlockState getBlockState(BlockPos p_112947_) {
      return this.blockStates[this.index(p_112947_)];
   }

   public FluidState getFluidState(BlockPos p_112943_) {
      return this.blockStates[this.index(p_112943_)].getFluidState();
   }

   public float getShade(Direction p_112940_, boolean p_112941_) {
      return this.level.getShade(p_112940_, p_112941_);
   }

   public LevelLightEngine getLightEngine() {
      return this.level.getLightEngine();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_112945_) {
      return this.getBlockEntity(p_112945_, LevelChunk.EntityCreationType.IMMEDIATE);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_112928_, LevelChunk.EntityCreationType p_112929_) {
      int i = SectionPos.blockToSectionCoord(p_112928_.getX()) - this.centerX;
      int j = SectionPos.blockToSectionCoord(p_112928_.getZ()) - this.centerZ;
      return this.chunks[i][j].getBlockEntity(p_112928_, p_112929_);
   }

   public int getBlockTint(BlockPos p_112937_, ColorResolver p_112938_) {
      return this.level.getBlockTint(p_112937_, p_112938_);
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }
}