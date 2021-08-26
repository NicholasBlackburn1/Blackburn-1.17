package net.minecraft.world;

public interface SnooperPopulator
{
    void populateSnooper(Snooper pSnooper);

    void populateSnooperInitial(Snooper pSnooper);

    boolean isSnooperEnabled();
}
