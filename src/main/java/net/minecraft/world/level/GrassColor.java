package net.minecraft.world.level;

public class GrassColor
{
    private static int[] pixels = new int[65536];

    public static void m_46418_(int[] p_46419_)
    {
        pixels = p_46419_;
    }

    public static int get(double pTemperature, double p_46417_)
    {
        p_46417_ = p_46417_ * pTemperature;
        int i = (int)((1.0D - pTemperature) * 255.0D);
        int j = (int)((1.0D - p_46417_) * 255.0D);
        int k = j << 8 | i;
        return k >= pixels.length ? -65281 : pixels[k];
    }
}
