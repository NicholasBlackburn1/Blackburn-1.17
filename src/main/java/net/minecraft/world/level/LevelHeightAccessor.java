package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
   int getHeight();

   int getMinBuildHeight();

   default int getMaxBuildHeight() {
      return this.getMinBuildHeight() + this.getHeight();
   }

   default int getSectionsCount() {
      return this.getMaxSection() - this.getMinSection();
   }

   default int getMinSection() {
      return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
   }

   default int getMaxSection() {
      return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
   }

   default boolean isOutsideBuildHeight(BlockPos p_151571_) {
      return this.isOutsideBuildHeight(p_151571_.getY());
   }

   default boolean isOutsideBuildHeight(int p_151563_) {
      return p_151563_ < this.getMinBuildHeight() || p_151563_ >= this.getMaxBuildHeight();
   }

   default int getSectionIndex(int p_151565_) {
      return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(p_151565_));
   }

   default int getSectionIndexFromSectionY(int p_151567_) {
      return p_151567_ - this.getMinSection();
   }

   default int getSectionYFromSectionIndex(int p_151569_) {
      return p_151569_ + this.getMinSection();
   }
}