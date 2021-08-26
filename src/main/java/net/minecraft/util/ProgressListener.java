package net.minecraft.util;

import net.minecraft.network.chat.Component;

public interface ProgressListener
{
    void progressStartNoAbort(Component pComponent);

    void progressStart(Component pComponent);

    void progressStage(Component pComponent);

    void progressStagePercentage(int pProgress);

    void stop();
}
