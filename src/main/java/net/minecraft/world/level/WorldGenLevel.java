package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface WorldGenLevel extends ServerLevelAccessor {
   long getSeed();

   Stream<? extends StructureStart<?>> startsForFeature(SectionPos p_47369_, StructureFeature<?> p_47370_);

   default boolean ensureCanWrite(BlockPos p_181157_) {
      return true;
   }
}