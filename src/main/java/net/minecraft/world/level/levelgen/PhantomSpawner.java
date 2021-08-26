package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner implements CustomSpawner
{
    private int nextTick;

    public int tick(ServerLevel pLevel, boolean pSpawnHostiles, boolean pSpawnPassives)
    {
        if (!pSpawnHostiles)
        {
            return 0;
        }
        else if (!pLevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA))
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
                this.nextTick += (60 + random.nextInt(60)) * 20;

                if (pLevel.getSkyDarken() < 5 && pLevel.dimensionType().hasSkyLight())
                {
                    return 0;
                }
                else
                {
                    int i = 0;

                    for (Player player : pLevel.players())
                    {
                        if (!player.isSpectator())
                        {
                            BlockPos blockpos = player.blockPosition();

                            if (!pLevel.dimensionType().hasSkyLight() || blockpos.getY() >= pLevel.getSeaLevel() && pLevel.canSeeSky(blockpos))
                            {
                                DifficultyInstance difficultyinstance = pLevel.getCurrentDifficultyAt(blockpos);

                                if (difficultyinstance.isHarderThan(random.nextFloat() * 3.0F))
                                {
                                    ServerStatsCounter serverstatscounter = ((ServerPlayer)player).getStats();
                                    int j = Mth.clamp(serverstatscounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                                    int k = 24000;

                                    if (random.nextInt(j) >= 72000)
                                    {
                                        BlockPos blockpos1 = blockpos.above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
                                        BlockState blockstate = pLevel.getBlockState(blockpos1);
                                        FluidState fluidstate = pLevel.getFluidState(blockpos1);

                                        if (NaturalSpawner.isValidEmptySpawnBlock(pLevel, blockpos1, blockstate, fluidstate, EntityType.PHANTOM))
                                        {
                                            SpawnGroupData spawngroupdata = null;
                                            int l = 1 + random.nextInt(difficultyinstance.getDifficulty().getId() + 1);

                                            for (int i1 = 0; i1 < l; ++i1)
                                            {
                                                Phantom phantom = EntityType.PHANTOM.create(pLevel);
                                                phantom.moveTo(blockpos1, 0.0F, 0.0F);
                                                spawngroupdata = phantom.finalizeSpawn(pLevel, difficultyinstance, MobSpawnType.NATURAL, spawngroupdata, (CompoundTag)null);
                                                pLevel.addFreshEntityWithPassengers(phantom);
                                            }

                                            i += l;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return i;
                }
            }
        }
    }
}
