package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller
{
    String ROOT = "root";

    void startTick();

    void endTick();

    void push(String pName);

    void push(Supplier<String> pName);

    void pop();

    void popPush(String pName);

    void popPush(Supplier<String> pName);

    void markForCharting(MetricCategory p_145959_);

    void incrementCounter(String p_18585_);

    void incrementCounter(Supplier<String> p_18586_);

    static ProfilerFiller tee(final ProfilerFiller p_18579_, final ProfilerFiller p_18580_)
    {
        if (p_18579_ == InactiveProfiler.INSTANCE)
        {
            return p_18580_;
        }
        else
        {
            return p_18580_ == InactiveProfiler.INSTANCE ? p_18579_ : new ProfilerFiller()
            {
                public void startTick()
                {
                    p_18579_.startTick();
                    p_18580_.startTick();
                }
                public void endTick()
                {
                    p_18579_.endTick();
                    p_18580_.endTick();
                }
                public void push(String pName)
                {
                    p_18579_.push(pName);
                    p_18580_.push(pName);
                }
                public void push(Supplier<String> pName)
                {
                    p_18579_.push(pName);
                    p_18580_.push(pName);
                }
                public void markForCharting(MetricCategory p_145961_)
                {
                    p_18579_.markForCharting(p_145961_);
                    p_18580_.markForCharting(p_145961_);
                }
                public void pop()
                {
                    p_18579_.pop();
                    p_18580_.pop();
                }
                public void popPush(String pName)
                {
                    p_18579_.popPush(pName);
                    p_18580_.popPush(pName);
                }
                public void popPush(Supplier<String> pName)
                {
                    p_18579_.popPush(pName);
                    p_18580_.popPush(pName);
                }
                public void incrementCounter(String p_18604_)
                {
                    p_18579_.incrementCounter(p_18604_);
                    p_18580_.incrementCounter(p_18604_);
                }
                public void incrementCounter(Supplier<String> p_18606_)
                {
                    p_18579_.incrementCounter(p_18606_);
                    p_18580_.incrementCounter(p_18606_);
                }
            };
        }
    }
}
