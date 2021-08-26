package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public interface BigContext<R extends Area> extends Context
{
    void initRandom(long pX, long p_76509_);

    R createResult(PixelTransformer pPixelTransformer);

default R createResult(PixelTransformer pPixelTransformer, R p_76512_)
    {
        return this.createResult(pPixelTransformer);
    }

default R createResult(PixelTransformer pPixelTransformer, R p_76514_, R p_76515_)
    {
        return this.createResult(pPixelTransformer);
    }

default int random(int p_76501_, int p_76502_)
    {
        return this.nextRandom(2) == 0 ? p_76501_ : p_76502_;
    }

default int random(int p_76504_, int p_76505_, int p_76506_, int p_76507_)
    {
        int i = this.nextRandom(4);

        if (i == 0)
        {
            return p_76504_;
        }
        else if (i == 1)
        {
            return p_76505_;
        }
        else
        {
            return i == 2 ? p_76506_ : p_76507_;
        }
    }
}
