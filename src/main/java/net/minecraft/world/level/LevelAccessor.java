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

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess
{
default long dayTime()
    {
        return this.getLevelData().getDayTime();
    }

    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

    LevelData getLevelData();

    DifficultyInstance getCurrentDifficultyAt(BlockPos pPos);

    @Nullable
    MinecraftServer getServer();

default Difficulty getDifficulty()
    {
        return this.getLevelData().getDifficulty();
    }

    ChunkSource getChunkSource();

default boolean hasChunk(int pChunkX, int pChunkZ)
    {
        return this.getChunkSource().hasChunk(pChunkX, pChunkZ);
    }

    Random getRandom();

default void blockUpdated(BlockPos pPos, Block pBlock)
    {
    }

    void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch);

    void addParticle(ParticleOptions pParticleData, double pX, double p_46785_, double pY, double p_46787_, double pZ, double p_46789_);

    void levelEvent(@Nullable Player pType, int pPos, BlockPos pData, int p_46774_);

default int getLogicalHeight()
    {
        return this.dimensionType().logicalHeight();
    }

default void levelEvent(int pType, BlockPos pPos, int pData)
    {
        this.levelEvent((Player)null, pType, pPos, pData);
    }

    void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_);

default void gameEvent(GameEvent p_151556_, BlockPos p_151557_)
    {
        this.gameEvent((Entity)null, p_151556_, p_151557_);
    }

default void gameEvent(GameEvent p_151553_, Entity p_151554_)
    {
        this.gameEvent((Entity)null, p_151553_, p_151554_.blockPosition());
    }

default void gameEvent(@Nullable Entity p_151546_, GameEvent p_151547_, Entity p_151548_)
    {
        this.gameEvent(p_151546_, p_151547_, p_151548_.blockPosition());
    }
}
