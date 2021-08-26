package com.mojang.blaze3d.font;

public interface GlyphInfo
{
    float getAdvance();

default float getAdvance(boolean p_83828_)
    {
        return this.getAdvance() + (p_83828_ ? this.getBoldOffset() : 0.0F);
    }

default float getBearingX()
    {
        return 0.0F;
    }

default float getBearingY()
    {
        return 0.0F;
    }

default float getBoldOffset()
    {
        return 1.0F;
    }

default float getShadowOffset()
    {
        return 1.0F;
    }
}
