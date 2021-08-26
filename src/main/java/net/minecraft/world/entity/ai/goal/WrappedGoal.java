package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class WrappedGoal extends Goal
{
    private final Goal goal;
    private final int priority;
    private boolean isRunning;

    public WrappedGoal(int p_25998_, Goal p_25999_)
    {
        this.priority = p_25998_;
        this.goal = p_25999_;
    }

    public boolean canBeReplacedBy(WrappedGoal pOther)
    {
        return this.isInterruptable() && pOther.getPriority() < this.getPriority();
    }

    public boolean canUse()
    {
        return this.goal.canUse();
    }

    public boolean canContinueToUse()
    {
        return this.goal.canContinueToUse();
    }

    public boolean isInterruptable()
    {
        return this.goal.isInterruptable();
    }

    public void start()
    {
        if (!this.isRunning)
        {
            this.isRunning = true;
            this.goal.start();
        }
    }

    public void stop()
    {
        if (this.isRunning)
        {
            this.isRunning = false;
            this.goal.stop();
        }
    }

    public void tick()
    {
        this.goal.tick();
    }

    public void setFlags(EnumSet<Goal.Flag> pFlagSet)
    {
        this.goal.setFlags(pFlagSet);
    }

    public EnumSet<Goal.Flag> getFlags()
    {
        return this.goal.getFlags();
    }

    public boolean isRunning()
    {
        return this.isRunning;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public Goal getGoal()
    {
        return this.goal;
    }

    public boolean equals(@Nullable Object p_26011_)
    {
        if (this == p_26011_)
        {
            return true;
        }
        else
        {
            return p_26011_ != null && this.getClass() == p_26011_.getClass() ? this.goal.equals(((WrappedGoal)p_26011_).goal) : false;
        }
    }

    public int hashCode()
    {
        return this.goal.hashCode();
    }
}
