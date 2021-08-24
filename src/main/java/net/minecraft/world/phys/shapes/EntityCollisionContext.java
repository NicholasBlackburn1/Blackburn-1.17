package net.minecraft.world.phys.shapes;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
   protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, ItemStack.EMPTY, (p_82891_) -> {
      return false;
   }, Optional.empty()) {
      public boolean isAbove(VoxelShape p_82898_, BlockPos p_82899_, boolean p_82900_) {
         return p_82900_;
      }
   };
   private final boolean descending;
   private final double entityBottom;
   private final ItemStack heldItem;
   private final ItemStack footItem;
   private final Predicate<Fluid> canStandOnFluid;
   private final Optional<Entity> entity;

   protected EntityCollisionContext(boolean p_166004_, double p_166005_, ItemStack p_166006_, ItemStack p_166007_, Predicate<Fluid> p_166008_, Optional<Entity> p_166009_) {
      this.descending = p_166004_;
      this.entityBottom = p_166005_;
      this.footItem = p_166006_;
      this.heldItem = p_166007_;
      this.canStandOnFluid = p_166008_;
      this.entity = p_166009_;
   }

   @Deprecated
   protected EntityCollisionContext(Entity p_82872_) {
      this(p_82872_.isDescending(), p_82872_.getY(), p_82872_ instanceof LivingEntity ? ((LivingEntity)p_82872_).getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY, p_82872_ instanceof LivingEntity ? ((LivingEntity)p_82872_).getMainHandItem() : ItemStack.EMPTY, p_82872_ instanceof LivingEntity ? ((LivingEntity)p_82872_)::canStandOnFluid : (p_82881_) -> {
         return false;
      }, Optional.of(p_82872_));
   }

   public boolean hasItemOnFeet(Item p_166011_) {
      return this.footItem.is(p_166011_);
   }

   public boolean isHoldingItem(Item p_82879_) {
      return this.heldItem.is(p_82879_);
   }

   public boolean canStandOnFluid(FluidState p_82883_, FlowingFluid p_82884_) {
      return this.canStandOnFluid.test(p_82884_) && !p_82883_.getType().isSame(p_82884_);
   }

   public boolean isDescending() {
      return this.descending;
   }

   public boolean isAbove(VoxelShape p_82886_, BlockPos p_82887_, boolean p_82888_) {
      return this.entityBottom > (double)p_82887_.getY() + p_82886_.max(Direction.Axis.Y) - (double)1.0E-5F;
   }

   public Optional<Entity> getEntity() {
      return this.entity;
   }
}