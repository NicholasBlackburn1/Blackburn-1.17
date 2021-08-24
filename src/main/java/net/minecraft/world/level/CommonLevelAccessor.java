package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151452_, BlockEntityType<T> p_151453_) {
      return LevelReader.super.getBlockEntity(p_151452_, p_151453_);
   }

   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_45834_, AABB p_45835_, Predicate<Entity> p_45836_) {
      return EntityGetter.super.getEntityCollisions(p_45834_, p_45835_, p_45836_);
   }

   default boolean isUnobstructed(@Nullable Entity p_45828_, VoxelShape p_45829_) {
      return EntityGetter.super.isUnobstructed(p_45828_, p_45829_);
   }

   default BlockPos getHeightmapPos(Heightmap.Types p_45831_, BlockPos p_45832_) {
      return LevelReader.super.getHeightmapPos(p_45831_, p_45832_);
   }

   RegistryAccess registryAccess();

   default Optional<ResourceKey<Biome>> getBiomeName(BlockPos p_45838_) {
      return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(this.getBiome(p_45838_));
   }
}