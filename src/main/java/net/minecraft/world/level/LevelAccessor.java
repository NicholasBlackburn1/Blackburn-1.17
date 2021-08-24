package net.minecraft.world.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
   default long dayTime() {
      return this.getLevelData().getDayTime();
   }

   TickList<Block> getBlockTicks();

   TickList<Fluid> getLiquidTicks();

   LevelData getLevelData();

   DifficultyInstance getCurrentDifficultyAt(BlockPos p_46800_);

   @Nullable
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   ChunkSource getChunkSource();

   default boolean hasChunk(int p_46794_, int p_46795_) {
      return this.getChunkSource().hasChunk(p_46794_, p_46795_);
   }

   Random getRandom();

   default void blockUpdated(BlockPos p_46781_, Block p_46782_) {
   }

   void playSound(@Nullable Player p_46775_, BlockPos p_46776_, SoundEvent p_46777_, SoundSource p_46778_, float p_46779_, float p_46780_);

   void addParticle(ParticleOptions p_46783_, double p_46784_, double p_46785_, double p_46786_, double p_46787_, double p_46788_, double p_46789_);

   void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_);

   default int getLogicalHeight() {
      return this.dimensionType().logicalHeight();
   }

   default void levelEvent(int p_46797_, BlockPos p_46798_, int p_46799_) {
      this.levelEvent((Player)null, p_46797_, p_46798_, p_46799_);
   }

   void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_);

   default void gameEvent(GameEvent p_151556_, BlockPos p_151557_) {
      this.gameEvent((Entity)null, p_151556_, p_151557_);
   }

   default void gameEvent(GameEvent p_151553_, Entity p_151554_) {
      this.gameEvent((Entity)null, p_151553_, p_151554_.blockPosition());
   }

   default void gameEvent(@Nullable Entity p_151546_, GameEvent p_151547_, Entity p_151548_) {
      this.gameEvent(p_151546_, p_151547_, p_151548_.blockPosition());
   }
}