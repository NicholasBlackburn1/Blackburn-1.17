package net.optifine.util;

import com.mojang.blaze3d.font.GlyphInfo;

public class GlyphAdvanceFixed implements GlyphInfo
{
    private float advanceWidth;

    public GlyphAdvanceFixed(float advanceWidth)
    {
        this.advanceWidth = advanceWidth;
    }

    public float getAdvance()
    {
        return this.advanceWidth;
    }
}
