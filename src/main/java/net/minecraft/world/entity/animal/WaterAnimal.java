package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class WaterAnimal extends PathfinderMob {
   protected WaterAnimal(EntityType<? extends WaterAnimal> p_30341_, Level p_30342_) {
      super(p_30341_, p_30342_);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public MobType getMobType() {
      return MobType.WATER;
   }

   public boolean checkSpawnObstruction(LevelReader p_30348_) {
      return p_30348_.isUnobstructed(this);
   }

   public int getAmbientSoundInterval() {
      return 120;
   }

   protected int getExperienceReward(Player p_30353_) {
      return 1 + this.level.random.nextInt(3);
   }

   protected void handleAirSupply(int p_30344_) {
      if (this.isAlive() && !this.isInWaterOrBubble()) {
         this.setAirSupply(p_30344_ - 1);
         if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(DamageSource.DROWN, 2.0F);
         }
      } else {
         this.setAirSupply(300);
      }

   }

   public void baseTick() {
      int i = this.getAirSupply();
      super.baseTick();
      this.handleAirSupply(i);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean canBeLeashed(Player p_30346_) {
      return false;
   }

   public static boolean checkUndergroundWaterCreatureSpawnRules(EntityType<? extends LivingEntity> p_149077_, ServerLevelAccessor p_149078_, MobSpawnType p_149079_, BlockPos p_149080_, Random p_149081_) {
      return p_149080_.getY() < p_149078_.getSeaLevel() && p_149080_.getY() < p_149078_.getHeight(Heightmap.Types.OCEAN_FLOOR, p_149080_.getX(), p_149080_.getZ()) && isDarkEnoughToSpawn(p_149078_, p_149080_) && isBaseStoneBelow(p_149080_, p_149078_);
   }

   public static boolean isBaseStoneBelow(BlockPos p_181145_, ServerLevelAccessor p_181146_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_181145_.mutable();

      for(int i = 0; i < 5; ++i) {
         blockpos$mutableblockpos.move(Direction.DOWN);
         BlockState blockstate = p_181146_.getBlockState(blockpos$mutableblockpos);
         if (blockstate.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
         }

         if (!blockstate.is(Blocks.WATER)) {
            return false;
         }
      }

      return false;
   }

   public static boolean isDarkEnoughToSpawn(ServerLevelAccessor p_181142_, BlockPos p_181143_) {
      int i = p_181142_.getLevel().isThundering() ? p_181142_.getMaxLocalRawBrightness(p_181143_, 10) : p_181142_.getMaxLocalRawBrightness(p_181143_);
      return i == 0;
   }
}