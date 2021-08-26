package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public enum DragonRespawnAnimation
{
    START {
        public void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos)
        {
            BlockPos blockpos = new BlockPos(0, 128, 0);

            for (EndCrystal endcrystal : pCrystals)
            {
                endcrystal.setBeamTarget(blockpos);
            }

            pManager.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
        }
    },
    PREPARING_TO_SUMMON_PILLARS {
        public void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos)
        {
            if (pTicks < 100)
            {
                if (pTicks == 0 || pTicks == 50 || pTicks == 51 || pTicks == 52 || pTicks >= 95)
                {
                    pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
                }
            }
            else
            {
                pManager.setRespawnStage(SUMMONING_PILLARS);
            }
        }
    },
    SUMMONING_PILLARS {
        public void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos)
        {
            int i = 40;
            boolean flag = pTicks % 40 == 0;
            boolean flag1 = pTicks % 40 == 39;

            if (flag || flag1)
            {
                List<SpikeFeature.EndSpike> list = SpikeFeature.getSpikesForLevel(pLevel);
                int j = pTicks / 40;

                if (j < list.size())
                {
                    SpikeFeature.EndSpike spikefeature$endspike = list.get(j);

                    if (flag)
                    {
                        for (EndCrystal endcrystal : pCrystals)
                        {
                            endcrystal.setBeamTarget(new BlockPos(spikefeature$endspike.getCenterX(), spikefeature$endspike.getHeight() + 1, spikefeature$endspike.getCenterZ()));
                        }
                    }
                    else
                    {
                        int k = 10;

                        for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(spikefeature$endspike.getCenterX() - 10, spikefeature$endspike.getHeight() - 10, spikefeature$endspike.getCenterZ() - 10), new BlockPos(spikefeature$endspike.getCenterX() + 10, spikefeature$endspike.getHeight() + 10, spikefeature$endspike.getCenterZ() + 10)))
                        {
                            pLevel.removeBlock(blockpos, false);
                        }

                        pLevel.explode((Entity)null, (double)((float)spikefeature$endspike.getCenterX() + 0.5F), (double)spikefeature$endspike.getHeight(), (double)((float)spikefeature$endspike.getCenterZ() + 0.5F), 5.0F, Explosion.BlockInteraction.DESTROY);
                        SpikeConfiguration spikeconfiguration = new SpikeConfiguration(true, ImmutableList.of(spikefeature$endspike), new BlockPos(0, 128, 0));
                        Feature.END_SPIKE.configured(spikeconfiguration).place(pLevel, pLevel.getChunkSource().getGenerator(), new Random(), new BlockPos(spikefeature$endspike.getCenterX(), 45, spikefeature$endspike.getCenterZ()));
                    }
                }
                else if (flag)
                {
                    pManager.setRespawnStage(SUMMONING_DRAGON);
                }
            }
        }
    },
    SUMMONING_DRAGON {
        public void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos)
        {
            if (pTicks >= 100)
            {
                pManager.setRespawnStage(END);
                pManager.resetSpikeCrystals();

                for (EndCrystal endcrystal : pCrystals)
                {
                    endcrystal.setBeamTarget((BlockPos)null);
                    pLevel.explode(endcrystal, endcrystal.getX(), endcrystal.getY(), endcrystal.getZ(), 6.0F, Explosion.BlockInteraction.NONE);
                    endcrystal.discard();
                }
            }
            else if (pTicks >= 80)
            {
                pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }
            else if (pTicks == 0)
            {
                for (EndCrystal endcrystal1 : pCrystals)
                {
                    endcrystal1.setBeamTarget(new BlockPos(0, 128, 0));
                }
            }
            else if (pTicks < 5)
            {
                pLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
            }
        }
    },
    END {
        public void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos)
        {
        }
    };

    public abstract void tick(ServerLevel pLevel, EndDragonFight pManager, List<EndCrystal> pCrystals, int pTicks, BlockPos pPos);
}
