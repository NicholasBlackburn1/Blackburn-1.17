package com.mojang.realmsclient.gui.task;

public class NoStartupDelay implements RestartDelayCalculator
{
    public void markExecutionStart()
    {
    }

    public long getNextDelayMs()
    {
        return 0L;
    }
}
