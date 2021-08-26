package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PatrolSpawner implements CustomSpawner
{
    private int nextTick;

    public int tick(ServerLevel pLevel, boolean pSpawnHostiles, boolean pSpawnPassives)
    {
        if (!pSpawnHostiles)
        {
            return 0;
        }
        else if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING))
        {
            return 0;
        }
        else
        {
            Random random = pLevel.random;
            --this.nextTick;

            if (this.nextTick > 0)
            {
                return 0;
            }
            else
            {
                this.nextTick += 12000 + random.nextInt(1200);
                long i = pLevel.getDayTime() / 24000L;

                if (i >= 5L && pLevel.isDay())
                {
                    if (random.nextInt(5) != 0)
                    {
                        return 0;
                    }
                    else
                    {
                        int j = pLevel.players().size();

                        if (j < 1)
                        {
                            return 0;
                        }
                        else
                        {
                            Player player = pLevel.players().get(random.nextInt(j));

                            if (player.isSpectator())
                            {
                                return 0;
                            }
                            else if (pLevel.isCloseToVillage(player.blockPosition(), 2))
                            {
                                return 0;
                            }
                            else
                            {
                                int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                int l = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                BlockPos.MutableBlockPos blockpos$mutableblockpos = player.blockPosition().mutable().move(k, 0, l);
                                int i1 = 10;

                                if (!pLevel.hasChunksAt(blockpos$mutableblockpos.getX() - 10, blockpos$mutableblockpos.getZ() - 10, blockpos$mutableblockpos.getX() + 10, blockpos$mutableblockpos.getZ() + 10))
                                {
                                    return 0;
                                }
                                else
                                {
                                    Biome biome = pLevel.getBiome(blockpos$mutableblockpos);
                                    Biome.BiomeCategory biome$biomecategory = biome.getBiomeCategory();

                                    if (biome$biomecategory == Biome.BiomeCategory.MUSHROOM)
                                    {
                                        return 0;
                                    }
                                    else
                                    {
                                        int j1 = 0;
                                        int k1 = (int)Math.ceil((double)pLevel.getCurrentDifficultyAt(blockpos$mutableblockpos).getEffectiveDifficulty()) + 1;

                                        for (int l1 = 0; l1 < k1; ++l1)
                                        {
                                            ++j1;
                                            blockpos$mutableblockpos.setY(pLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutableblockpos).getY());

                                            if (l1 == 0)
                                            {
                                                if (!this.spawnPatrolMember(pLevel, blockpos$mutableblockpos, random, true))
                                                {
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                this.spawnPatrolMember(pLevel, blockpos$mutableblockpos, random, false);
                                            }

                                            blockpos$mutableblockpos.setX(blockpos$mutableblockpos.getX() + random.nextInt(5) - random.nextInt(5));
                                            blockpos$mutableblockpos.setZ(blockpos$mutableblockpos.getZ() + random.nextInt(5) - random.nextInt(5));
                                        }

                                        return j1;
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    return 0;
                }
            }
        }
    }

    private boolean spawnPatrolMember(ServerLevel pLevel, BlockPos p_64566_, Random pRandom, boolean p_64568_)
    {
        BlockState blockstate = pLevel.getBlockState(p_64566_);

        if (!NaturalSpawner.isValidEmptySpawnBlock(pLevel, p_64566_, blockstate, blockstate.getFluidState(), EntityType.PILLAGER))
        {
            return false;
        }
        else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, pLevel, MobSpawnType.PATROL, p_64566_, pRandom))
        {
            return false;
        }
        else
        {
            PatrollingMonster patrollingmonster = EntityType.PILLAGER.create(pLevel);

            if (patrollingmonster != null)
            {
                if (p_64568_)
                {
                    patrollingmonster.setPatrolLeader(true);
                    patrollingmonster.findPatrolTarget();
                }

                patrollingmonster.setPos((double)p_64566_.getX(), (double)p_64566_.getY(), (double)p_64566_.getZ());
                patrollingmonster.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(p_64566_), MobSpawnType.PATROL, (SpawnGroupData)null, (CompoundTag)null);
                pLevel.addFreshEntityWithPassengers(patrollingmonster);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
