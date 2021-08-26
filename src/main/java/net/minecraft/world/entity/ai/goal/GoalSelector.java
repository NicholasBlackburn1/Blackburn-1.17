package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.util.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoalSelector
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal()
    {
        public boolean canUse()
        {
            return false;
        }
    })
    {
        public boolean isRunning()
        {
            return false;
        }
    };
    private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap<>(Goal.Flag.class);
    private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
    private final Supplier<ProfilerFiller> profiler;
    private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
    private int tickCount;
    private int newGoalRate = 3;

    public GoalSelector(Supplier<ProfilerFiller> p_25351_)
    {
        this.profiler = p_25351_;
    }

    public void addGoal(int pPriority, Goal pTask)
    {
        this.availableGoals.add(new WrappedGoal(pPriority, pTask));
    }

    @VisibleForTesting
    public void removeAllGoals()
    {
        this.availableGoals.clear();
    }

    public void removeGoal(Goal pTask)
    {
        this.availableGoals.stream().filter((p_25376_1_) ->
        {
            return p_25376_1_.getGoal() == pTask;
        }).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
        this.availableGoals.removeIf((p_25365_1_) ->
        {
            return p_25365_1_.getGoal() == pTask;
        });
    }

    public void tick()
    {
        ProfilerFiller profilerfiller = this.profiler.get();
        profilerfiller.push("goalCleanup");

        if (this.availableGoals.size() > 0)
        {
            for (WrappedGoal wrappedgoal : this.availableGoals)
            {
                if (wrappedgoal.isRunning() && (!wrappedgoal.isRunning() || CollectionUtils.anyMatch(wrappedgoal.getFlags(), this.disabledFlags) || !wrappedgoal.canContinueToUse()))
                {
                    wrappedgoal.stop();
                }
            }
        }

        if (this.lockedFlags.size() > 0)
        {
            this.lockedFlags.forEach((p_25357_1_, p_25357_2_) ->
            {
                if (!p_25357_2_.isRunning())
                {
                    this.lockedFlags.remove(p_25357_1_);
                }
            });
        }

        profilerfiller.pop();
        profilerfiller.push("goalUpdate");

        if (this.availableGoals.size() > 0)
        {
            for (WrappedGoal wrappedgoal1 : this.availableGoals)
            {
                if (!wrappedgoal1.isRunning() && CollectionUtils.noneMatch(wrappedgoal1.getFlags(), this.disabledFlags) && allPreemptedBy(wrappedgoal1, wrappedgoal1.getFlags(), this.lockedFlags) && wrappedgoal1.canUse())
                {
                    resetTasks(wrappedgoal1, wrappedgoal1.getFlags(), this.lockedFlags);
                    wrappedgoal1.start();
                }
            }
        }

        profilerfiller.pop();
        profilerfiller.push("goalTick");

        if (this.availableGoals.size() > 0)
        {
            for (WrappedGoal wrappedgoal2 : this.availableGoals)
            {
                if (wrappedgoal2.isRunning())
                {
                    wrappedgoal2.tick();
                }
            }
        }

        profilerfiller.pop();
    }

    private static boolean allPreemptedBy(WrappedGoal goal, EnumSet<Goal.Flag> flags, Map<Goal.Flag, WrappedGoal> flagGoals)
    {
        if (flags.isEmpty())
        {
            return true;
        }
        else
        {
            for (Goal.Flag goal$flag : flags)
            {
                WrappedGoal wrappedgoal = flagGoals.getOrDefault(goal$flag, NO_GOAL);

                if (!wrappedgoal.canBeReplacedBy(goal))
                {
                    return false;
                }
            }

            return true;
        }
    }

    private static void resetTasks(WrappedGoal goal, EnumSet<Goal.Flag> flags, Map<Goal.Flag, WrappedGoal> flagGoals)
    {
        if (!flags.isEmpty())
        {
            for (Goal.Flag goal$flag : flags)
            {
                WrappedGoal wrappedgoal = flagGoals.getOrDefault(goal$flag, NO_GOAL);
                wrappedgoal.stop();
                flagGoals.put(goal$flag, goal);
            }
        }
    }

    public Set<WrappedGoal> getAvailableGoals()
    {
        return this.availableGoals;
    }

    public Stream<WrappedGoal> getRunningGoals()
    {
        return this.availableGoals.stream().filter(WrappedGoal::isRunning);
    }

    public void setNewGoalRate(int p_148098_)
    {
        this.newGoalRate = p_148098_;
    }

    public void disableControlFlag(Goal.Flag pFlag)
    {
        this.disabledFlags.add(pFlag);
    }

    public void enableControlFlag(Goal.Flag pFlag)
    {
        this.disabledFlags.remove(pFlag);
    }

    public void setControlFlag(Goal.Flag pFlag, boolean pEnabled)
    {
        if (pEnabled)
        {
            this.enableControlFlag(pFlag);
        }
        else
        {
            this.disableControlFlag(pFlag);
        }
    }
}
