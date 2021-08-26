package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect
{
    public InstantenousMobEffect(MobEffectCategory p_19440_, int p_19441_)
    {
        super(p_19440_, p_19441_);
    }

    public boolean isInstantenous()
    {
        return true;
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier)
    {
        return pDuration >= 1;
    }
}
