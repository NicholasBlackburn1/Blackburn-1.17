package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
   private final LevelAccessor level;
   private final WorldGenSettings worldGenSettings;

   public StructureFeatureManager(LevelAccessor p_47269_, WorldGenSettings p_47270_) {
      this.level = p_47269_;
      this.worldGenSettings = p_47270_;
   }

   public StructureFeatureManager forWorldGenRegion(WorldGenRegion p_47273_) {
      if (p_47273_.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid feature manager (source level: " + p_47273_.getLevel() + ", region: " + p_47273_);
      } else {
         return new StructureFeatureManager(p_47273_, this.worldGenSettings);
      }
   }

   public Stream<? extends StructureStart<?>> startsForFeature(SectionPos p_47290_, StructureFeature<?> p_47291_) {
      return this.level.getChunk(p_47290_.x(), p_47290_.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(p_47291_).stream().map((p_47307_) -> {
         return SectionPos.of(new ChunkPos(p_47307_), this.level.getMinSection());
      }).map((p_47276_) -> {
         return this.getStartForFeature(p_47276_, p_47291_, this.level.getChunk(p_47276_.x(), p_47276_.z(), ChunkStatus.STRUCTURE_STARTS));
      }).filter((p_47278_) -> {
         return p_47278_ != null && p_47278_.isValid();
      });
   }

   @Nullable
   public StructureStart<?> getStartForFeature(SectionPos p_47298_, StructureFeature<?> p_47299_, FeatureAccess p_47300_) {
      return p_47300_.getStartForFeature(p_47299_);
   }

   public void setStartForFeature(SectionPos p_47302_, StructureFeature<?> p_47303_, StructureStart<?> p_47304_, FeatureAccess p_47305_) {
      p_47305_.setStartForFeature(p_47303_, p_47304_);
   }

   public void addReferenceForFeature(SectionPos p_47293_, StructureFeature<?> p_47294_, long p_47295_, FeatureAccess p_47296_) {
      p_47296_.addReferenceForFeature(p_47294_, p_47295_);
   }

   public boolean shouldGenerateFeatures() {
      return this.worldGenSettings.generateFeatures();
   }

   public StructureStart<?> getStructureAt(BlockPos p_47286_, boolean p_47287_, StructureFeature<?> p_47288_) {
      return DataFixUtils.orElse(this.startsForFeature(SectionPos.of(p_47286_), p_47288_).filter((p_151637_) -> {
         return p_47287_ ? p_151637_.getPieces().stream().anyMatch((p_151633_) -> {
            return p_151633_.getBoundingBox().isInside(p_47286_);
         }) : p_151637_.getBoundingBox().isInside(p_47286_);
      }).findFirst(), StructureStart.INVALID_START);
   }
}