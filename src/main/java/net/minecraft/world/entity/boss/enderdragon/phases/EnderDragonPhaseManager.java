package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnderDragonPhaseManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final EnderDragon dragon;
    private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
    private DragonPhaseInstance currentPhase;

    public EnderDragonPhaseManager(EnderDragon p_31414_)
    {
        this.dragon = p_31414_;
        this.setPhase(EnderDragonPhase.HOVERING);
    }

    public void setPhase(EnderDragonPhase<?> pPhase)
    {
        if (this.currentPhase == null || pPhase != this.currentPhase.getPhase())
        {
            if (this.currentPhase != null)
            {
                this.currentPhase.end();
            }

            this.currentPhase = this.getPhase(pPhase);

            if (!this.dragon.level.isClientSide)
            {
                this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, pPhase.getId());
            }

            LOGGER.debug("Dragon is now in phase {} on the {}", pPhase, this.dragon.level.isClientSide ? "client" : "server");
            this.currentPhase.begin();
        }
    }

    public DragonPhaseInstance getCurrentPhase()
    {
        return this.currentPhase;
    }

    public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> pPhase)
    {
        int i = pPhase.getId();

        if (this.phases[i] == null)
        {
            this.phases[i] = pPhase.createInstance(this.dragon);
        }

        return (T)this.phases[i];
    }
}
