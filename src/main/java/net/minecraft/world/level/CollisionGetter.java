package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends BlockGetter {
   WorldBorder getWorldBorder();

   @Nullable
   BlockGetter getChunkForCollisions(int p_45774_, int p_45775_);

   default boolean isUnobstructed(@Nullable Entity p_45750_, VoxelShape p_45751_) {
      return true;
   }

   default boolean isUnobstructed(BlockState p_45753_, BlockPos p_45754_, CollisionContext p_45755_) {
      VoxelShape voxelshape = p_45753_.getCollisionShape(this, p_45754_, p_45755_);
      return voxelshape.isEmpty() || this.isUnobstructed((Entity)null, voxelshape.move((double)p_45754_.getX(), (double)p_45754_.getY(), (double)p_45754_.getZ()));
   }

   default boolean isUnobstructed(Entity p_45785_) {
      return this.isUnobstructed(p_45785_, Shapes.create(p_45785_.getBoundingBox()));
   }

   default boolean noCollision(AABB p_45773_) {
      return this.noCollision((Entity)null, p_45773_, (p_45780_) -> {
         return true;
      });
   }

   default boolean noCollision(Entity p_45787_) {
      return this.noCollision(p_45787_, p_45787_.getBoundingBox(), (p_45760_) -> {
         return true;
      });
   }

   default boolean noCollision(Entity p_45757_, AABB p_45758_) {
      return this.noCollision(p_45757_, p_45758_, (p_45745_) -> {
         return true;
      });
   }

   default boolean noCollision(@Nullable Entity p_45769_, AABB p_45770_, Predicate<Entity> p_45771_) {
      return this.getCollisions(p_45769_, p_45770_, p_45771_).allMatch(VoxelShape::isEmpty);
   }

   Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_45776_, AABB p_45777_, Predicate<Entity> p_45778_);

   default Stream<VoxelShape> getCollisions(@Nullable Entity p_45781_, AABB p_45782_, Predicate<Entity> p_45783_) {
      return Stream.concat(this.getBlockCollisions(p_45781_, p_45782_), this.getEntityCollisions(p_45781_, p_45782_, p_45783_));
   }

   default Stream<VoxelShape> getBlockCollisions(@Nullable Entity p_45762_, AABB p_45763_) {
      return StreamSupport.stream(new CollisionSpliterator(this, p_45762_, p_45763_), false);
   }

   default boolean hasBlockCollision(@Nullable Entity p_151415_, AABB p_151416_, BiPredicate<BlockState, BlockPos> p_151417_) {
      return !this.getBlockCollisions(p_151415_, p_151416_, p_151417_).allMatch(VoxelShape::isEmpty);
   }

   default Stream<VoxelShape> getBlockCollisions(@Nullable Entity p_45765_, AABB p_45766_, BiPredicate<BlockState, BlockPos> p_45767_) {
      return StreamSupport.stream(new CollisionSpliterator(this, p_45765_, p_45766_, p_45767_), false);
   }

   default Optional<Vec3> findFreePosition(@Nullable Entity p_151419_, VoxelShape p_151420_, Vec3 p_151421_, double p_151422_, double p_151423_, double p_151424_) {
      if (p_151420_.isEmpty()) {
         return Optional.empty();
      } else {
         AABB aabb = p_151420_.bounds().inflate(p_151422_, p_151423_, p_151424_);
         VoxelShape voxelshape = this.getBlockCollisions(p_151419_, aabb).flatMap((p_151426_) -> {
            return p_151426_.toAabbs().stream();
         }).map((p_151413_) -> {
            return p_151413_.inflate(p_151422_ / 2.0D, p_151423_ / 2.0D, p_151424_ / 2.0D);
         }).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
         VoxelShape voxelshape1 = Shapes.join(p_151420_, voxelshape, BooleanOp.ONLY_FIRST);
         return voxelshape1.closestPointTo(p_151421_);
      }
   }
}