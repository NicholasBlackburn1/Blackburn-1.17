package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
   public NoiseAffectingStructureStart(StructureFeature<C> p_162982_, ChunkPos p_162983_, int p_162984_, long p_162985_) {
      super(p_162982_, p_162983_, p_162984_, p_162985_);
   }

   protected BoundingBox createBoundingBox() {
      return super.createBoundingBox().inflate(12);
   }
}