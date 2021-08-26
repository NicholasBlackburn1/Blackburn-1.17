package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;

public enum WhiteGlyph implements RawGlyph
{
    INSTANCE;

    private static final int WIDTH = 5;
    private static final int HEIGHT = 8;
    private static final NativeImage IMAGE_DATA = Util.make(new NativeImage(NativeImage.Format.RGBA, 5, 8, false), (p_95319_) -> {
        for (int i = 0; i < 8; ++i)
        {
            for (int j = 0; j < 5; ++j)
            {
                if (j != 0 && j + 1 != 5 && i != 0 && i + 1 != 8)
                {
                    boolean flag = false;
                }
                else
                {
                    boolean flag1 = true;
                }

                p_95319_.setPixelRGBA(j, i, -1);
            }
        }

        p_95319_.untrack();
    });

    public int getPixelWidth()
    {
        return 5;
    }

    public int getPixelHeight()
    {
        return 8;
    }

    public float getAdvance()
    {
        return 6.0F;
    }

    public float getOversample()
    {
        return 1.0F;
    }

    public void upload(int pXOffset, int pYOffset)
    {
        IMAGE_DATA.upload(0, pXOffset, pYOffset, false);
    }

    public boolean isColored()
    {
        return true;
    }
}
